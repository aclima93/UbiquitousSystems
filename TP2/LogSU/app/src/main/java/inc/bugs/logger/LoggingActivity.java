package inc.bugs.logger;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class LoggingActivity extends AppCompatActivity {

    EditText editText;
    File wifiLogsFile;
    private SensorEventListener mSensorEventListener;
    String orientation;
    int successCounter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logging);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        wifiLogsFile = getStorageFile();
        mSensorEventListener = new SensorEventListener() {

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }

            @Override
            public void onSensorChanged(SensorEvent event) {

                float[] mGravity = null;
                float[] mMagnetic = null;

                if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
                    mGravity = event.values.clone();
                }
                if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                    mMagnetic = event.values.clone();
                }

                if (mGravity != null && mMagnetic != null) {

                    // Create rotation Matrix
                    float[] rotationMatrix = new float[9];
                    if (SensorManager.getRotationMatrix(rotationMatrix, null,
                            mGravity, mMagnetic)) {

                        // Compensate device orientation
                        // http://android-developers.blogspot.de/2010/09/one-screen-turn-deserves-another.html
                        float[] remappedRotationMatrix = new float[9];
                        switch (getWindowManager().getDefaultDisplay()
                                .getRotation()) {
                            case Surface.ROTATION_0:
                                SensorManager.remapCoordinateSystem(rotationMatrix,
                                        SensorManager.AXIS_X, SensorManager.AXIS_Y,
                                        remappedRotationMatrix);
                                break;
                            case Surface.ROTATION_90:
                                SensorManager.remapCoordinateSystem(rotationMatrix,
                                        SensorManager.AXIS_Y,
                                        SensorManager.AXIS_MINUS_X,
                                        remappedRotationMatrix);
                                break;
                            case Surface.ROTATION_180:
                                SensorManager.remapCoordinateSystem(rotationMatrix,
                                        SensorManager.AXIS_MINUS_X,
                                        SensorManager.AXIS_MINUS_Y,
                                        remappedRotationMatrix);
                                break;
                            case Surface.ROTATION_270:
                                SensorManager.remapCoordinateSystem(rotationMatrix,
                                        SensorManager.AXIS_MINUS_Y,
                                        SensorManager.AXIS_X, remappedRotationMatrix);
                                break;
                        }

                        // Calculate Orientation
                        float results[] = new float[3];
                        SensorManager.getOrientation(remappedRotationMatrix,
                                results);

                        // Get measured value
                        float current_measured_bearing = (float) (results[0] * 180 / Math.PI);
                        if (current_measured_bearing < 0) {
                            current_measured_bearing += 360;
                        }

                        if(current_measured_bearing >= 0 && current_measured_bearing < 90){
                            orientation = "N";
                        }
                        else if(current_measured_bearing >= 90 && current_measured_bearing < 180){
                            orientation = "E";
                        }
                        else if(current_measured_bearing >= 90 && current_measured_bearing < 180){
                            orientation = "S";
                        }
                        else{
                            orientation = "W";
                        }

                    }
                }
            }
        };

        initSensors();

        EditText editText = (EditText) findViewById(R.id.cur_location_etv);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    addSignals();
                }
            });
        }

    }

    private void addSignals() {

        successCounter = 0;

        while(successCounter < 30) {

            String location = (editText != null ? editText.getText().toString() : null);

            if (location == null) {
                Toast.makeText(getApplicationContext(), "Missing Location", Toast.LENGTH_SHORT).show();
                return;
            }

            location += " - " + orientation;

            final WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

            if (wifi.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {

                for (ScanResult result : wifi.getScanResults()) {

                    int signalStrength = wifi.getConnectionInfo().getRssi(); // received signal strength indicator in dBm

                    String log = new WifiMeasurement(signalStrength, result.BSSID, location).toString();

                    try {
                        FileWriter fw = new FileWriter(wifiLogsFile, true);
                        BufferedWriter bw = new BufferedWriter(fw);
                        PrintWriter out = new PrintWriter(bw);

                        out.println(log);
                        Log.d("Entry", log);

                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "IO Failure", Toast.LENGTH_SHORT).show();
                    }

                }

                successCounter++;
                Toast.makeText(getApplicationContext(), "Success #" + successCounter, Toast.LENGTH_SHORT).show();

            }

            // sleep for a little bit
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }

    /**
     * Initialize the Sensors (Gravity and magnetic field, required as a compass
     * sensor)
     */
    private void initSensors() {

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor mSensorGravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        Sensor mSensorMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        // Initialize the gravity sensor
        if (mSensorGravity != null) {
            Log.i("Grav", "Gravity sensor available. (TYPE_GRAVITY)");
            sensorManager.registerListener(mSensorEventListener, mSensorGravity, SensorManager.SENSOR_DELAY_GAME);
        }

        // Initialize the magnetic field sensor
        if (mSensorMagneticField != null) {
            Log.i("Mag", "Magnetic field sensor available. (TYPE_MAGNETIC_FIELD)");
            sensorManager.registerListener(mSensorEventListener, mSensorMagneticField, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    /**
     *  Checks if external storage is available for read and write
     */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     *  Checks if external storage is available to at least read
     */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    public File getStorageFile() {

        File file = null;

        if( isExternalStorageReadable() && isExternalStorageWritable() ) {

            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File (sdCard.getAbsolutePath() + "/wifi_logs");
            file = new File(dir, "wifi_logs.txt");

            if(!dir.mkdirs() && !file.exists()) {
                try {
                    if (!file.createNewFile()) {
                        Log.e("Oops!", "File not created.");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "IO Failure", Toast.LENGTH_SHORT).show();
                }
            }

        }

        return file;
    }

}
