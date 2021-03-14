package eu.johannes.runninggag;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

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

    private ArrayList<RunTime> time = new ArrayList<>();

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

    public double getAverageInSecsPerKilometer() {
        double distance = getDistance();
        long time = caculateTotalRunTime();
        distance = distance / 1000d;

        time = time / 1000l;
        if (distance != 0 && time != 0) {
            double secprokm = ((double) time) / distance;
            return secprokm;
        }
        return -1f;
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
        if (dataPoints != null) {
            getDataPoints().addAll(dataPoints);
        }
    }

    public void flattenDataPoints() {
        KalmanLatLon kalmanLatLon = new KalmanLatLon(6);
        for (DataPoint dataPoint : dataPoints) {
            kalmanLatLon.Process(dataPoint.getLatitude(), dataPoint.getLongitude(), (float) dataPoint.getAccuracy(), dataPoint.getTime());
            dataPoint.setLatitude(kalmanLatLon.get_lat());
            dataPoint.setLongitude(kalmanLatLon.get_lng());
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

    public void removeDataPoints(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(getDataPointFileName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("data");
        editor.commit();
    }

    @NonNull
    public String getDataPointFileName() {
        return "point_data_" + "_" + getStartTime() + getStopTime();
    }

    public long caculateTotalRunTime() {
        long totalRunTime = 0;
        for (RunTime run : time) {

            totalRunTime = totalRunTime + run.stoptime - run.startime;
        }
        return totalRunTime;
    }

    public Integer getTimeSegment(DataPoint dataPoint) {
        int newDataPointTimeIndex = 0;
        boolean timeSegmentFound = false;
        for (RunTime timeSegment : getTime()) {
            if (dataPoint.getTime() >= timeSegment.startime && dataPoint.getTime() <= timeSegment.stoptime) {
                // found
                timeSegmentFound = true;
                break;
            }
            newDataPointTimeIndex++;
        }
        if (!timeSegmentFound) {
            // ok, point seems inside of a pause. drop it.
            return null;
        }
        return newDataPointTimeIndex;
    }

    /**
     * @return the amount of kilometers casted to int.
     */
    public int getCategory() {
        return (int) (getDistance() / 1000d);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OnlyOneRun that = (OnlyOneRun) o;
        return Double.compare(that.distance, distance) == 0 &&
                points == that.points &&
                startTime == that.startTime &&
                stopTime == that.stopTime &&
                time.equals(that.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(distance, points, startTime, stopTime, time);
    }


    public static class TimePerKilometer {
        public double distanceSinceLastKilometer;
        long roundtime;
        long totalRoundtime;
        boolean pauseOccurredInThisRound = false;
        double totalDistance = 0;
        long currentTime;
    }

    public List<TimePerKilometer> getTimesPerKilometer() {
        List<TimePerKilometer> ret = new ArrayList<>();
        if (this.getPoints() > 0 && !this.getTime().isEmpty()) {
            // at the beginning, the last point is the first.
            DataPoint lastDataPoint = this.getDataPoints().get(this.getDataPoints().size() - 1);
            double distanceSinceLastKilometer = 0;
            double totalDistance = 0;
            int timeIndex = 0;
            long lasttime = this.getTime().get(timeIndex).startime;
            long startTime = lasttime;
            DataPoint lastPoint = this.getDataPoints().get(0);
            long accumulatedTime = 0;
            long accumulatedRoundTime = 0;
            boolean pauseOccurredInThisRound = false;
            for (DataPoint dataPoint : this.getDataPoints()) {
                // determine segment, where this point is located in:
                Integer newDataPointTimeIndex = this.getTimeSegment(dataPoint);
                if (newDataPointTimeIndex == null) {
                    // ok, point seems inside of a pause. drop it.
                    continue;
                }
                if (newDataPointTimeIndex != timeIndex) {
                    // new index. keep it, but don't calculate:
                    timeIndex = newDataPointTimeIndex;
                    lastPoint = dataPoint;
                    lasttime = dataPoint.getTime();
                    startTime = lasttime;
                    pauseOccurredInThisRound = true;
                    continue;
                }
                // ok, same index. Continue to calculate.
                // time has advanced:
                accumulatedTime += dataPoint.getTime() - lasttime;
                accumulatedRoundTime += dataPoint.getTime() - lasttime;
                // distance has advanced, too.
                double distanceToLastPoint = getLocation(dataPoint).distanceTo(getLocation(lastPoint));
                distanceSinceLastKilometer = distanceSinceLastKilometer + distanceToLastPoint;
                totalDistance += distanceToLastPoint;
                if (distanceSinceLastKilometer > 1000 || dataPoint == lastDataPoint) {
                    long roundtime = (long) (accumulatedRoundTime / distanceSinceLastKilometer);
                    long totalRoundtime = (long) (accumulatedTime / totalDistance);
                    TimePerKilometer timePerKilometer = new TimePerKilometer();
                    timePerKilometer.pauseOccurredInThisRound = pauseOccurredInThisRound;
                    timePerKilometer.roundtime = roundtime;
                    timePerKilometer.totalRoundtime = totalRoundtime;
                    timePerKilometer.totalDistance = totalDistance;
                    timePerKilometer.currentTime = dataPoint.getTime();
                    timePerKilometer.distanceSinceLastKilometer = distanceSinceLastKilometer;
                    ret.add(timePerKilometer);
                    distanceSinceLastKilometer = totalDistance % 1000;
                    // reset the round time.
                    accumulatedRoundTime = 0;
                    pauseOccurredInThisRound = false;
                }
                lasttime = dataPoint.getTime();
                lastPoint = dataPoint;
            }
        }
        return ret;
    }

    @NonNull
    private Location getLocation(DataPoint dataPoint) {
        Location lLast;
        lLast = new Location("test");
        lLast.setLatitude(dataPoint.getLatitude());
        lLast.setLongitude(dataPoint.getLongitude());
        return lLast;
    }


}
