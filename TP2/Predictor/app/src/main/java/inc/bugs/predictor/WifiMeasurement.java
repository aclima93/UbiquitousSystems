package inc.bugs.predictor;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by aclima on 28/04/16.
 */
public class WifiMeasurement {

    private int signalStrength;
    private String BSSID;
    private String location;

    WifiMeasurement(String csvLine){

        // parse logged line
        ArrayList<String> parsedLine = new ArrayList<>();
        parsedLine.addAll(Arrays.asList(csvLine.split(",")));

        int idx = 0;
        this.signalStrength = Integer.parseInt( parsedLine.get( idx++ ) );
        this.BSSID = parsedLine.get(idx++);
        this.location = parsedLine.get(idx++);

    }

    public int getSignalStrength() {
        return signalStrength;
    }

    public String getBSSID() {
        return BSSID;
    }

    public String getLocation() {
        return location;
    }

}
