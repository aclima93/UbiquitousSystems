package inc.bugs.logger;

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

    @Override
    public String toString(){
        return signalStrength + "," + BSSID + "," + location + "\n";
    }
}
