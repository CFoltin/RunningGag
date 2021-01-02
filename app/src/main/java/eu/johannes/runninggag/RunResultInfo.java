package eu.johannes.runninggag;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import org.osmdroid.config.Configuration;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by johannes on 27.08.18.
 */

public class RunResultInfo extends Fragment {
    private static final String TAG = RunResultInfo.class.getName();

    private OnlyOneRunViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //handle permissions first, before map is created. not depicted here

        //load/initialize the osmdroid configuration, this can be done
        Context ctx = getActivity();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        View v = inflater.inflate(R.layout.runresultinfo, container, false);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(OnlyOneRunViewModel.class);
        displayRunInformation(v);
    }

    public void displayRunInformation(View v) {
        TextView minprokm = v.findViewById(R.id.MinproKM);
        double distance = getRun().getDistance();
        long time = getRun().caculateTotalRunTime();
        distance = distance / 1000d;

        time = time / 1000l;
        if (distance != 0 && time != 0) {
            double secprokm = ((double) time) / distance;
            minprokm.setText(getString(R.string.runresult_mean_time, Runnow.getDurationString((long) secprokm)));
        } else {
            minprokm.setText(getString(R.string.runresult_no_result));
        }
        DecimalFormat f = new DecimalFormat("#0.00");
        if (getRun().getPoints() > 0 && !getRun().getTime().isEmpty()) {
            // at the beginning, the last point is the first.
            DataPoint lastDataPoint = getRun().getDataPoints().get(getRun().getDataPoints().size() - 1);
            double distanceSinceLastKilometer = 0;
            double totalDistance = 0;
            int timeIndex = 0;
            long lasttime = getRun().getTime().get(timeIndex).startime;
            long startTime = lasttime;
            DataPoint lastPoint = getRun().getDataPoints().get(0);
            long accumulatedTime = 0;
            long accumulatedRoundTime = 0;
            boolean pauseOccurredInThisRound = false;
            String ausgabe = getString(R.string.runresult_lap_results) + "\n";
            for (DataPoint dataPoint : getRun().getDataPoints()) {
                // determine segment, where this point is located in:
                Integer newDataPointTimeIndex = getRun().getTimeSegment(dataPoint);
                if (newDataPointTimeIndex == null) {
                    // ok, point seems inside of a pause. drop it.
                    continue;
                }
                if (newDataPointTimeIndex != timeIndex) {
                    // new index. keep it, but don't calculate:
                    timeIndex = newDataPointTimeIndex;
                    lastPoint = dataPoint;
                    lasttime = dataPoint.getTime();
                    startTime = lasttime;
                    pauseOccurredInThisRound = true;
                    continue;
                }
                // ok, same index. Continue to calculate.
                // time has advanced:
                accumulatedTime += dataPoint.getTime() - lasttime;
                accumulatedRoundTime += dataPoint.getTime() - lasttime;
                // distance has advanced, too.
                double distanceToLastPoint = getLocation(dataPoint).distanceTo(getLocation(lastPoint));
                distanceSinceLastKilometer = distanceSinceLastKilometer + distanceToLastPoint;
                totalDistance += distanceToLastPoint;
                if (distanceSinceLastKilometer > 1000 || dataPoint == lastDataPoint) {
                    long roundtime = (long) (accumulatedRoundTime / distanceSinceLastKilometer);
                    long totalRoundtime = (long) (accumulatedTime / totalDistance);
                    ausgabe = getString(R.string.runresult_lap_time_contents,
                            ausgabe,
                            f.format(totalDistance / 1000d),
                            Runnow.getDurationString(roundtime),
                            Runnow.getDurationString(totalRoundtime),
                            (pauseOccurredInThisRound) ? " P" : "");
                    distanceSinceLastKilometer = totalDistance % 1000;
                    // reset the round time.
                    accumulatedRoundTime = 0;
                    pauseOccurredInThisRound = false;
                }
                lasttime = dataPoint.getTime();
                lastPoint = dataPoint;
            }
            TextView roundtimefinal = v.findViewById(R.id.rundenzeiten);
            roundtimefinal.setText(ausgabe);
        }
        TextView Distance = v.findViewById(R.id.Distance);
        Distance.setText(getString(R.string.runnow_distance, f.format(distance)));
        TextView Laufzeit = v.findViewById(R.id.time);
        Laufzeit.setText(getString(R.string.runnow_time, Runnow.getDurationString(time)));
        TextView Points = v.findViewById(R.id.points);
        int punkte = getRun().getPoints();
        Points.setText(getString(R.string.runnow_points, punkte));
        TextView dateOfRun = v.findViewById(R.id.Date);
        DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        dateOfRun.setText(df.format(new Date(getRun().getStartTime())));
    }

    public OnlyOneRun getRun() {
        return viewModel.getSelectedOnlyOneRun().getValue();
    }

    @NonNull
    private Location getLocation(DataPoint dataPoint) {
        Location lLast;
        lLast = new Location("test");
        lLast.setLatitude(dataPoint.getLatitude());
        lLast.setLongitude(dataPoint.getLongitude());
        return lLast;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_flatten:
                callFlattenAction();
                break;
        }
        return true;
    }

    private void callFlattenAction() {
        getRun().flattenDataPoints();
        displayRunInformation(getView());
    }
}
