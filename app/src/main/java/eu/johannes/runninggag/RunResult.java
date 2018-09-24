package eu.johannes.runninggag;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by johannes on 27.08.18.
 */

public class RunResult extends AppCompatActivity {

    MapView map = null;
    @Override public void onCreate(Bundle savedInstanceState) {
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
        OnlyOneRun run = bundle.getParcelable("com.example.runs.run");


        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);


        List<GeoPoint> geoPoints = new ArrayList<>();

        for(DataPoint dataPoint : run.getDataPoints()){

            Log.d("RunResult", String.valueOf(dataPoint));
            GeoPoint geo = new GeoPoint(dataPoint.getLatitude(),dataPoint.getLongitude());
            geoPoints.add(geo);

        }
        IMapController mapController = map.getController();
        mapController.setZoom(15);
        if (run.getDataPoints().isEmpty()){}

        else {
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

        TextView minprokm = findViewById(R.id.MinproKM);
        double distance = run.getDistance();
        long startime = run.getStartTime();
        long stoptime = run.getStopTime();
        long time = stoptime - startime;
        distance = distance / 1000d;
        time = time / 60000l;
        if (distance != 0 && time != 0) {
            double mindurchkm = ((double)time) / distance;
            minprokm.setText("Durchschnittszeit:" + mindurchkm);

        }
        else {
            minprokm.setText("Du faule Sau");
        }

        TextView Distance = findViewById(R.id.Distance);
        Distance.setText("Distance: "+ distance + "km");
        TextView Laufzeit = findViewById(R.id.time);
        Laufzeit.setText("Zeit: " + time + "min");
        TextView Points = findViewById(R.id.points);
        int punkte = run.getPoints();
        Points.setText("Punkte " + punkte);
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


}
