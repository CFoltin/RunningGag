package eu.johannes.runninggag;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class MyService extends Service
{
    private static final String TAG = "BOOMBOOMTESTGPS";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 4000;
    private static final float LOCATION_DISTANCE = 1f;
    private Callback activity;
    private final IBinder mBinder = new LocalBinder();
    private double distance;
    private int points;
    private boolean isPaused;
    private ArrayList <DataPoint> dataPoints = new ArrayList<>();
    Location mLastLocation = new Location("test");
    public void registerClient(Callback activity){
        this.activity = activity;
    }
    //private long startTimeInMS;
    private long alltime;

    public void unregisterClient() {
        this.activity = null ;
    }

    public double getDistance() {
        return distance;
    }

    public int getPoints() {
        return points;
    }

    public ArrayList<DataPoint> getDataPoints() {
        return dataPoints;
    }

    public Location getmLastLocation() {
        return mLastLocation;
    }

    public void setStartTimeInMS (long pStartRuntimeInMS){

        RunTime s = new RunTime();
        s.startime = pStartRuntimeInMS;
        time.add(s);
    }
    public void setPause() {
        RunTime p = time.get(time.size()-1);
        p.stoptime = System.currentTimeMillis();
        isPaused = true;



    }

    public void setResume() {
        RunTime s = new RunTime();
        s.startime = System.currentTimeMillis();
        time.add(s);
        isPaused = false;
    }

    public void setTheEnd() {
        setPause();
    }

    public interface Callback{
        public void gpslocation(double speed, double distance, int points, float accuracy, ArrayList<DataPoint> dataPoints);
    }

    public long caculateTotalRunTime (){
        long totalRunTime = 0;
        for (RunTime run : time){
            if (run.stoptime > 0) {
                totalRunTime = totalRunTime + run.stoptime - run.startime;
            }
            else {
                totalRunTime = totalRunTime + System.currentTimeMillis() - run.startime;
            }
        }
        return totalRunTime;
    }

    public ArrayList<RunTime> getTime() {
        return time;
    }

    private ArrayList <RunTime> time = new ArrayList<>();
    private class LocationListener implements android.location.LocationListener
    {

        public LocationListener(String provider)
        {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location)
        {
            if (isPaused){
                return;
            }
            Log.e(TAG, "onLocationChanged: " + location);
            points = points+1;
            DataPoint data = new DataPoint();
            data.setLatitude(location.getLatitude());
            data.setLongitude(location.getLongitude());
            data.setSpeed(location.getSpeed());
            data.setTime(location.getTime());
            data.setProvider(location.getProvider());
            data.setAccuracy(location.getAccuracy());
            data.setAltitude(location.getAltitude());
            alltime = System.currentTimeMillis();
            dataPoints .add(data);
            if (points>1&&mLastLocation.hasAccuracy()&&mLastLocation.getAccuracy()<500&&location.hasAccuracy()&&location.getAccuracy()<500) {
                float distanceInMeters = location.distanceTo(mLastLocation);
                distance = distance+distanceInMeters;
            }
            mLastLocation.set(location);
            if(activity!=null) {
                activity.gpslocation(location.getSpeed(), distance,points,location.getAccuracy(),dataPoints);
            }
            DecimalFormat f = new DecimalFormat("#0.00");
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), MyService.class.getName())
                    .setSmallIcon(R.drawable.ic_runnotification)
                    .setContentTitle("wie weit du gelaufen bist du lappen")
                    .setContentText("Time: " + Runnow.getDurationString(caculateTotalRunTime()/1000) + "  Distance:"+ f.format(distance/1000d) + "km  " + "Average: " + f.format(distance/caculateTotalRunTime()) + "km/h")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());

                 // notificationId is a unique int for each notification that you must define
                notificationManager.notify(15, mBuilder.build());
        }

        @Override
        public void onProviderDisabled(String provider)
        {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            // new LocationListener(LocationManager.NETWORK_PROVIDER)
    };


    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate()
    {
        Log.e(TAG, "onCreate");
        initializeLocationManager();
//        try {
//            mLocationManager.requestLocationUpdates(
//                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
//                    mLocationListeners[1]);
//        } catch (java.lang.SecurityException ex) {
//            Log.i(TAG, "fail to request location update, ignore", ex);
//        } catch (IllegalArgumentException ex) {
//            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
//        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }

    @Override
    public void onDestroy()
    {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }


     @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    //returns the instance of the service
    public class LocalBinder extends Binder {
        public MyService getServiceInstance(){
            return MyService.this;
        }
    }
}
