package eu.johannes.runninggag;

/**
 * Created by johannes on 11.01.19.
 */
public class RunTime {
    long startime;
    long stoptime;

    public long getStartime() {
        return startime;
    }

    public void setStartime(long startime) {
        this.startime = startime;
    }

    public long getStoptime() {
        return stoptime;
    }

    public void setStoptime(long stoptime) {
        this.stoptime = stoptime;
    }

    @Override
    public String toString() {
        return "RunTime{" +
                "startime=" + startime +
                ", stoptime=" + stoptime +
                '}';
    }
}
