package pt.uc.su.predictor;

import android.util.Pair;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by pedro on 03/06/2016.
 */
public class Entry {
    private long id;
    private double latitude;
    private double longitude;
    private Date start;
    private Date end;
    private int startHour, startMinute, startSecond;
    private int endHour, endMinute, endSecond;

    public Entry(long id, double latitude, double longitude, Date start, Date end) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.start = start;
        this.end = end;

        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(this.start);

        this.startHour = calendar.get(Calendar.HOUR_OF_DAY);
        this.startMinute = calendar.get(Calendar.MINUTE);
        this.startSecond = calendar.get(Calendar.SECOND);

        calendar.setTime(this.end);

        this.endHour = calendar.get(Calendar.HOUR_OF_DAY);
        this.endMinute = calendar.get(Calendar.MINUTE);
        this.endSecond = calendar.get(Calendar.SECOND);
    }

    public Entry(JSONObject object) {
        try {
            this.id = object.getJSONObject("place").getLong("id");
            this.latitude = object.getJSONObject("place").getJSONObject("location").getDouble("lat");
            this.longitude = object.getJSONObject("place").getJSONObject("location").getDouble("lon");
            this.start = new SimpleDateFormat("yyyyMMdd'T'HHmmss'+0100'").parse(object.getString("startTime"));
            this.end = new SimpleDateFormat("yyyyMMdd'T'HHmmss'+0100'").parse(object.getString("endTime"));

            Calendar calendar = GregorianCalendar.getInstance();
            calendar.setTime(this.start);

            this.startHour = calendar.get(Calendar.HOUR_OF_DAY);
            this.startMinute = calendar.get(Calendar.MINUTE);
            this.startSecond = calendar.get(Calendar.SECOND);

            System.out.println("Starting at " + startHour + " h " + startMinute + " m " + startSecond);

            calendar.setTime(this.end);

            this.endHour = calendar.get(Calendar.HOUR_OF_DAY);
            this.endMinute = calendar.get(Calendar.MINUTE);
            this.endSecond = calendar.get(Calendar.SECOND);
        } catch (JSONException|ParseException e) {
            e.printStackTrace();
            return;
        }
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

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public int getStartHour() {
        return startHour;
    }

    public void setStartHour(int startHour) {
        this.startHour = startHour;
    }

    public int getStartMinute() {
        return startMinute;
    }

    public void setStartMinute(int startMinute) {
        this.startMinute = startMinute;
    }

    public int getStartSecond() {
        return startSecond;
    }

    public void setStartSecond(int startSecond) {
        this.startSecond = startSecond;
    }

    public int getEndHour() {
        return endHour;
    }

    public void setEndHour(int endHour) {
        this.endHour = endHour;
    }

    public int getEndMinute() {
        return endMinute;
    }

    public void setEndMinute(int endMinute) {
        this.endMinute = endMinute;
    }

    public int getEndSecond() {
        return endSecond;
    }

    public void setEndSecond(int endSecond) {
        this.endSecond = endSecond;
    }

    public long getDuration() {
        int hours = 0;
        int minutes = 0;
        int seconds = 0;
        if(startHour <= endHour) {
            hours = endHour - startHour;
        } else {
            hours = endHour - startHour - 1;
        }
        if(startMinute <= endMinute) {
            minutes = endMinute - startMinute;
        } else {
            minutes = endMinute - startMinute - 1;
        }
        if(startSecond <= endSecond) {
            seconds = endSecond - startSecond;
        } else {
            seconds = endSecond - startSecond - 1;
        }
        return hours*3600 + minutes*60 + seconds;
    }
}
