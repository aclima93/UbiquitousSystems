package inc.bugs.predictor;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class PredictingActivity extends AppCompatActivity {

    ArrayList<WifiMeasurement> measurements;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_predicting);

        createRefTable();

        TextView locationTextView = ((TextView) findViewById(R.id.location_tv));
        if (locationTextView != null) {
            EditText numberOfNeighboursEditText = (EditText) findViewById(R.id.number_of_neighbours_etv);

            if (numberOfNeighboursEditText != null) {

                while (true) {

                    try {
                        int numberOfNeighbours = Integer.valueOf(numberOfNeighboursEditText.getText().toString());

                        if (numberOfNeighbours > measurements.size()) {
                            numberOfNeighbours = measurements.size();
                        } else if (numberOfNeighbours < 1) {
                            numberOfNeighbours = 1;
                        }

                        String location = matchCurrentSignal(numberOfNeighbours);

                        locationTextView.setText(location);

                    } catch (NumberFormatException e) {
                        locationTextView.setText("Invalid number of neighbours.");
                    }

                    // sleep for a little bit
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

            }

        }

    }

    private String matchCurrentSignal(int numberOfNeighbours) {

        final WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        if(wifi.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {

            String bssid = wifi.getConnectionInfo().getBSSID(); // basic service set identifier
            Integer signalStrength = wifi.getConnectionInfo().getRssi(); // received signal strength indicator in dBm


            // calculate euclidean distance to each measurement

            // sort by distance

            // return most frequent location

            //return measurements.get(bssid).get(signalStrength);

        }

        return "Not defined";

    }

    private void createRefTable() {

        measurements = new ArrayList<>();

        File wifiLogsFile = getStorageFile();
        ArrayList<String> lines = new ArrayList<>();
        try {
            lines = getStringFromFile(wifiLogsFile);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "IO Failure", Toast.LENGTH_SHORT).show();
        }

        for(String line : lines) {
            measurements.add( new WifiMeasurement(line) );
        }

    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /* Checks if external storage is available to at least read */
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

    public ArrayList<String> convertStreamToString(InputStream is) throws Exception {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ArrayList<String> lines = new ArrayList<>();
        String line;

        while ((line = reader.readLine()) != null) {
            lines.add(line);
        }

        reader.close();
        return lines;
    }

    public ArrayList<String> getStringFromFile (File file) throws Exception {

        FileInputStream fin = new FileInputStream(file);

        ArrayList<String> ret = convertStreamToString(fin);

        fin.close();
        return ret;
    }

}
