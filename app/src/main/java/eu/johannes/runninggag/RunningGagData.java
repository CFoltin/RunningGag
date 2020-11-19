package eu.johannes.runninggag;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class RunningGagData {
    public ArrayList<OnlyOneRun> getRuns() {
        return runs;
    }

    public void setRuns(ArrayList<OnlyOneRun> runs) {
        this.runs = runs;
    }

    private ArrayList <OnlyOneRun> runs = new ArrayList<>();

    public boolean isDontAskForLocation() {
        return dontAskForLocation;
    }

    public void setDontAskForLocation(boolean dontAskForLocation) {
        this.dontAskForLocation = dontAskForLocation;
    }

    private boolean dontAskForLocation = false;

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
    public double caculateTotalRunDistance (){
        double totalrundistance = 0;
        for (OnlyOneRun run :runs){

            totalrundistance = totalrundistance + run.getDistance() / 1000d;
        }
        return totalrundistance;
    }

    public double caculateYearRunDistance (){
        double yearrundistance = 0;
        Calendar calnow = Calendar.getInstance();
        double year = calnow.get(Calendar.YEAR);
        for (OnlyOneRun run :runs){
            Date datum = new Date(run.getStartTime());
            Calendar datumyear = Calendar.getInstance();
            datumyear.setTime(datum);
            if ( year == datumyear.get(Calendar.YEAR))
            {

                yearrundistance = yearrundistance + run.getDistance() / 1000d;
            }


        }
        return yearrundistance;
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
