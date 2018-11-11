package eu.johannes.runninggag;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class Runnow extends AppCompatActivity implements MyService.Callback {
    public static final int SWITCH_UPDATE_INTERVAL = 1000;
    private Intent serviceIntent;
    private Runnable runnable;
    private Handler handler;
    private long startTime;
    private long time;
    private double loldistance;
    private int lolpoints;
    private ArrayList<DataPoint> loldatapoints;
    private double kmdistance;
    private long kmtime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.runnow);
        serviceIntent = new Intent(this, MyService.class);
        startService(serviceIntent);
        bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
        startTime = System.currentTimeMillis();
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                time = System.currentTimeMillis();
                TextView runtime = findViewById(R.id.time);
                runtime.setText("Time:" + getTimePassed(startTime, time));

                handler.postDelayed(this, SWITCH_UPDATE_INTERVAL);
            }
        };


        final Button setService = findViewById(R.id.stopService);
        setService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RunningGagData runningGagData = RunningGagData.loadData(Runnow.this);
                OnlyOneRun myRun = new OnlyOneRun();
                myRun.setDistance(loldistance);
                myRun.setPoints(lolpoints);
                myRun.setStartTime(startTime);
                myRun.setStopTime(System.currentTimeMillis());
                myRun.setDataPoints(loldatapoints);
                myRun.storeDataPoints(Runnow.this);
                runningGagData.getRuns().add(myRun);
                runningGagData.storeData(Runnow.this);

                Toast.makeText(Runnow.this, "Stop", Toast.LENGTH_SHORT).show();
                myService.unregisterClient();
                unbindService(mConnection);
                stopService(serviceIntent);
                startActivity(new Intent(Runnow.this, MainActivity.class));


            }
        });
        final Button pause = findViewById(R.id.pauseButton);
            pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pause.getText().equals("PAUSE")){

                    pause.setText("Resume");
                }

                else

                    pause.setText("PAUSE");

            }
        });
    }

    private MyService myService;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Toast.makeText(Runnow.this, "Start", Toast.LENGTH_SHORT).show();
            // We've binded to LocalService, cast the IBinder and get LocalService instance
            MyService.LocalBinder binder = (MyService.LocalBinder) service;
            myService = binder.getServiceInstance(); //Get instance of your service!
            myService.registerClient(Runnow.this); //Activity register in the service as client for callabcks!

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {


        }
    };

    @Override
    public void gpslocation(double speed, double distance, int points, float accuracy, ArrayList<DataPoint> dataPoints) {


        TextView data = findViewById(R.id.GpsDaten);
        data.setText("speed:" + speed);
        TextView distancelol = findViewById(R.id.distance);
        kmdistance = distance;
        if (kmdistance >= 1000);
        {
            kmtime = startTime;

        }
        distancelol.setText("Distance:"+distance);
        loldistance = distance;
        TextView accuracy1 = findViewById(R.id.accuracy);
        accuracy1.setText("accuracy:"+accuracy);
        TextView points1 = findViewById(R.id.points);
        points1.setText("points:"+points);
        lolpoints = points;
        loldatapoints = dataPoints;

    }

    public static String getTimePassed(Long lastStarted, long currentTime) {
        long seconds = (currentTime - lastStarted) / 1000;
        return getDurationString(seconds);
    }

    @NonNull
    public static String getDurationString(long seconds) {
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        long weeks = days / 7;
        days = days % 7;
        hours = hours % 24;
        minutes = minutes % 60;
        seconds = seconds % 60;
        String out = "" + minutes + "m " + seconds + "s";
        if (hours > 0 || days > 0 || weeks > 0) {
            out = "" + hours + "h " + out;
        }
        if (days > 0 || weeks > 0) {
            out = "" + days + "d " + out;
        }
        if (weeks > 0) {
            out = "" + weeks + "w " + out;
        }
        return out;
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(runnable);
        myService.unregisterClient();
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (myService != null) {
            myService.registerClient(Runnow.this);
        }
        handler.postDelayed(runnable, SWITCH_UPDATE_INTERVAL);
        super.onResume();
        if (myService != null ) {
            gpslocation(myService.getmLastLocation().getSpeed(), myService.getDistance(), myService.getPoints(), myService.getmLastLocation().getAccuracy(), myService.getDataPoints());
        }

    }
}
