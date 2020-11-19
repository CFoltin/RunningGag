package eu.johannes.runninggag;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import de.codecrafters.tableview.SortableTableView;
import de.codecrafters.tableview.TableDataAdapter;
import de.codecrafters.tableview.listeners.TableDataClickListener;
import de.codecrafters.tableview.listeners.TableDataLongClickListener;
import de.codecrafters.tableview.model.TableColumnWeightModel;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;
import eu.johannes.runninggag.fitness22.Fitness22;
import eu.johannes.runninggag.fitness22.LocationPointsArray;


public class MainActivity extends AppCompatActivity {
    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static final int WRITE_REQUEST_CODE = 17;
    public static final String APPLICATION_JSON = "text/plain";
    public static final String APPLICATION_ZIP = "application/zip";
    private static final String TAG = "MainActivity";
    private static final String[] TABLE_HEADERS = {"Datum", "Dist.", "Zeit", "km"};
    private static final int PICKFILE_RESULT_CODE = 1;
    private RunningGagData runningGagData;
    private SortableTableView<OnlyOneRun> tableView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Vorher: ");
        setContentView(R.layout.activity_main);
        //createShareEvent();
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

        tableView = (SortableTableView<OnlyOneRun>) findViewById(R.id.runsTable);
        tableView.setColumnCount(TABLE_HEADERS.length);

        TableColumnWeightModel columnModel = new TableColumnWeightModel(TABLE_HEADERS.length);
        columnModel.setColumnWeight(0, 2);
        columnModel.setColumnWeight(2, 2);
        tableView.setColumnModel(columnModel);

        setRunlistAdapter();
        tableView.setColumnComparator(0, new Comparator<OnlyOneRun>() {
            @Override
            public int compare(OnlyOneRun o1, OnlyOneRun o2) {
                return -Long.compare(o1.getStartTime(), o2.getStartTime());
            }
        });
        tableView.setColumnComparator(1, new Comparator<OnlyOneRun>() {
            @Override
            public int compare(OnlyOneRun o1, OnlyOneRun o2) {
                return -Double.compare(o1.getDistance(), o2.getDistance());
            }
        });
        tableView.setColumnComparator(2, new Comparator<OnlyOneRun>() {
            @Override
            public int compare(OnlyOneRun o1, OnlyOneRun o2) {
                return -Long.compare(o1.caculateTotalRunTime(), o2.caculateTotalRunTime());
            }
        });
        tableView.setColumnComparator(3, new Comparator<OnlyOneRun>() {
            @Override
            public int compare(OnlyOneRun o1, OnlyOneRun o2) {
                if (o1.getCategory() != o2.getCategory()) {
                    return -Integer.compare(o1.getCategory(), o2.getCategory());
                }
                return -Long.compare(o1.caculateTotalRunTime(), o2.caculateTotalRunTime());
            }
        });

