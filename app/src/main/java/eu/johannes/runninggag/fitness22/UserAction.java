
package eu.johannes.runninggag.fitness22;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserAction {

    @SerializedName("actionType")
    @Expose
    private Long actionType;
    @SerializedName("timeStamp")
    @Expose
    private Long timeStamp;

    public Long getActionType() {
        return actionType;
    }

    public void setActionType(Long actionType) {
        this.actionType = actionType;
    }

    public UserAction withActionType(Long actionType) {
        this.actionType = actionType;
        return this;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public UserAction withTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
        return this;
    }

}
