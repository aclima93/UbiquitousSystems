package inc.bugs.predictor;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class PredictingActivity extends AppCompatActivity {

    ArrayList<WifiMeasurement> measurements;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_predicting);

        createRefTable();

        final EditText numberOfNeighboursEditText = (EditText) findViewById(R.id.number_of_neighbours_etv);
        final TextView locationTextView = (TextView) findViewById(R.id.location_tv);
        final ImageView locationImageView = (ImageView) findViewById(R.id.location_img);

        if (locationTextView != null && locationImageView != null && numberOfNeighboursEditText != null) {

            final Handler h = new Handler();
            final int delay = 1000; //milliseconds

            h.postDelayed(new Runnable(){
                public void run(){

                    try {
                        int numberOfNeighbours = Integer.valueOf(numberOfNeighboursEditText.getText().toString());

                        if (numberOfNeighbours > measurements.size()) {
                            numberOfNeighbours = measurements.size();
                        } else if (numberOfNeighbours < 1) {
                            numberOfNeighbours = 1;
                        }

                        ArrayList<String> predictedLocations = matchCurrentSignal(numberOfNeighbours);
                        String predictedLocation = null;

                        if(predictedLocations != null && predictedLocations.size() > 0){

                            predictedLocation = predictedLocations.get(0);
                            predictedLocations.remove(0);

                            for(String location : predictedLocations){
                                predictedLocation += ", " + location;
                            }
                        }

                        if (predictedLocation == null){

                            locationTextView.setText( "Unknown Location.");
                            locationImageView.setImageDrawable(getResources().getDrawable(R.drawable._default));

                        }

                        else {

                            locationTextView.setText(predictedLocation);

                            String predictedLocationImg = "";
                            /*
                             *  Check only the first two most frequent predicted locations
                             *  because we don't have generated images for all n-combinations.
                             */
                            if( predictedLocations.size() >= 2 ) {
                                String location1 = predictedLocations.get(0).split(" - ")[0];
                                String location2 = predictedLocations.get(1).split(" - ")[0];

                                if (location1.compareTo(location2) < 0){
                                    predictedLocationImg = location1.charAt(0) + "_" + location2.charAt(0);
                                }
                                else {
                                    predictedLocationImg = location2.charAt(0) + "_" + location1.charAt(0);
                                }

                            }
                            else if(predictedLocations.size() == 1){
                                predictedLocationImg = predictedLocations.get(0).split(" - ")[0];
                            }

                            Log.d("pL", predictedLocation);
                            Log.d("plImg", predictedLocationImg);

                            int imageId;
                            switch (predictedLocationImg){

                                case "A" :imageId = R.drawable.a; break;
                                case "A_B" :imageId = R.drawable.a_b; break;
                                case "A_D" :imageId = R.drawable.a_d; break;

                                case "B" :imageId = R.drawable.b; break;
                                case "B_C" :imageId = R.drawable.b_c; break;
                                case "B_D" :imageId = R.drawable.b_d; break;

                                case "C" :imageId = R.drawable.c; break;
                                case "C_D" :imageId = R.drawable.c_d; break;
                                case "C_E" :imageId = R.drawable.c_e; break;

                                case "D1" :imageId = R.drawable.d1; break;
                                case "D2" :imageId = R.drawable.d2; break;
                                case "D3" :imageId = R.drawable.d3; break;
                                case "D_E" :imageId = R.drawable.d_e; break;
                                case "D_G" :imageId = R.drawable.d_g; break;

                                case "E" :imageId = R.drawable.e; break;
                                case "E_G" :imageId = R.drawable.e_g; break;

                                case "G" :imageId = R.drawable.g; break;

                                default: imageId = R.drawable._default;

                            }

                            locationImageView.setImageDrawable(getResources().getDrawable(imageId));
                        }

                    } catch (NumberFormatException e) {

                        locationTextView.setText("Invalid number of neighbours.");
                        locationImageView.setImageDrawable(getResources().getDrawable(R.drawable._default));
                    }

                    h.postDelayed(this, delay);
                }
            }, delay);

        }

    }

    private ArrayList<String> matchCurrentSignal(int numberOfNeighbours) {

        final WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        if(wifi.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {

            Integer signalStrength = wifi.getConnectionInfo().getRssi(); // received signal strength indicator in dBm

            ArrayList<WifiDistance> distances = new ArrayList<>();

            // calculate euclidean distance to each measurement
            for(WifiMeasurement wifiMeasurement : measurements){
                distances.add(new WifiDistance(signalStrength, wifiMeasurement));
            }

            // sort by distance
            Collections.sort(distances, new Comparator<WifiDistance>() {
                @Override
                public int compare(WifiDistance o1, WifiDistance o2) {
                    return o1.getDistance().compareTo(o2.getDistance());
                }
            });

            // add up what class each neighbour belongs to
            HashMap<String, Integer> counter = new HashMap<>();
            for(int i=0; i<numberOfNeighbours; i++) {

                String location = distances.get(i).getWifiMeasurement().getLocation();

                if (counter.containsKey(location)){
                    counter.put(location, counter.get(location) + 1);
                }
                else {
                    counter.put(location, 1);
                }
            }

            // return most frequent location of the K neighbours
            ArrayList<String> mostFrequentLocations = new ArrayList<>();
            int mostFrequentCount = 0;

            for(Map.Entry<String, Integer> entryLocation : counter.entrySet()){

                if(entryLocation.getValue() > mostFrequentCount) {
                    mostFrequentCount = entryLocation.getValue();
                    mostFrequentLocations = new ArrayList<>();
                    mostFrequentLocations.add(entryLocation.getKey());
                }
                else if(entryLocation.getValue() == mostFrequentCount) {
                    mostFrequentLocations.add(entryLocation.getKey());
                }
            }

            return mostFrequentLocations;

        }
        else{
            Toast.makeText(getApplicationContext(), "Turn on WiFi", Toast.LENGTH_SHORT).show();
        }

        return null;
    }

    private void createRefTable() {

        measurements = new ArrayList<>();

        File wifiLogsFile = getStorageFile();
        ArrayList<String> lines = new ArrayList<>();

        if (wifiLogsFile == null){

            InputStream inputStream;

            try {
                inputStream = getAssets().open("wifi_logs.txt");
                lines = convertStreamToString( inputStream );
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "IO Failure", Toast.LENGTH_SHORT).show();
            }

        }
        else {


            try {
                lines = getStringFromFile(wifiLogsFile);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "IO Failure", Toast.LENGTH_SHORT).show();
            }
        }

        for(String line : lines) {
            measurements.add( new WifiMeasurement(line) );
        }

    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    public File getStorageFile() {

        File file = null;

        if( isExternalStorageReadable() ) {

            File sdCard = new File(Environment.getExternalStorageDirectory() + "/Documents");
            File dir = new File (sdCard.getAbsolutePath() + "/wifi_logs");
            file = new File(dir, "wifi_logs.txt");

            if(!dir.mkdirs() && !file.exists()) {
                try {
                    if (!file.createNewFile()) {
                        Log.e("Oops!", "File not created.");
                        return null;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "IO Failure", Toast.LENGTH_SHORT).show();
                    return null;
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