        SimpleTableHeaderAdapter simpleTableHeaderAdapter = new SimpleTableHeaderAdapter(this, TABLE_HEADERS);
        simpleTableHeaderAdapter.setTextColor(getResources().getColor(R.color.tableForeground));
        simpleTableHeaderAdapter.setPaddingLeft(0);
        tableView.setHeaderAdapter(simpleTableHeaderAdapter);
        tableView.setHeaderBackgroundColor(getResources().getColor(R.color.transparent));
        tableView.addDataClickListener(new TableDataClickListener<OnlyOneRun>() {
            @Override
            public void onDataClicked(int rowIndex, OnlyOneRun run) {
                // ListView Clicked item value
                Intent intent = new Intent(MainActivity.this, RunResult.class);
                intent.putExtra("com.example.runs.run", run);
                startActivity(intent);
            }
        });
        tableView.addDataLongClickListener(new TableDataLongClickListener<OnlyOneRun>() {
            @Override
            public boolean onDataLongClicked(int rowIndex, final OnlyOneRun runToBeDeleted) {
                // ListView Clicked item value
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                runToBeDeleted.removeDataPoints(MainActivity.this);
                                runningGagData.getRuns().remove(runToBeDeleted);
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

        DecimalFormat f = new DecimalFormat("#0.00");
        TextView gesamtgelaufen = findViewById(R.id.gesamtgelaufen);
        gesamtgelaufen.setText(f.format(runningGagData.caculateTotalRunDistance()) + "km");
        TextView yeargelaufen = findViewById(R.id.diesesjahrgelaufen);
        yeargelaufen.setText(f.format(runningGagData.caculateYearRunDistance()) + "km");
    }

    private void setRunlistAdapter() {
        ArrayList<OnlyOneRun> runs = new ArrayList<>();
        runs.addAll(runningGagData.getRuns());
        Collections.reverse(runs);
        tableView.setDataAdapter(new RunTableDataAdapter(this, runs));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu resource file.
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_backup);

        // Fetch and store ShareActionProvider
/*        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        mShareActionProvider.setShareIntent(shareIntent);*/

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_import:
                callImportAction();
                break;
            case R.id.menu_item_backup:
                callBackupAction();
                break;
            case R.id.menu_item_backup_disk:
                callBackupToDiskAction();
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

    private void callBackupAction() {
        File jsonFile = null;
        try {
            jsonFile = getBackupFileContent();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Exception: " + e.getLocalizedMessage(), Toast.LENGTH_LONG);
        }
        Intent backupIntent = new Intent();
        backupIntent.setAction(Intent.ACTION_SEND);
        backupIntent.setType(APPLICATION_ZIP);
        if (backupIntent.resolveActivity(getPackageManager()) != null) {
            Uri backupUri = getUriForFile(jsonFile);
            backupIntent.putExtra(Intent.EXTRA_STREAM, backupUri);
            backupIntent.setData(backupUri);
            backupIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Log.d(TAG, "Intent:  " + backupIntent + " URI: " + backupUri);
        }
        startActivity(Intent.createChooser(backupIntent, "Wohin mit dem Backup File?"));
    }

    private void callBackupToDiskAction() {
        // when you create document, you need to add Intent.ACTION_CREATE_DOCUMENT
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

        // filter to only show openable items.
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Create a file with the requested Mime type
        intent.setType(APPLICATION_ZIP);
        intent.putExtra(Intent.EXTRA_TITLE, "RunningGagBackup_" + (new Date().toString()) + ".zip");

        startActivityForResult(intent, WRITE_REQUEST_CODE);
    }

    private File getBackupFileContent() throws IOException {
        File temp = File.createTempFile("RunningGagBackup_", ".json", getCacheDir());
        temp.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(temp);
        writeBackupToOutputStream(fos);
        return temp;
    }

    private void writeBackupToOutputStream(OutputStream fos) throws IOException {
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        Set<String> fileNames = new HashSet<>();
        for (OnlyOneRun run : runningGagData.getRuns()) {
            String fileName = run.getDataPointFileName() + ".json";
            // to prevent doubled files.
            if (fileNames.add(fileName)) {
                ZipEntry zipEntry = new ZipEntry(fileName);
                zipOut.putNextEntry(zipEntry);
                zipOut.write(run.getDataPointsFromDiskAsString(this).getBytes());
                zipOut.closeEntry();
            }
        }
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        ZipEntry zipEntry = new ZipEntry("application_data.json");
        zipOut.putNextEntry(zipEntry);
        zipOut.write(gson.toJson(runningGagData).getBytes());
        zipOut.closeEntry();
        zipOut.flush();
        zipOut.close();
        // not necessary to close fos.
    }

    @Nullable
    private Uri getTemporaryUriForFile(String gpxFile) {
        Uri backupURI = null;
        try {
            File temp = File.createTempFile("RunningGagBackup_", ".json", getCacheDir());
            temp.deleteOnExit();
            FileWriter writer = new FileWriter(temp);
            writer.write(gpxFile);
            writer.close();
            backupURI = getUriForFile(temp);

        } catch (IOException ex) {
            Log.e(TAG, ex.getMessage());
        }
        return backupURI;
    }

    private Uri getUriForFile(File temp) {
        Uri backupURI;
        backupURI = FileProvider.getUriForFile(this,
                getString(R.string.file_provider_authority),
                temp);
        return backupURI;
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
                        Type collectionType = new TypeToken<Collection<Fitness22>>() {
                        }.getType();
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
            case WRITE_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    if (data != null
                            && data.getData() != null) {
                        try {
                            OutputStream outputStream = getContentResolver().openOutputStream(data.getData());
                            writeBackupToOutputStream(outputStream);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                    break;
                }
        }
    }

    @Override
    public void onBackPressed() {
        // do nothing here to prevent back navigation.
        // see https://stackoverflow.com/questions/4779954/disable-back-button-in-android
    }

    public class RunTableDataAdapter extends TableDataAdapter<OnlyOneRun> {
        DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        DecimalFormat f = new DecimalFormat("#0.00");

        public RunTableDataAdapter(Context context, List<OnlyOneRun> data) {
            super(context, data);
        }

        @Override
        public View getCellView(int rowIndex, int columnIndex, ViewGroup parentView) {
            OnlyOneRun run = getRowData(rowIndex);
            TextView textView = (TextView) View.inflate(MainActivity.this, R.layout.list_yellow_textview, null);
            textView.setPadding(textView.getPaddingLeft(), 10, textView.getPaddingRight(), 10);
            if (runningGagData.getRuns().indexOf(run) == runningGagData.getRuns().size() - 1) {
                textView.setTextColor(getResources().getColor(R.color.colorGreen));
            }
            String content = "Hae??";
            switch (columnIndex) {
                case 0:
                    content = df.format(new Date(run.getStartTime()));
                    break;
                case 1:
                    textView.setGravity(Gravity.RIGHT);
                    content = f.format(run.getDistance() / 1000d) + " km ";
                    break;
                case 2:
                    content = Runnow.getDurationString(run.caculateTotalRunTime() / 1000l);
                    break;
                case 3:
                    textView.setGravity(Gravity.RIGHT);
                    content = "" + run.getCategory() + " km";
                    break;
            }
            textView.setText(content);
            return textView;
        }
    }

}
