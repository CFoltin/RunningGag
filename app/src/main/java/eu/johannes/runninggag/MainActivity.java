package eu.johannes.runninggag;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ListView runlist;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Vorher: ");
        setContentView(R.layout.activity_main);
        Toolbar title = findViewById(R.id.toolbar);
        setSupportActionBar(title);

        final Button setService = findViewById(R.id.startService);
        setService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(MainActivity.this, Runnow.class));
            }
        });


        Log.d(TAG, "Nachher: ");
        runlist = (ListView) findViewById(R.id.runs);

        ArrayList<String> values = new ArrayList<>();
        RunningGagData runningGagData = RunningGagData.loadData(this);
        for(OnlyOneRun run : runningGagData.getRuns()){
            String theRun = "Lauf vom "+new Date(run.getStartTime()) + "  Gelaufen:" + run.getDistance();

            values.add(theRun);


        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.list_yellow_text, R.id.list_content, values);

        runlist.setAdapter(adapter);

        // ListView Item Click Listener
        runlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // ListView Clicked item index
                int itemPosition = position;

                // ListView Clicked item value
                String itemValue = (String) runlist.getItemAtPosition(position);

                RunningGagData runningGagData = RunningGagData.loadData(MainActivity.this);
                OnlyOneRun run = runningGagData.getRuns().get(position);
                Intent intent = new Intent(MainActivity.this, RunResult.class);
                intent.putExtra ("com.example.runs.run", run);
                startActivity(intent);



            }
        });
    }


}
