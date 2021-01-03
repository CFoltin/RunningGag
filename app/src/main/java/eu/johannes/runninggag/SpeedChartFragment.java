package eu.johannes.runninggag;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

public class SpeedChartFragment extends Fragment {
    private OnlyOneRunViewModel viewModel;
    private final SimpleDateFormat mFormat = new SimpleDateFormat("HH:mm");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.speed_chart_fragment, container, false);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(OnlyOneRunViewModel.class);
        LineChart lineChart = v.findViewById(R.id.speedChart);
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.TOP_INSIDE);
//        xAxis.setTypeface(tfLight);
        xAxis.setTextSize(10f);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(true);
        xAxis.setTextColor(Color.rgb(255, 192, 56));
        xAxis.setCenterAxisLabels(true);
        xAxis.setGranularity(1f); // one hour
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return mFormat.format(new Date((long) value));
            }
        });

        setData(lineChart);
    }

    private void setData(LineChart lineChart) {
        ArrayList<Entry> values = new ArrayList<>();

        OnlyOneRun run = viewModel.getSelectedOnlyOneRun().getValue();
        for (RunTime time : run.getTime()) {
            long lastTime = 0;
            Location lastLocation = null;
            boolean isFirst = true;
            float totalDistance = 0f;
            for (DataPoint point : run.getDataPoints()) {
                long currentTime = point.getTime();
                if (!time.contains(currentTime)) {
                    continue;
                }
                if (isFirst) {
                    isFirst = false;
                    lastLocation = getLocation(point);
                    lastTime = currentTime;
                    totalDistance = 0f;
                    continue;
                }
                Location currentLocation = getLocation(point);
                totalDistance += currentLocation.distanceTo(lastLocation);
                lastLocation = currentLocation;
                if (currentTime - lastTime > 120000L) {
                    values.add(new Entry(currentTime, totalDistance * 60f * 60f / (currentTime - lastTime)));
                    isFirst = true;
                }
            }
        }
        ArrayList<Entry> meanValues = mean(values, 10);

        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(meanValues, "Speed");
        decorateDataSet(set1, ColorTemplate.getHoloBlue());

        // create a data object with the data sets
        LineData data = new LineData(set1);

        data.setValueTextColor(Color.WHITE);
        data.setValueTextSize(9f);

        // set data
        lineChart.setData(data);
    }

    private ArrayList<Entry> mean(ArrayList<Entry> values, int amount) {
        ArrayList<Entry> means = new ArrayList<>();
        LinkedList<Float> meanValues = new LinkedList<>();
        float meanSum = 0F;
        for (Entry val : values) {
            meanValues.addLast(val.getY());
            meanSum += val.getY();
            while (meanValues.size() > amount) {
                meanSum -= meanValues.removeFirst();
            }
            if (meanValues.size() == amount) {
                means.add(new Entry(val.getX(), meanSum / amount));
            }
        }
        return means;
    }

    private Location getLocation(DataPoint point) {
        Location location = new Location("test");
        location.setLatitude(point.getLatitude());
        location.setLongitude(point.getLongitude());
        return location;
    }

    private void decorateDataSet(LineDataSet set2, int materialColor) {
        set2.setAxisDependency(YAxis.AxisDependency.LEFT);
        set2.setColor(materialColor);
        set2.setValueTextColor(materialColor);
        set2.setLineWidth(1.5f);
        set2.setDrawCircles(false);
        set2.setDrawValues(false);
        set2.setFillAlpha(65);
        set2.setFillColor(materialColor);
        set2.setHighLightColor(Color.rgb(244, 117, 117));
        set2.setDrawCircleHole(false);
    }

}
