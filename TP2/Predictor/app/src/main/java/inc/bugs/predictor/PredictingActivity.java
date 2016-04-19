package inc.bugs.predictor;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

public class PredictingActivity extends AppCompatActivity {

    HashMap<String, ArrayList< ArrayList<String>> > refTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_predicting);

        createRefTable();

        while(true){

            matchCurrentSignal();

            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private void matchCurrentSignal() {

        //TODO: find closest match
        //TODO: find 2nd closest match

        //TODO: if closest == last -> closest = prev
        //TODO: if 2nd closest == last -> 2nd closest = next

        //((TextView) findViewById(R.id.prev_location_tv)).setText();
        //((TextView) findViewById(R.id.next_location_tv)).setText();

    }

    private void createRefTable() {

        refTable = new HashMap<>();

        File wifiLogsFile = getStorageFile();
        ArrayList<String> lines = new ArrayList<>();
        try {
            lines = getStringFromFile(wifiLogsFile);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "IO Failure", Toast.LENGTH_SHORT).show();
        }

        for(String line : lines) {

            ArrayList<String> parsedLine = (ArrayList<String>) Arrays.asList(line.split(","));
            String rssi = parsedLine.get(0);
            ArrayList< ArrayList<String>> storedLines = refTable.get(rssi);
            storedLines.add(parsedLine);

            refTable.put(rssi, storedLines);

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
