package pt.uc.su.predictor;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import android.os.Handler;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private Location currentLocation;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private ArrayList<Place> places;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        this.places = loadDataFromAssets();

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

    public ArrayList<Place> loadDataFromAssets() {
        String json = null;
        ArrayList<Place> result = new ArrayList<>();
        Place cur, prev;
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
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONArray array1 = array.getJSONObject(i).getJSONArray("segments");
                prev = new Place(array1.getJSONObject(0));
                boolean found = false;
                for (Place temp : result) {
                    if (temp.getId() == prev.getId()) {
                        prev = temp;
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    result.add(prev);
                }
                cur = prev;
                for (int j = 1; j < array1.length(); j++) {
                    prev = cur;
                    cur = new Place(array1.getJSONObject(j));
                    found = false;
                    for (Place temp : result) {
                        if (temp.getId() == cur.getId()) {
                            cur = temp;
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        result.add(cur);
                    }
                    prev.addDestination(cur);
                    cur.addPredecessor(prev);

                }
            }
        } catch (JSONException je) {
            je.printStackTrace();
            return null;
        }

        return result;
    }

    public Place makePrediction() {
        if (this.currentLocation == null) {
            return null;
        }
        double latitude = this.currentLocation.getLatitude();
        double longitude = this.currentLocation.getLongitude();

        Place current = null;

        double closestLat = 0.0;
        double closestLon = 0.0;
        double guessLat = 0.0;
        double guessLon = 0.0;

        double minDistance = Double.MAX_VALUE;

        for (Place aux : this.places) {
            double distance = Math.sqrt((Math.pow((aux.getLatitude() - latitude), 2)) + (Math.pow((aux.getLongitude() - longitude), 2)));
            if (distance < minDistance) {
                minDistance = distance;
                closestLat = aux.getLatitude();
                closestLon = aux.getLongitude();
                current = aux;
            }
        }

        Place guess = current.getDestination();
        guessLat = guess.getLatitude();
        guessLon = guess.getLongitude();

        TextView t1 = (TextView) findViewById(R.id.currentLocation);
        t1.setText("Your current location (blue) is: " + latitude + " : " + longitude);
        TextView t2 = (TextView) findViewById(R.id.closestLocation);
        t2.setText("The closest stored location (pink) is: " + closestLat + " : " + closestLon);
        TextView t3 = (TextView) findViewById(R.id.nextLocation);
        t3.setText("Your next location (yellow) will probably be: " + guessLat + " : " + guessLon);

        mMap.clear();
        LatLng currentLatLng = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(currentLatLng).title("Atual").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        LatLng closestLatLng = new LatLng(closestLat, closestLon);
        mMap.addMarker(new MarkerOptions().position(closestLatLng).title("Vizinho").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
        LatLng guessLatLng = new LatLng(guessLat, guessLon);
        mMap.addMarker(new MarkerOptions().position(guessLatLng).title("Próximo").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(closestLatLng));

        return guess;
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
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(base, 10));
    }
}
