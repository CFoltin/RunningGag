package eu.johannes.runninggag;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import eu.johannes.runninggag.fitness22.Fitness22;
import eu.johannes.runninggag.fitness22.LocationPointsArray;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ListView runlist;
    private static final int PICKFILE_RESULT_CODE = 1;
    private RunningGagData runningGagData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Vorher: ");
        setContentView(R.layout.activity_main);
        Toolbar title = findViewById(R.id.toolbar);
        setSupportActionBar(title);
        runningGagData = RunningGagData.loadData(this);

        final Button setService = findViewById(R.id.startService);
        setService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(MainActivity.this, Runnow.class));
            }
        });
        Log.d(TAG, "Nachher: ");
        runlist = (ListView) findViewById(R.id.runs);

        setRunlistAdapter();

        // ListView Item Click Listener
        runlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // ListView Clicked item value
                String itemValue = (String) runlist.getItemAtPosition(position);

                OnlyOneRun run = runningGagData.getRuns().get(runningGagData.getRuns().size() - position - 1);
                Intent intent = new Intent(MainActivity.this, RunResult.class);
                intent.putExtra("com.example.runs.run", run);
                startActivity(intent);
            }
        });
        runlist.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // ListView Clicked item value
                String itemValue = (String) runlist.getItemAtPosition(position);
                final int runIndex = runningGagData.getRuns().size() - position - 1;
                final OnlyOneRun runToBeDeleted = runningGagData.getRuns().get(runIndex);
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                runToBeDeleted.removeDataPoints(MainActivity.this);
                                runningGagData.getRuns().remove(runIndex);
                                runningGagData.storeData(MainActivity.this);
                                setRunlistAdapter();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Are you sure to delete the run from " + new Date(runToBeDeleted.getStartTime()) + "?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();

                return true;
            }
        });
    }



    private void setRunlistAdapter() {
        ArrayList<String> values = new ArrayList<>();
        for (OnlyOneRun run : runningGagData.getRuns()) {
            String theRun = "Lauf vom " + new Date(run.getStartTime()) + "  Gelaufen: " + run.getDistance();

            values.add(0, theRun);


        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.list_yellow_text, R.id.list_content, values);

        runlist.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu resource file.
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_import:
                callImportAction();
                break;
        }
        return true;
    }

    private void callImportAction() {
        verifyStoragePermissions(this);
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("file/*");
        startActivityForResult(intent, PICKFILE_RESULT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PICKFILE_RESULT_CODE:
                if (resultCode == RESULT_OK) {
                    Uri fileUri = data.getData();
                    // start import action:
                    try {
                        Log.d(TAG, "Importing data...");
                        Gson gson = new Gson();
                        InputStream inputStream = getContentResolver().openInputStream(fileUri);
                        Type collectionType = new TypeToken<Collection<Fitness22>>(){}.getType();
                        Collection<Fitness22> result = gson.fromJson(new InputStreamReader(inputStream), collectionType);
                        inputStream.close();
                        Log.d(TAG, "Importing data. Done");
                        RUNLOOP:
                        for (Fitness22 run : result) {
                            OnlyOneRun newRun = new OnlyOneRun();
                            boolean isNew = true;
                            for (OnlyOneRun ownRun : runningGagData.getRuns()) {
                                if (ownRun.getStartTime() == run.getStartDate()) {
                                    // update instead of new:
                                    newRun = ownRun;
                                    isNew = false;
                                }
                            }
                            // run is new. Let's import it.
                            newRun.setStartTime(run.getStartDate());
                            newRun.setPoints(run.getLocationPointsArray().size());
                            newRun.setDistance(run.getDistanceMeters());
                            newRun.setStopTime(run.getStartDate() + run.getWorkoutDuration());
                            newRun.getDataPoints().clear();
                            for (LocationPointsArray point : run.getLocationPointsArray()) {
                                DataPoint dp = new DataPoint();
                                dp.setAccuracy(point.getMAccuracy());
                                dp.setLatitude(point.getMLatitude());
                                dp.setLongitude(point.getMLongitude());
                                dp.setProvider(point.getProvider());
                                dp.setSpeed(point.getMSpeed());
                                dp.setTime(point.getMTime());
                                dp.setAltitude(point.getMAltitude());
                                newRun.getDataPoints().add(dp);
                            }
                            newRun.storeDataPoints(this);
                            if (isNew) {
                                runningGagData.getRuns().add(newRun);
                                Log.d(TAG, "Created new run: " + newRun);
                            } else {
                                Log.d(TAG, "Updated existing run");
                            }
                        }
                        Log.d(TAG, "Sorting...");
                        // final sort:
                        Collections.sort(runningGagData.getRuns(), new Comparator<OnlyOneRun>() {
                            @Override
                            public int compare(OnlyOneRun o1, OnlyOneRun o2) {
                                return (int) Math.signum(o1.getStartTime() - o2.getStartTime());
                            }
                        });
                        Log.d(TAG, "Storing data....");
                        runningGagData.storeData(this);
                        Log.d(TAG, "Recreating list...");
                        setRunlistAdapter();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(TAG, e.getLocalizedMessage());
                    }
                }
                break;

        }
    }

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * Checks if the app has permission to write to device storage
     * <p>
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
}
