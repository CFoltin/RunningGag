package eu.johannes.runninggag;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.core.content.FileProvider;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MapResult extends Fragment {

    private static final String TAG = MapResult.class.getName();
    public static final String APPLICATION_GPX_XML = "application/gpx+xml";
    MapView map = null;
    private Intent shareIntent;
    private ShareActionProvider mShareActionProvider;
    private OnlyOneRunViewModel viewModel;
    private static boolean isInitialized = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //handle permissions first, before map is created. not depicted here

        //load/initialize the osmdroid configuration, this can be done
        if (!isInitialized) {
            Context ctx = getActivity();
            Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
            //setting this before the layout is inflated is a good idea
            //it 'should' ensure that the map has a writable location for the map cache, even without permissions
            //if no tiles are displayed, you can try overriding the cache path using Configuration.getInstance().setCachePath
            //see also StorageUtils
            //note, the load method also sets the HTTP User Agent to your application's package name, abusing osm's tile servers will get you banned based on this string
            isInitialized = true;
        }
        View v = inflater.inflate(R.layout.mapresult, container, false);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(OnlyOneRunViewModel.class);
        createShareAction();
        //inflate and create the map
        displayMapWithPoints(v);

    }


    public OnlyOneRun getRun() {
        return viewModel.getSelectedOnlyOneRun().getValue();
    }

    public void displayMapWithPoints(View v) {
        map = (MapView) v.findViewById(R.id.mymap);
        map.setTileSource(TileSourceFactory.MAPNIK);


        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        map.getOverlayManager().clear();

        List<GeoPoint> geoPoints = new ArrayList<>();
        // load points
        ArrayList<Polyline> lines = new ArrayList<>();
        int timeSegmentOfLastDataPoint = 0;
        for (DataPoint dataPoint : getRun().getDataPoints()) {
            // determine time segment of this point:
            Integer newDataPointTimeIndex = getRun().getTimeSegment(dataPoint);
            if (newDataPointTimeIndex == null) {
                continue;
            }
            if (newDataPointTimeIndex != timeSegmentOfLastDataPoint && !geoPoints.isEmpty()) {
                Polyline line = new Polyline();
                line.setPoints(geoPoints);
                lines.add(line);
                geoPoints.clear();
            }
            timeSegmentOfLastDataPoint = newDataPointTimeIndex;
            //Log.d("RunResult", String.valueOf(dataPoint));
            GeoPoint geo = new GeoPoint(dataPoint.getLatitude(), dataPoint.getLongitude());
            geoPoints.add(geo);
        }
        if (!geoPoints.isEmpty()) {
            Polyline line = new Polyline();
            line.setPoints(geoPoints);
            lines.add(line);
        }
        IMapController mapController = map.getController();
        mapController.setZoom(15);
        if (!getRun().getDataPoints().isEmpty()) {
            DataPoint firstPoint = getRun().getDataPoints().get(0);
            GeoPoint startPoint = new GeoPoint(firstPoint.getLatitude(), firstPoint.getLongitude());
            mapController.setCenter(startPoint);
        }
        for (Polyline line : lines) {
            line.setOnClickListener(new Polyline.OnClickListener() {
                @Override
                public boolean onClick(Polyline polyline, MapView mapView, GeoPoint eventPos) {
                    Toast.makeText(mapView.getContext(), getString(R.string.runresult_polyline_tapped, polyline.getPoints().size()), Toast.LENGTH_LONG).show();
                    return false;
                }
            });
            map.getOverlayManager().add(line);
        }
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        mShareActionProvider.setShareIntent(shareIntent);
        super.onCreateOptionsMenu(menu, inflater);
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
        if (shareIntent.resolveActivity(getActivity().getPackageManager()) != null) {
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
        if (gpxIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            Uri gpxURI = getTemporaryUriForFile(gpxFile);
            gpxIntent.setData(gpxURI);
            gpxIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            shareIntent.putExtra(Intent.EXTRA_STREAM, gpxURI);
            Log.d(TAG, "Intent:  " + gpxIntent + " URI: " + gpxURI);
        }
        startActivity(Intent.createChooser(gpxIntent, getString(R.string.runresult_file_location)));
    }

    private void callPngAction() {
        //https://www.logicchip.com/share-image-without-saving/
        Bitmap bitmap = getBitmapFromView(map);
        try {
            File file = new File(this.getActivity().getExternalCacheDir(), "map_image.png");
            FileOutputStream fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
            file.setReadable(true, false);
            final Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            intent.setType("image/png");
            startActivity(Intent.createChooser(intent, getString(R.string.runresult_share_image)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Nullable
    private Uri getTemporaryUriForFile(String gpxFile) {
        Uri gpxURI = null;
        try {
            File temp = File.createTempFile("RunningGag_", ".gpx", getActivity().getCacheDir());
            temp.deleteOnExit();
            FileWriter writer = new FileWriter(temp);
            writer.write(gpxFile);
            writer.close();
            gpxURI = FileProvider.getUriForFile(getActivity(),
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
        for (DataPoint dataPoint : getRun().getDataPoints()) {
            gpxFile += "<trkpt lat=\"" + dataPoint.getLatitude() + "\" lon=\"" + dataPoint.getLongitude() + "\" ><time>" + df.format(new Date(dataPoint.getTime())) + "</time> </trkpt>";
        }
        gpxFile += "</trkseg></trk></gpx>";
        return gpxFile;
    }

    private Bitmap getBitmapFromView(View view) {
        view.setDrawingCacheEnabled(true);
        return view.getDrawingCache();

//        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(returnedBitmap);
//
//        Drawable bgDrawable =view.getBackground();
//        if (bgDrawable!=null) {
//            //has background drawable, then draw it on the canvas
//            bgDrawable.draw(canvas);
//        }   else{
//            //does not have background drawable, then draw white background on the canvas
//            canvas.drawColor(Color.WHITE);
//        }
//        view.draw(canvas);
//        return returnedBitmap;
    }

}
