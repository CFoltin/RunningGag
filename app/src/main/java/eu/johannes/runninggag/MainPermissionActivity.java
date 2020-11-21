package eu.johannes.runninggag;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class MainPermissionActivity extends AppCompatActivity {
    // Location Permissions
    private static final int REQUEST_LOCATION = 2;
    private static String[] PERMISSIONS_LOCATION = {
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION
    };


    /**
     * Checks if the app has permission to access the GPS in background
     * <p>
     * If the app does not has permission then the user will be prompted to grant permissions
     */
    public void verifyLocationPermissions() {
        final RunningGagData runningGagData = RunningGagData.loadData(this);
        // Check if we have location permission
        int permission2 = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int permission3 = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        if (permission2 != PackageManager.PERMISSION_GRANTED || permission3 != PackageManager.PERMISSION_GRANTED) {
            if (!runningGagData.isDontAskForLocation()) {
                // first display an explanation to be inline with the requirements
                // ListView Clicked item value
                DialogInterface.OnClickListener approvalDialogListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                // We don't have permission so prompt the user
                                ActivityCompat.requestPermissions(
                                        MainPermissionActivity.this,
                                        PERMISSIONS_LOCATION,
                                        REQUEST_LOCATION
                                );
                                //Yes button clicked
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                moveToMainActivity();
                                break;
                            case DialogInterface.BUTTON_NEUTRAL:
                                //Don't ask again-button clicked
                                runningGagData.setDontAskForLocation(true);
                                runningGagData.storeData(MainPermissionActivity.this);
                                moveToMainActivity();
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(MainPermissionActivity.this);
                builder.setTitle(R.string.use_your_location)
                        .setMessage(R.string.disclaimer)
                        .setIcon(R.drawable.runmapsmall)
                        .setPositiveButton(R.string.turn_on, approvalDialogListener)
                        .setNegativeButton(R.string.no_thanks, approvalDialogListener)
                        .setNeutralButton(R.string.dont_ask_again, approvalDialogListener)
                        .show();

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        moveToMainActivity();
    }

    public void verifyBatteryOptimizationIsOff() {
        // see https://stackoverflow.com/a/54982071
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent();
            String packageName = getPackageName();
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                startActivity(intent);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_permission);
        verifyBatteryOptimizationIsOff();
        verifyLocationPermissions();
    }

    private void moveToMainActivity() {
        startActivity(new Intent(MainPermissionActivity.this, MainActivity.class));
    }

    @Override
    public void onBackPressed() {
        // do nothing here to prevent back navigation.
        // see https://stackoverflow.com/questions/4779954/disable-back-button-in-android
    }
}