package eu.johannes.runninggag;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
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
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
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

        map = (MapView) findViewById(R.id.mymap);
        map.setTileSource(TileSourceFactory.MAPNIK);

        Bundle bundle = getIntent().getExtras();
        run = bundle.getParcelable("com.example.runs.run");


        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        List<GeoPoint> geoPoints = new ArrayList<>();
        for (DataPoint dataPoint : run.getDataPoints()) {

            //Log.d("RunResult", String.valueOf(dataPoint));
            GeoPoint geo = new GeoPoint(dataPoint.getLatitude(), dataPoint.getLongitude());
            geoPoints.add(geo);
        }

        IMapController mapController = map.getController();
        mapController.setZoom(15);
        DataPoint firstPoint = run.getDataPoints().get(0);
        GeoPoint startPoint = new GeoPoint(firstPoint.getLatitude(), firstPoint.getLongitude());
        mapController.setCenter(startPoint);
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
    }

    public void onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    public void onPause() {
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
}
