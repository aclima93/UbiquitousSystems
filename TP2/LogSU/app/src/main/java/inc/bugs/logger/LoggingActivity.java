package inc.bugs.logger;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class LoggingActivity extends AppCompatActivity {

    File wifiLogsFile;
    int successCounter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logging);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        wifiLogsFile = getStorageFile();

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

            //TODO: add magnetometer information to log

            EditText editText = (EditText) findViewById(R.id.cur_location_etv);
            String location = (editText != null ? editText.getText().toString() : null);

            if (location == null) {
                Toast.makeText(getApplicationContext(), "Missing Location", Toast.LENGTH_SHORT).show();
                return;
            }


            final WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

            if (wifi.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {

                for (ScanResult result : wifi.getScanResults()) {

                    int rssi = wifi.getConnectionInfo().getRssi(); // received signal strength indicator in dBm
                    int signalStrength = WifiManager.calculateSignalLevel(rssi, result.level);

                    String log = result.BSSID + "," + signalStrength + "," + location;

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

}
