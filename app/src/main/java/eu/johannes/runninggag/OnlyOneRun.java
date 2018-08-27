package eu.johannes.runninggag;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.ArrayList;

public class OnlyOneRun implements Parcelable{
        private double distance;
        private int points;
        private long startTime;
        private long stopTime;

        public ArrayList<DataPoint> getDataPoints() {
            return dataPoints;
        }

        public void setDataPoints(ArrayList<DataPoint> dataPoints) {
            this.dataPoints = dataPoints;
        }

        private ArrayList<DataPoint> dataPoints = new ArrayList<>();

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

}
