package eu.johannes.runninggag;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RunTime runTime = (RunTime) o;
        return startime == runTime.startime &&
                stoptime == runTime.stoptime;
    }

    @Override
    public int hashCode() {
        return Objects.hash(startime, stoptime);
    }
}
