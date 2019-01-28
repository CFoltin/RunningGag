package eu.johannes.runninggag;

import android.app.Application;
import android.content.Intent;

public class RunningGagApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Setup handler for uncaught exceptions.
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable e) {
                handleUncaughtException(thread, e);
            }
        });
    }

    public void handleUncaughtException(Thread thread, Throwable e) {
        e.printStackTrace(); // not all Android versions will print the stack trace automatically

        Intent intent = new Intent();
        intent.setAction("eu.johannes.runninggag.SEND_LOG"); // see step 5.
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // required when starting from Application
        startActivity(intent);

        System.exit(1); // kill off the crashed app
    }

}
