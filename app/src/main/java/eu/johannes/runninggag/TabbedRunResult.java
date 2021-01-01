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

        TabLayout.Tab firstTab = tabLayout.newTab();
        firstTab.setText(R.string.tabbed_run_result_tab_run); // set the Text for the first Tab
        firstTab.setIcon(R.drawable.osm_ic_center_map); // set an icon for the
        tabLayout.addTab(firstTab); // add  the tab at in the TabLayout

        TabLayout.Tab secondTab = tabLayout.newTab();
        secondTab.setText(R.string.tabbed_run_result_tab_categories); // set the Text for the second Tab
        secondTab.setIcon(R.drawable.marker_default); // set an icon for the second tab
        tabLayout.addTab(secondTab); // add  the tab  in the TabLayout

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
}
