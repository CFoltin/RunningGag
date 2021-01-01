package eu.johannes.runninggag;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

public class TabbedRunResult extends AppCompatActivity {
    private static final String TAG = TabbedRunResult.class.getName();
    ViewPager simpleViewPager;
    TabLayout tabLayout;
    private OnlyOneRunViewModel viewModel;


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

        addTab(R.string.tabbed_run_result_tab_run, R.drawable.osm_ic_center_map);
        addTab(R.string.tabbed_run_result_tab_infos, R.drawable.marker_default);
        addTab(R.string.tabbed_run_result_tab_categories, R.drawable.marker_default);
        addTab(R.string.tabbed_run_result_tab_map, R.drawable.osm_ic_center_map);

        PagerAdapter adapter = new PagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount());
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

    private void addTab(int p, int p2) {
        TabLayout.Tab tab = tabLayout.newTab();
        tab.setText(p);
        tab.setIcon(p2);
        tabLayout.addTab(tab);
    }
}
