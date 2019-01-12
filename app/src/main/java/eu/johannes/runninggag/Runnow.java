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

import java.text.DecimalFormat;
import java.util.ArrayList;


/*
Hier werden die werte angezeigt(in der App) bzw. der stopknopf

 */
public class Runnow extends AppCompatActivity implements MyService.Callback {
    public static final int SWITCH_UPDATE_INTERVAL = 1000;
    private Intent serviceIntent;
    private Runnable runnable;
    private Handler handler;
    //private long startTime;
    private long time;
    private double loldistance;
    private int lolpoints;
    private ArrayList<DataPoint> loldatapoints;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.runnow);
        serviceIntent = new Intent(this, MyService.class);
        startService(serviceIntent);
        bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                time = System.currentTimeMillis();
                TextView runtime = findViewById(R.id.time);
                runtime.setText("Time: " + getDurationString(myService.caculateTotalRunTime()/1000));

                handler.postDelayed(this, SWITCH_UPDATE_INTERVAL);
            }
        };


        final Button stopService = findViewById(R.id.stopService);
        stopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myService.setTheEnd();
                RunningGagData runningGagData = RunningGagData.loadData(Runnow.this);
                OnlyOneRun myRun = new OnlyOneRun();
                myRun.setDistance(loldistance);
                myRun.setPoints(lolpoints);
                myRun.setTime(myService.getTime());
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
                    myService.setPause();
                }

                else {
                    pause.setText("PAUSE");
                    myService.setResume();
                }
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
            myService.setStartTimeInMS(System.currentTimeMillis());
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {


        }
    };

    @Override
    public void gpslocation(final double speed, final double distance, final int points, final float accuracy, final ArrayList<DataPoint> dataPoints) {

        runOnUiThread(new Runnable() {
                          @Override
                          public void run() {
                              TextView data = findViewById(R.id.GpsDaten);
                              data.setText("Speed: " + speed);
                              TextView distancelol = findViewById(R.id.distance);
                              DecimalFormat f = new DecimalFormat("#0.00");
                              distancelol.setText("Distance: " + f.format(distance/1000d) + "km");
                              loldistance = distance;
                              TextView accuracy1 = findViewById(R.id.accuracy);
                              accuracy1.setText("Accuracy: " + accuracy);
                              TextView points1 = findViewById(R.id.points);
                              points1.setText("Points: " + points);
                              lolpoints = points;
                              loldatapoints = dataPoints;
                          }
                      });
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
        if (myService != null) {
            gpslocation(myService.getmLastLocation().getSpeed(), myService.getDistance(), myService.getPoints(), myService.getmLastLocation().getAccuracy(), myService.getDataPoints());
        }

    }
}
