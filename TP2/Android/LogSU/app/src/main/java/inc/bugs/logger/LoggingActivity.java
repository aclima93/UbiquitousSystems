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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoggingActivity extends AppCompatActivity implements SensorEventListener, AdapterView.OnItemSelectedListener {

    TextView orientationTextView;

    FloatingActionButton fab;

    SensorManager sensorManager;
    Sensor accelerometerSensor;
    Sensor magnetometerSensor;
    float[] gravityValues;
    float[] geomagneticValues;

    String location;
    String orientation;

    File wifiLogsFile;
    ArrayList<WifiMeasurement> measurements;

    int successCounter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logging);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        wifiLogsFile = getStorageFile();

        Button resetButton = (Button) findViewById(R.id.reset_btn);
        assert resetButton != null;
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                while( wifiLogsFile.exists() ){
                    if(wifiLogsFile.delete()) {
                        break;
                    }
                }
                wifiLogsFile = getStorageFile();
            }
        });

        orientationTextView = (TextView) findViewById(R.id.cur_orientation_tv);
        Spinner locationSpinner = (Spinner) findViewById(R.id.cur_location_spinner);

        // Spinner Drop down elements
        List<String> categories = new ArrayList<>();
        categories.add("A");
        categories.add("B");
        categories.add("C");
        categories.add("D1");
        categories.add("D2");
        categories.add("D3");
        categories.add("E");
        categories.add("G");

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner + click listener
        if (locationSpinner != null) {
            locationSpinner.setOnItemSelectedListener(this);
            locationSpinner.setAdapter(dataAdapter);
        }

        // floating action button
        fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    fab.setVisibility(View.GONE);
                    addSignals();
                    fab.setVisibility(View.VISIBLE);
                }
            });
        }

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
        location = parent.getItemAtPosition(position).toString();
    }

    public void onNothingSelected(AdapterView<?> arg0) { }

    private void addSignals() {

        successCounter = 0;

        if (location == null) {
            Toast.makeText(getApplicationContext(), "Missing Location", Toast.LENGTH_SHORT).show();
            return;
        }
        if (orientation == null){
            Toast.makeText(getApplicationContext(), "Turn on Compass", Toast.LENGTH_SHORT).show();
            return;
        }

        String locationAndOrientation = location + " - " + orientation;

        final WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        measurements = new ArrayList<>();
        while(successCounter < 30) {

            if (wifi.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {

                for (ScanResult result : wifi.getScanResults()) {

                    int signalStrength = wifi.getConnectionInfo().getRssi(); // received signal strength indicator in dBm

                    measurements.add(new WifiMeasurement(signalStrength, result.BSSID, locationAndOrientation));

                }

                successCounter++;

                // sleep for a little bit for more diverse measurements
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
            else{
                Toast.makeText(getApplicationContext(), "Turn on WiFi", Toast.LENGTH_SHORT).show();
                return;
            }

        }

        HashMap<String, Integer> counter = new HashMap<>();
        for(WifiMeasurement measurement : measurements) {

            String key = measurement.toString();

            if (counter.containsKey(key)){
                counter.put(key, counter.get(key) + 1);
            }
            else {
                counter.put(key, 1);
            }
        }

        // return most frequent location of the K neighbours
        ArrayList<String> mostFrequentMeasurement = new ArrayList<>();
        int mostFrequentCount = 0;

        for(Map.Entry<String, Integer> measurementCounter : counter.entrySet()){

            if(measurementCounter.getValue() > mostFrequentCount) {
                mostFrequentCount = measurementCounter.getValue();
                mostFrequentMeasurement = new ArrayList<>();
                mostFrequentMeasurement.add(measurementCounter.getKey());
            }
            else if(measurementCounter.getValue() == mostFrequentCount) {
                mostFrequentMeasurement.add(measurementCounter.getKey());
            }
        }

        try{

            for(String measurement : mostFrequentMeasurement) {

                FileOutputStream fileOutputStream = new FileOutputStream(wifiLogsFile, true);
                fileOutputStream.write(measurement.getBytes());
                fileOutputStream.close();

                Log.d("Entry", measurement);
            }

            Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "IO Failure", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, magnetometerSensor, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
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

            File sdCard = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
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

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            gravityValues = event.values;
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagneticValues = event.values;
        }

        if (gravityValues != null && geomagneticValues != null) {

            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, gravityValues, geomagneticValues);

            if (success) {

                // orientations contains: azimuth, pitch and roll
                float orientations[] = new float[3];
                SensorManager.getOrientation(R, orientations);

                int azimuth = (int) (orientations[0] * 180 / Math.PI);
                azimuth = azimuth % 360; // value between [0, 360[ for easier verification

                if( (azimuth >= 315 && azimuth < 360) || (azimuth >= 0 && azimuth < 45) ){
                    orientation = "N";
                }
                else if(azimuth >= 45 && azimuth < 135){
                    orientation = "E";
                }
                else if(azimuth >= 135 && azimuth < 225){
                    orientation = "S";
                }
                else{
                    orientation = "W";
                }

                orientationTextView.setText(orientation);
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
