package eu.johannes.runninggag;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

public class TabbedRunResult extends AppCompatActivity {
    private static final String TAG = TabbedRunResult.class.getName();
    ViewPager simpleViewPager;
    TabLayout tabLayout;
    private OnlyOneRunViewModel viewModel;
    private ArrayList<Fragment> fragments = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tabbed_runresult);
        viewModel = new ViewModelProvider(this).get(OnlyOneRunViewModel.class);
        viewModel.getSelectedOnlyOneRun().observe(this, onlyOneRun -> {
            // Perform an action with the latest onlyOneRun data
            Log.d(TAG, "onCreate: SelectedOnlyOneRun: " + onlyOneRun);
        });
        Bundle bundle = getIntent().getExtras();
        OnlyOneRun run;
        run = bundle.getParcelable("com.example.runs.run");
        run.loadDataPoints(this);
        viewModel.selectOnlyOneRun(run);

        simpleViewPager = (ViewPager) findViewById(R.id.simpleViewPager);
        tabLayout = (TabLayout) findViewById(R.id.simpleTabLayout);

        addTab(R.string.tabbed_run_result_tab_run, R.drawable.osm_ic_center_map, new RunResult());
        addTab(R.string.tabbed_run_result_tab_infos, R.drawable.marker_default, new RunResultInfo());
        addTab(R.string.tabbed_run_result_tab_categories, R.drawable.marker_default, new RunComparisonListFragment());
        addTab(R.string.tabbed_run_result_tab_speed_chart, R.drawable.direction_arrow, new SpeedChartFragment());
        addTab(R.string.tabbed_run_result_tab_map, R.drawable.osm_ic_center_map, new MapResult());

        PagerAdapter adapter = new PagerAdapter
                (getSupportFragmentManager(), fragments);
        simpleViewPager.setAdapter(adapter);
        simpleViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tabSelected) {
                simpleViewPager.setCurrentItem(tabSelected.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tabSelected) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tabSelected) {

            }
        });
    }

    private void addTab(int p, int p2, Fragment fragment) {
        TabLayout.Tab tab = tabLayout.newTab();
        tab.setText(p);
        tab.setIcon(p2);
        tabLayout.addTab(tab);
        fragments.add(fragment);
    }
}
