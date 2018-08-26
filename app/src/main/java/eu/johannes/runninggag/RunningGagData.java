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
        return gson.fromJson(data, RunningGagData.class);
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
