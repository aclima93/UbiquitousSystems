package pt.uc.su.predictor;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import android.os.Handler;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private Location currentLocation;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private ArrayList<ArrayList<Entry>> entries;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        this.entries = loadDataFromAssets();

        final Handler handler = new Handler();
        final int delay = 1000;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                makePrediction();
                handler.postDelayed(this, delay);
            }
        }, delay);

        this.locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                currentLocation = location;
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

    }

    public ArrayList<ArrayList<Entry>> loadDataFromAssets() {
        String json = null;
        ArrayList<ArrayList<Entry>> result = new ArrayList<>();
        try {
            InputStream is = getAssets().open("places.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return null;
        }

        try {
            JSONArray days = new JSONArray(json);
            for (int i = 0; i < days.length(); i++) {
                ArrayList<Entry> today = new ArrayList<>();
                JSONArray places = days.getJSONObject(i).getJSONArray("segments");

                for (int j = 0; j < places.length(); j++) {
                    today.add(new Entry(places.getJSONObject(j)));
                }
                result.add(today);
            }
        } catch (JSONException je) {
            je.printStackTrace();
            return null;
        }

        return result;
    }

    public void makePrediction() {
        if (this.currentLocation == null) {
            return;
        }
        double latitude = this.currentLocation.getLatitude();
        double longitude = this.currentLocation.getLongitude();

        double closestLat = 0.0;
        double closestLon = 0.0;

        double guessLat = 0.0;
        double guessLon = 0.0;

        double minDistance = Double.MAX_VALUE;

        for(ArrayList<Entry> day : this.entries) {
            for(Entry entry : day) {
                double distance = Math.sqrt((Math.pow((entry.getLatitude() - latitude), 2)) + (Math.pow((entry.getLongitude() - longitude), 2)));
                if (distance < minDistance) {
                    minDistance = distance;
                    closestLat = entry.getLatitude();
                    closestLon = entry.getLongitude();
                }
            }
        }

        Date now = new Date();
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(now);

        int nowHour = calendar.get(Calendar.HOUR_OF_DAY);
        int nowMinute = calendar.get(Calendar.MINUTE);
        int nowSecond = calendar.get(Calendar.SECOND);

        ArrayList<Long> background = new ArrayList<>();
        ArrayList<Long> follow_ups = new ArrayList<>();

        for(int i = 0; i<this.entries.size(); i++) {
            for (int j = 0; j<this.entries.get(i).size(); j++) {
                if(between(nowHour, nowMinute, nowSecond, this.entries.get(i).get(j))) {
                    if(j < this.entries.get(i).size()-1) {
                        background.add(this.entries.get(i).get(j).getId());
                        follow_ups.add(this.entries.get(i).get(j+1).getId());
                    }
                    else if(i < this.entries.size()-1) {
                        background.add(this.entries.get(i).get(j).getId());
                        follow_ups.add(this.entries.get(i+1).get(0).getId());
                    }
                }
            }
        }

        HashMap<Long,Integer> options = new HashMap<>();
        for(Long current : background) {
            if(!options.containsKey(current)) {
                options.put(current,1);
            } else {
                options.put(current, options.get(current)+1);
            }
        }

        int bestCount = 0;
        long bestId = 0;

        long bestDuration = 0;
        int bestStartTime = 0;

        for(Long current : options.keySet()) {
            if(options.get(current) > bestCount) {
                bestCount = options.get(current);
                bestId = current;
            }
        }

        for(ArrayList<Entry> day : this.entries) {
            for(Entry current : day) {
                if(current.getId() == bestId) {
                    bestDuration += current.getDuration();
                    bestStartTime += (3600*current.getStartHour() + 60*current.getStartMinute() + current.getStartSecond());
                    guessLat = current.getLatitude();
                    guessLon = current.getLongitude();
                }
            }
        }

        bestDuration/=bestCount;
        bestStartTime/=bestCount;

        TextView t1 = (TextView) findViewById(R.id.currentLocation);
        t1.setText("Your current location is in blue.");
        TextView t2 = (TextView) findViewById(R.id.closestLocation);
        t2.setText("The closest stored location is in pink.");
        if(bestCount != 0) {
            TextView t3 = (TextView) findViewById(R.id.nextLocation);
            t3.setText("Our guessed location is in yellow. This is based in " + bestCount + " previous read" + (bestCount>1?"s.":".") + "You'll be there for " + this.getDurationString(bestDuration) + "You'll arrive there at around " + this.getStartString(bestStartTime));
        } else {
            TextView t3 = (TextView) findViewById(R.id.nextLocation);
            t3.setText("We were unable to guess your next location. Try moving more often!");
        }

        mMap.clear();
        LatLng currentLatLng = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(currentLatLng).title("Current").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        LatLng closestLatLng = new LatLng(closestLat, closestLon);
        mMap.addMarker(new MarkerOptions().position(closestLatLng).title("Closest").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
        LatLng guessLatLng = new LatLng(guessLat, guessLon);
        if(bestCount != 0) {
            mMap.addMarker(new MarkerOptions().position(guessLatLng).title("Guess").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
        }
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(currentLatLng);
        builder.include(closestLatLng);
        if(bestCount!=0) {
            builder.include(guessLatLng);
        }
        LatLngBounds bounds = builder.build();

        int padding = 75;
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds,padding);

        mMap.animateCamera(cameraUpdate);
    }

    public boolean between(int hour, int minute, int second, Entry test) {
        boolean afterStart = false;
        boolean beforeEnd = false;

        if( (hour > test.getStartHour()) || (hour==test.getStartHour() && minute > test.getStartMinute()) || (hour==test.getStartHour() && minute==test.getStartMinute() && second > test.getStartSecond()) ) {
            afterStart = true;
        }
        if( (hour < test.getEndHour()) || (hour==test.getEndHour() && minute < test.getEndMinute()) || (hour==test.getEndHour() && minute==test.getEndMinute() && second < test.getEndSecond()) ) {
            beforeEnd = true;
        }

        if(afterStart && beforeEnd) {
            return true;
        }
        return false;
    }

    public String getDurationString(long duration) {
        long hours = duration / 3600;
        long minutes = (duration - hours*3600)/ 60;
        long seconds = duration - hours*3600 - minutes*60;
        if(hours == 0) {
            if(minutes == 0) {
                if(seconds == 0) {
                    return "no time at all.";
                } else {
                    return seconds + " second" + (seconds>1?"s.":".");
                }
            } else {
                if(seconds == 0) {
                    return minutes + " minute" + (seconds>1?"s.":".");
                } else {
                    return minutes + " minute" + (seconds>1?"s e ":" e ") + seconds + " second" + (seconds>1?"s.":".");
                }
            }
        } else {
            if(minutes == 0) {
                if(seconds == 0) {
                    return hours + " hour" + (seconds>1?"s.":".");
                } else {
                    return hours + " hour" + (seconds>1?"s e ":" e ") + seconds + " second" + (seconds>1?"s.":".");
                }
            } else {
                if(seconds == 0) {
                    return hours + " hour" + (seconds>1?"s e ":" e ") + minutes + " minute" + (minutes>1?"s.":".");
                } else {
                    return hours + " hour" + (seconds>1?"s, ":", ") + minutes + " minute" + (minutes>1?"s e ":" e ") + seconds + " second" + (seconds>1?"s.":".");
                }
            }
        }
    }

    public String getStartString(long start) {
        long hours = start / 3600;
        long minutes = (start - hours*3600)/ 60;
        long seconds = start - hours*3600 - minutes*60;
        return hours + "h" + minutes + "m" + seconds + ".";
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.update_location) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return false;
            }
            this.locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
            this.currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng base = new LatLng(40.1867241, -8.4157713);
        mMap.addMarker(new MarkerOptions().position(base).title("Base marker"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(base, 15));
    }
}
