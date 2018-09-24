package eu.johannes.runninggag;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ListView runlist;
    private static final int PICKFILE_RESULT_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Vorher: ");
        setContentView(R.layout.activity_main);
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
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu resource file.
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_import:
                callImportAction();
                break;
        }
        return true;
    }

    private void callImportAction() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("file/*");
        startActivityForResult(intent,PICKFILE_RESULT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode){
            case PICKFILE_RESULT_CODE:
                if(resultCode==RESULT_OK){
                    String FilePath = data.getData().getPath();
                    // start import action:
                    Log.d(TAG, "File: " + FilePath);
                }
                break;

        }
    }

    private String readTextFromUri(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }
        inputStream.close();
        return stringBuilder.toString();
    }
}
