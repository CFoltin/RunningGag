package eu.johannes.runninggag;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.ArrayList;

public class RunningGagData {
    public ArrayList<OnlyOneRun> getRuns() {
        return runs;
    }

    public void setRuns(ArrayList<OnlyOneRun> runs) {
        this.runs = runs;
    }

    private ArrayList <OnlyOneRun> runs = new ArrayList<>();

    public static RunningGagData loadData(Context context){
        SharedPreferences preferences = context.getSharedPreferences("application_data", Context.MODE_PRIVATE);
        String data = preferences.getString("data", "{}");
        Gson gson = new Gson();
        RunningGagData runningGagData = gson.fromJson(data, RunningGagData.class);
        // store all points separately.
        boolean changed = false;
        for (OnlyOneRun run : runningGagData.getRuns()){
            if(!run.getDataPoints().isEmpty()){
                run.storeDataPoints(context);
                run.getDataPoints().clear();
                changed = true;
            }
            if(run.getTime().isEmpty()){
                // the new time array for pauses is not present. Create it.
                RunTime runTime = new RunTime();
                runTime.startime = run.getStartTime();
                runTime.stoptime = run.getStopTime();
                run.getTime().add(runTime);
                changed = true;
            }
        }
        if(changed) {
            runningGagData.storeData(context);
        }
        return runningGagData;
    }

    public void storeData(Context context){
        SharedPreferences preferences = context.getSharedPreferences("application_data", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String out = gson.toJson(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("data", out);
        editor.commit();
    }

}
