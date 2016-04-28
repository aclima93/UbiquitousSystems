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

    WifiMeasurement(int signalStrength, String BSSID, String location){
        this.signalStrength = signalStrength;
        this.BSSID = BSSID;
        this.location = location;
    }

    WifiMeasurement(String csvLine){

        // parse logged line
        ArrayList<String> parsedLine = (ArrayList<String>) Arrays.asList(csvLine.split(","));
        this.BSSID = parsedLine.get(0);
        this.signalStrength = Integer.parseInt( parsedLine.get( 1 ) );
        this.location = parsedLine.get(2);

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

    @Override
    public String toString(){
        return signalStrength + "," + BSSID + "," + location;
    }

}
