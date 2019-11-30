package eu.johannes.runninggag;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;

public class OnlyOneRun implements Parcelable {
    private double distance;
    private int points;

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getStopTime() {
        return stopTime;
    }

    public void setStopTime(long stopTime) {
        this.stopTime = stopTime;
    }

    private long startTime;
    private long stopTime;
    private transient ArrayList<DataPoint> dataPoints = new ArrayList<>();

    public ArrayList<RunTime> getTime() {
        return time;
    }

    public void setTime(ArrayList<RunTime> time) {
        this.time = time;
    }

    private ArrayList <RunTime> time = new ArrayList<>();

    public ArrayList<DataPoint> getDataPoints() {
        return dataPoints;
    }

    public void setDataPoints(ArrayList<DataPoint> dataPoints) {
        this.dataPoints = dataPoints;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    /**
     * @return Distance in meters
     */
    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getData());
    }

    public String getData() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static final Parcelable.Creator<OnlyOneRun> CREATOR
            = new Parcelable.Creator<OnlyOneRun>() {
        public OnlyOneRun createFromParcel(Parcel in) {
            Gson gson = new Gson();
            return gson.fromJson(in.readString(), OnlyOneRun.class);
        }

        public OnlyOneRun[] newArray(int size) {
            return new OnlyOneRun[size];
        }
    };

    public void loadDataPoints(Context context) {
        String data = getDataPointsFromDiskAsString(context);
        Gson gson = new Gson();
        Type collectionType = new TypeToken<Collection<DataPoint>>() {
        }.getType();
        Collection<DataPoint> dataPoints = gson.fromJson(data, collectionType);
        getDataPoints().clear();
        if(dataPoints != null) {
            getDataPoints().addAll(dataPoints);
        }
    }

    @NonNull
    public String getDataPointsFromDiskAsString(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(getDataPointFileName(), Context.MODE_PRIVATE);
        return preferences.getString("data", "[]");
    }


    public void storeDataPoints(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(getDataPointFileName(), Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String out = gson.toJson(getDataPoints());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("data", out);
        editor.commit();
    }

    public void removeDataPoints(Context context){
        SharedPreferences preferences = context.getSharedPreferences(getDataPointFileName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("data");
        editor.commit();
    }

    @NonNull
    public String getDataPointFileName() {
        return "point_data_" + "_" + getStartTime() + getStopTime();
    }
    public long caculateTotalRunTime (){
        long totalRunTime = 0;
        for (RunTime run : time){

                totalRunTime = totalRunTime + run.stoptime - run.startime;
        }
        return totalRunTime;
    }

    public Integer getTimeSegment(DataPoint dataPoint){
        int newDataPointTimeIndex = 0;
        boolean timeSegmentFound = false;
        for (RunTime timeSegment : getTime()){
            if(dataPoint.getTime()>= timeSegment.startime && dataPoint.getTime()<= timeSegment.stoptime){
                // found
                timeSegmentFound = true;
                break;
            }
            newDataPointTimeIndex++;
        }
        if(!timeSegmentFound){
            // ok, point seems inside of a pause. drop it.
            return null;
        }
        return newDataPointTimeIndex;
    }
}
