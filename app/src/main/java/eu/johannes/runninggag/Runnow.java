package eu.johannes.runninggag;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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
    private boolean mRunStopped = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.runnow);
        serviceIntent = new Intent(this, MyService.class);
        startService(serviceIntent);
        bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
        mRunStopped = false;

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                time = System.currentTimeMillis();
                TextView runtime = findViewById(R.id.time);
                String durationString = getDurationString(myService.caculateTotalRunTime() / 1000);
                runtime.setText(getString(R.string.runnow_time, durationString));

                handler.postDelayed(this, SWITCH_UPDATE_INTERVAL);
            }
        };


        final Button stopService = findViewById(R.id.stopService);
        stopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mRunStopped){
                    return;
                }
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                mRunStopped = true;
                                myService.setTheEnd();
                                RunningGagData runningGagData = RunningGagData.loadData(Runnow.this);
                                OnlyOneRun myRun = new OnlyOneRun();
                                myRun.setDistance(loldistance);
                                myRun.setPoints(lolpoints);
                                myRun.setTime(myService.getTime());
                                // set start and stop time to store the points correctly.
                                myRun.setStartTime(myService.getTime().get(0).startime);
                                myRun.setStopTime(myService.getTime().get(myService.getTime().size()-1).stoptime);
                                myRun.setDataPoints(loldatapoints);
                                myRun.storeDataPoints(Runnow.this);
                                runningGagData.getRuns().add(myRun);
                                runningGagData.storeData(Runnow.this);

                                Toast.makeText(Runnow.this, getString(R.string.runnow_stop), Toast.LENGTH_SHORT).show();
                                myService.unregisterClient();
                                unbindService(mConnection);
                                stopService(serviceIntent);
                                startActivity(new Intent(Runnow.this, MainActivity.class));
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(Runnow.this);
                builder.setMessage(R.string.runnow_sure_to_end)
                        .setPositiveButton(R.string.yes, dialogClickListener)
                        .setNegativeButton(R.string.no, dialogClickListener).show();

            }
        });
        final Button pause = findViewById(R.id.pauseButton);
            pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pause.getText().equals(getText(R.string.pause))){
                    pause.setText(getText(R.string.resume));
                    myService.setPause();
                }
                else {
                    pause.setText(getString(R.string.pause));
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
            Toast.makeText(Runnow.this, getString(R.string.runnow_start), Toast.LENGTH_SHORT).show();
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
                              DecimalFormat f = new DecimalFormat("#0.00");
                              TextView data = findViewById(R.id.GpsDaten);
                              data.setText(getString(R.string.runnow_speed, f.format(speed)));
                              TextView distancelol = findViewById(R.id.distance);
                              distancelol.setText(getString(R.string.runnow_distance, f.format(distance / 1000d)));
                              loldistance = distance;
                              TextView accuracy1 = findViewById(R.id.accuracy);
                              accuracy1.setText(getString(R.string.runnow_accuracy, accuracy));
                              TextView points1 = findViewById(R.id.points);
                              points1.setText(getString(R.string.runnow_points, points));
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

    @Override
    public void onBackPressed() {
        // do nothing here to prevent back navigation.
        // see https://stackoverflow.com/questions/4779954/disable-back-button-in-android
    }
}
