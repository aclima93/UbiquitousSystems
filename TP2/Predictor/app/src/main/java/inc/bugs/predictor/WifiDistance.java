package inc.bugs.predictor;

/**
 * Created by aclima on 28/04/16.
 */
public class WifiDistance {

    private int signalStrength;
    private WifiMeasurement wifiMeasurement;
    private Integer distance;

    WifiDistance(int signalStrength, WifiMeasurement wifiMeasurement){
        this.signalStrength = signalStrength;
        this.wifiMeasurement = wifiMeasurement;
        this.distance = Math.abs( signalStrength - wifiMeasurement.getSignalStrength() );
    }

    public int getSignalStrength() {
        return signalStrength;
    }

    public WifiMeasurement getWifiMeasurement() {
        return wifiMeasurement;
    }

    public Integer getDistance() {
        return distance;
    }
}
