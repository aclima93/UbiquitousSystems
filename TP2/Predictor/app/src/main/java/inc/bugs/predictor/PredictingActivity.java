package inc.bugs.predictor;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class PredictingActivity extends AppCompatActivity {

    // BSSID > Signal Strength > Location
    HashMap<String, HashMap<Integer, String> > referenceTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_predicting);

        createRefTable();

        while(true){

            matchCurrentSignal();

            // sleep for a little bit
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private void matchCurrentSignal() {
        
        final WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        if(wifi.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {

            String bssid = wifi.getConnectionInfo().getBSSID(); // basic service set identifier
            Integer signalStrength = wifi.getConnectionInfo().getRssi(); // received signal strength indicator in dBm
            String location;

            try {
                location = referenceTable.get(bssid).get(signalStrength);
            }
            catch (Exception e){
                location = "Not defined";
            }

            TextView locationTextView = ((TextView) findViewById(R.id.location_tv));
            if (locationTextView != null) {
                locationTextView.setText(location);
            }
        }

    }

    private void createRefTable() {

        referenceTable = new HashMap<>();

        File wifiLogsFile = getStorageFile();
        ArrayList<String> lines = new ArrayList<>();
        try {
            lines = getStringFromFile(wifiLogsFile);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "IO Failure", Toast.LENGTH_SHORT).show();
        }

        // BSSID > Signal Strength > Location
        HashMap<String, HashMap<Integer, ArrayList<String>> > logsTable = new HashMap<>();

        for(String line : lines) {

            // parse logged line
            ArrayList<String> parsedLine = (ArrayList<String>) Arrays.asList(line.split(","));
            String bssid = parsedLine.get(0);
            Integer signalStrength = Integer.parseInt( parsedLine.get( 1 ) );
            String location = parsedLine.get(2);

            // init/retrive data
            HashMap<Integer, ArrayList<String>> signalStrengthList = logsTable.get(bssid);
            if( signalStrengthList == null || signalStrengthList.size() == 0 ) {
                signalStrengthList = new HashMap<>();
            }
            ArrayList<String> locations = signalStrengthList.get(location);
            if( locations == null || locations.size() == 0 ) {
                locations = new ArrayList<>();
            }

            // update table
            locations.add(location);
            signalStrengthList.put(signalStrength, locations);
            logsTable.put(bssid, signalStrengthList);

        }

        // calculate average signal strength

        for( String bssid : logsTable.keySet() ){

            for(Integer signalStrength : logsTable.get(bssid).keySet() ){

                ArrayList<String> locationList = logsTable.get(bssid).get(signalStrength);
                ArrayList<String> uniqueLocations = new ArrayList<>( new HashSet<>(locationList) );
                Integer[] locationCounter = new Integer[uniqueLocations.size()];

                // count frequency of each location for this BSSID and SignalStrength
                for(int i=0; i< locationList.size(); i++){
                    locationCounter[ uniqueLocations.indexOf(locationList.get(i)) ]++;
                }

                // determine most frequent
                int mostFrequent = 0;
                for(int i=0; i< locationCounter.length; i++){
                    if(locationCounter[mostFrequent] > locationCounter[i]){
                        mostFrequent = i;
                    }
                }

                HashMap<Integer, String> locationForSignalStrength = referenceTable.get(bssid);
                if( locationForSignalStrength == null || locationForSignalStrength.size() == 0 ) {
                    locationForSignalStrength= new HashMap<>();
                }

                locationForSignalStrength.put(signalStrength, uniqueLocations.get(mostFrequent));
                referenceTable.put(bssid, locationForSignalStrength);

            }

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
