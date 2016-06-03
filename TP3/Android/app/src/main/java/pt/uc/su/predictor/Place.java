package pt.uc.su.predictor;

import android.util.Log;
import android.util.Pair;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by pedro on 03/06/2016.
 */
public class Place {
    private long id;
    private double latitude;
    private double longitude;
    private ArrayList<Pair<Place,Integer>> destinations;
    private ArrayList<Pair<Place,Integer>> predecessors;

    public Place(long id, double latitude, double longitude) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.destinations = new ArrayList<>();
        this.predecessors = new ArrayList<>();
    }

    public Place(JSONObject object) {
        try {
            this.id = object.getJSONObject("place").getLong("id");
            this.latitude = object.getJSONObject("place").getJSONObject("location").getDouble("lat");
            this.longitude = object.getJSONObject("place").getJSONObject("location").getDouble("lon");
            this.destinations = new ArrayList<>();
            this.predecessors = new ArrayList<>();
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
    }

    public Place() {
        this.destinations = new ArrayList<>();
        this.predecessors = new ArrayList<>();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void addDestination(Place place) {
        for (Pair<Place,Integer> aux : this.destinations) {
            if (aux.first.getId() == place.getId()) {
                this.destinations.remove(aux);
                this.destinations.add(new Pair<Place, Integer>(aux.first, aux.second.intValue() + 1));
                return;
            }
        }
        this.destinations.add(new Pair<Place, Integer>(place,1));
    }

    public void addPredecessor(Place place) {
        for (Pair<Place,Integer> aux : this.predecessors) {
            if (aux.first.getId() == place.getId()) {
                this.predecessors.remove(aux);
                this.predecessors.add(new Pair<Place, Integer>(aux.first, aux.second.intValue() + 1));
                return;
            }
        }
        this.predecessors.add(new Pair<Place, Integer>(place,1));
    }

    public Place getDestination() {
        int max = 0;
        Place result = null;
        for(Pair<Place,Integer> aux:this.destinations) {
            if(aux.second.intValue() > max) {
                max = aux.second.intValue();
                result = aux.first;
            }
        }
        return result;
    }

    public Place getPredecessor() {
        int max = 0;
        Place result = null;
        for(Pair<Place,Integer> aux:this.predecessors) {
            if(aux.second.intValue() > max) {
                max = aux.second.intValue();
                result = aux.first;
            }
        }
        return result;
    }
}
