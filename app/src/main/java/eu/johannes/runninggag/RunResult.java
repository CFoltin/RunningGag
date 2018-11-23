package eu.johannes.runninggag;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by johannes on 27.08.18.
 */

public class RunResult extends AppCompatActivity {


    private static final String TAG = RunResult.class.getName();
    public static final String APPLICATION_GPX_XML = "application/gpx+xml";
    MapView map = null;
    private Intent shareIntent;
    private OnlyOneRun run;
    private ShareActionProvider mShareActionProvider;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //handle permissions first, before map is created. not depicted here

        //load/initialize the osmdroid configuration, this can be done
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        //setting this before the layout is inflated is a good idea
        //it 'should' ensure that the map has a writable location for the map cache, even without permissions
        //if no tiles are displayed, you can try overriding the cache path using Configuration.getInstance().setCachePath
        //see also StorageUtils
        //note, the load method also sets the HTTP User Agent to your application's package name, abusing osm's tile servers will get you banned based on this string

        //inflate and create the map
        setContentView(R.layout.runresult);
        Toolbar title = findViewById(R.id.toolbar);
        setSupportActionBar(title);

        map = (MapView) findViewById(R.id.mymap);
        map.setTileSource(TileSourceFactory.MAPNIK);

        Bundle bundle = getIntent().getExtras();
        run = bundle.getParcelable("com.example.runs.run");


        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);


        List<GeoPoint> geoPoints = new ArrayList<>();
        // load points
        run.loadDataPoints(this);
        for(DataPoint dataPoint : run.getDataPoints()){

            //Log.d("RunResult", String.valueOf(dataPoint));
            GeoPoint geo = new GeoPoint(dataPoint.getLatitude(), dataPoint.getLongitude());
            geoPoints.add(geo);

        }
        IMapController mapController = map.getController();
        mapController.setZoom(15);
        if(!run.getDataPoints().isEmpty()) {
            DataPoint firstPoint = run.getDataPoints().get(0);
            GeoPoint startPoint = new GeoPoint(firstPoint.getLatitude(), firstPoint.getLongitude());
            mapController.setCenter(startPoint);
        }
        Polyline line = new Polyline();   //see note below!
        line.setPoints(geoPoints);
        line.setOnClickListener(new Polyline.OnClickListener() {
            @Override
            public boolean onClick(Polyline polyline, MapView mapView, GeoPoint eventPos) {
                Toast.makeText(mapView.getContext(), "polyline with " + polyline.getPoints().size() + "pts was tapped", Toast.LENGTH_LONG).show();
                return false;
            }
        });
        map.getOverlayManager().add(line);
        createShareAction();

        TextView minprokm = findViewById(R.id.MinproKM);
        double distance = run.getDistance();
        long startime = run.getStartTime();
        long stoptime = run.getStopTime();
        long time = stoptime - startime;
        distance = distance / 1000d;

        time = time / 1000l;
        if (distance != 0 && time != 0) {
            double secprokm = ((double)time) / distance;
            minprokm.setText("Durchschnittszeit: " + Runnow.getDurationString((long)secprokm));

        }
        else {
            minprokm.setText("Du faule Sau");
        }
        DecimalFormat f = new DecimalFormat("#0.00");
        if (run.getPoints() > 0) {
            double distance2 = 0;
            double totalDistance = 0;
            long lasttime = run.getStartTime();
            DataPoint lastPoint = run.getDataPoints().get(0);
            String ausgabe = "Rundenzeiten:\n";
            for (DataPoint dataPoint : run.getDataPoints()) {
                double distanceToLastPoint = getLocation(dataPoint).distanceTo(getLocation(lastPoint));
                distance2 = distance2 + distanceToLastPoint;
                totalDistance += distanceToLastPoint;
                if (distance2 > 1000) {
                    long roundtime = (long) ((dataPoint.getTime() - lasttime)/distance2);
                    long totalRoundtime = (long) ((dataPoint.getTime() - run.getStartTime()) / totalDistance);
                    ausgabe = ausgabe + "Dist.: " + f.format(totalDistance/1000d) + "km; "
                            + "Runde: " + Runnow.getDurationString(roundtime)
                            + "; "
                            + "Gesamt: " + Runnow.getDurationString(totalRoundtime) + "\n";
                    distance2 = totalDistance % 1000;
                    lasttime = dataPoint.getTime();
                }
                lastPoint = dataPoint;
            }
            long roundtime = (long) ((lastPoint.getTime() - lasttime)/distance2);
            long totalRoundtime = (long) ((lastPoint.getTime() - run.getStartTime()) / totalDistance);
            ausgabe = ausgabe + "Dist.: " + f.format(totalDistance/1000d) + "km; "
                    + "Runde: " + Runnow.getDurationString(roundtime)
                    + "; "
                    + "Gesamt: " + Runnow.getDurationString(totalRoundtime) + "\n";
            TextView roundtimefinal = findViewById(R.id.rundenzeiten);
            roundtimefinal.setText(ausgabe);
        }
        TextView Distance = findViewById(R.id.Distance);
        Distance.setText("Distanz: "+ f.format(distance) + "km");
        TextView Laufzeit = findViewById(R.id.time);
        Laufzeit.setText("Zeit: " + Runnow.getTimePassed(run.getStartTime(),run.getStopTime()));
        TextView Points = findViewById(R.id.points);
        int punkte = run.getPoints();
        Points.setText("Punkte: " + punkte);

    }

    @NonNull
    private Location getLocation(DataPoint dataPoint) {
        Location lLast;
        lLast = new Location("test");
        lLast.setLatitude(dataPoint.getLatitude());
        lLast.setLongitude(dataPoint.getLongitude());
        return lLast;
    }

    public void onResume(){
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    public void onPause(){
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu resource file.
        getMenuInflater().inflate(R.menu.run_result_menu, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        mShareActionProvider.setShareIntent(shareIntent);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_gpx:
                callGpxAction();
                break;
            case R.id.menu_item_image:
                callPngAction();
                break;
        }
        return true;
    }

    private void createShareAction() {
        // create gpx intent:
        String gpxFile = getGpxFileContent();
        shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType(APPLICATION_GPX_XML);
        if (shareIntent.resolveActivity(getPackageManager()) != null) {
            Uri gpxURI = getTemporaryUriForFile(gpxFile);
            shareIntent.putExtra(Intent.EXTRA_STREAM, gpxURI);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Log.d(TAG, "Intent:  " + shareIntent + " URI: " + gpxURI);
        }
    }

    private void callGpxAction() {
        // create gpx intent:
        String gpxFile = getGpxFileContent();
        Intent gpxIntent = new Intent();
        gpxIntent.setAction(Intent.ACTION_VIEW);
        gpxIntent.setType(APPLICATION_GPX_XML);
        if (gpxIntent.resolveActivity(getPackageManager()) != null) {
            Uri gpxURI = getTemporaryUriForFile(gpxFile);
            gpxIntent.setData(gpxURI);
            gpxIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            shareIntent.putExtra(Intent.EXTRA_STREAM, gpxURI);
            Log.d(TAG, "Intent:  " + gpxIntent + " URI: " + gpxURI);
        }
        startActivity(Intent.createChooser(gpxIntent, "Wohin mit dem GPX File?"));
    }

    private void callPngAction(){
        //https://www.logicchip.com/share-image-without-saving/
        Bitmap bitmap =getBitmapFromView(map);
        try {
            File file = new File(this.getExternalCacheDir(),"map_image.png");
            FileOutputStream fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
            file.setReadable(true, false);
            final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            intent.setType("image/png");
            startActivity(Intent.createChooser(intent, "Share image via"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Nullable
    private Uri getTemporaryUriForFile(String gpxFile) {
        Uri gpxURI = null;
        try {
            File temp = File.createTempFile("RunningGag_", ".gpx", getCacheDir());
            temp.deleteOnExit();
            FileWriter writer = new FileWriter(temp);
            writer.write(gpxFile);
            writer.close();
            gpxURI = FileProvider.getUriForFile(this,
                    getString(R.string.file_provider_authority),
                    temp);

        } catch (IOException ex) {
            Log.e(TAG, ex.getMessage());
        }
        return gpxURI;
    }

    @NonNull
    private String getGpxFileContent() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        String gpxFile = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<gpx xmlns=\"http://www.topografix.com/GPX/1/1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" version=\"1.1\" creator=\"RunningGag\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\"><trk><trkseg>";
        for (DataPoint dataPoint : run.getDataPoints()) {
            gpxFile += "<trkpt lat=\"" + dataPoint.getLatitude() + "\" lon=\"" + dataPoint.getLongitude() + "\" ><time>" + df.format(new Date(dataPoint.getTime())) + "</time> </trkpt>";
        }
        gpxFile += "</trkseg></trk></gpx>";
        return gpxFile;
    }

    private Bitmap getBitmapFromView(View view) {
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable =view.getBackground();
        if (bgDrawable!=null) {
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        }   else{
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);
        }
        view.draw(canvas);
        return returnedBitmap;
    }

}
