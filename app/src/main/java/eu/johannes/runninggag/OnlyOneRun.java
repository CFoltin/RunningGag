package eu.johannes.runninggag;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;

import eu.johannes.runninggag.fitness22.Fitness22;

public class OnlyOneRun implements Parcelable {
    private double distance;
    private int points;
    private long startTime;
    private long stopTime;
    private transient ArrayList<DataPoint> dataPoints = new ArrayList<>();

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
        SharedPreferences preferences = context.getSharedPreferences(getDataPointFileName(), Context.MODE_PRIVATE);
        String data = preferences.getString("data", "[]");
        Gson gson = new Gson();
        Type collectionType = new TypeToken<Collection<DataPoint>>() {
        }.getType();
        Collection<DataPoint> dataPoints = gson.fromJson(data, collectionType);
        getDataPoints().clear();
        getDataPoints().addAll(dataPoints);
    }


    public void storeDataPoints(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(getDataPointFileName(), Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String out = gson.toJson(getDataPoints());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("data", out);
        editor.commit();
    }

    @NonNull
    private String getDataPointFileName() {
        return "point_data_" + "_" + getStartTime() + getStopTime();
    }
}
