package eu.johannes.runninggag;

import android.content.Context;
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
import java.util.List;

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
        List<OnlyOneRun.TimePerKilometer> timesPerKilometer = getRun().getTimesPerKilometer();
        String ausgabe = getString(R.string.runresult_lap_results) + "\n";
        for (OnlyOneRun.TimePerKilometer timePerKilometer : timesPerKilometer) {
            ausgabe = getString(R.string.runresult_lap_time_contents,
                    ausgabe,
                    f.format(timePerKilometer.totalDistance / 1000d),
                    Runnow.getDurationString(timePerKilometer.roundtime),
                    Runnow.getDurationString(timePerKilometer.totalRoundtime),
                    (timePerKilometer.pauseOccurredInThisRound) ? " P" : "");
        }
        TextView roundtimefinal = v.findViewById(R.id.rundenzeiten);
        roundtimefinal.setText(ausgabe);
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
