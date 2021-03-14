package eu.johannes.runninggag;

import android.graphics.Color;
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
import java.util.List;

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
        List<OnlyOneRun.TimePerKilometer> timesPerKilometer = run.getTimesPerKilometer();
        for (OnlyOneRun.TimePerKilometer timePerKilometer : timesPerKilometer) {
            double speed = timePerKilometer.distanceSinceLastKilometer / 1000f * 60f * 60f / (timePerKilometer.roundtime);
            values.add(new Entry(timePerKilometer.currentTime, (float) speed));
        }

        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(values, "Speed");
        decorateDataSet(set1, ColorTemplate.getHoloBlue());

        // create a data object with the data sets
        LineData data = new LineData(set1);

        data.setValueTextColor(Color.WHITE);
        data.setValueTextSize(9f);

        // set data
        lineChart.setData(data);
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
