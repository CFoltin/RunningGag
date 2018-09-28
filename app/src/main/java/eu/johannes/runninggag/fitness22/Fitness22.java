
package eu.johannes.runninggag.fitness22;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Fitness22 {

    @SerializedName("activityType")
    @Expose
    private Long activityType;
    @SerializedName("caloriesBurned")
    @Expose
    private Double caloriesBurned;
    @SerializedName("distanceMeters")
    @Expose
    private Long distanceMeters;
    @SerializedName("heartRateSamplesArray")
    @Expose
    private List<Object> heartRateSamplesArray = null;
    @SerializedName("historyID")
    @Expose
    private String historyID;
    @SerializedName("isUserEdited")
    @Expose
    private Boolean isUserEdited;
    @SerializedName("locationPointsArray")
    @Expose
    private List<LocationPointsArray> locationPointsArray = null;
    @SerializedName("nonAccurateLocationPointsArray")
    @Expose
    private List<Object> nonAccurateLocationPointsArray = null;
    @SerializedName("paceMinKM")
    @Expose
    private Long paceMinKM;
    @SerializedName("startDate")
    @Expose
    private Long startDate;
    @SerializedName("userActions")
    @Expose
    private List<UserAction> userActions = null;
    @SerializedName("workoutDuration")
    @Expose
    private Long workoutDuration;
    @SerializedName("workoutID")
    @Expose
    private Long workoutID;
    @SerializedName("workoutPlanID")
    @Expose
    private String workoutPlanID;

    public Long getActivityType() {
        return activityType;
    }

    public void setActivityType(Long activityType) {
        this.activityType = activityType;
    }

    public Fitness22 withActivityType(Long activityType) {
        this.activityType = activityType;
        return this;
    }

    public Double getCaloriesBurned() {
        return caloriesBurned;
    }

    public void setCaloriesBurned(Double caloriesBurned) {
        this.caloriesBurned = caloriesBurned;
    }

    public Fitness22 withCaloriesBurned(Double caloriesBurned) {
        this.caloriesBurned = caloriesBurned;
        return this;
    }

    public Long getDistanceMeters() {
        return distanceMeters;
    }

    public void setDistanceMeters(Long distanceMeters) {
        this.distanceMeters = distanceMeters;
    }

    public Fitness22 withDistanceMeters(Long distanceMeters) {
        this.distanceMeters = distanceMeters;
        return this;
    }

    public List<Object> getHeartRateSamplesArray() {
        return heartRateSamplesArray;
    }

    public void setHeartRateSamplesArray(List<Object> heartRateSamplesArray) {
        this.heartRateSamplesArray = heartRateSamplesArray;
    }

    public Fitness22 withHeartRateSamplesArray(List<Object> heartRateSamplesArray) {
        this.heartRateSamplesArray = heartRateSamplesArray;
        return this;
    }

    public String getHistoryID() {
        return historyID;
    }

    public void setHistoryID(String historyID) {
        this.historyID = historyID;
    }

    public Fitness22 withHistoryID(String historyID) {
        this.historyID = historyID;
        return this;
    }

    public Boolean getIsUserEdited() {
        return isUserEdited;
    }

    public void setIsUserEdited(Boolean isUserEdited) {
        this.isUserEdited = isUserEdited;
    }

    public Fitness22 withIsUserEdited(Boolean isUserEdited) {
        this.isUserEdited = isUserEdited;
        return this;
    }

    public List<LocationPointsArray> getLocationPointsArray() {
        return locationPointsArray;
    }

    public void setLocationPointsArray(List<LocationPointsArray> locationPointsArray) {
        this.locationPointsArray = locationPointsArray;
    }

    public Fitness22 withLocationPointsArray(List<LocationPointsArray> locationPointsArray) {
        this.locationPointsArray = locationPointsArray;
        return this;
    }

    public List<Object> getNonAccurateLocationPointsArray() {
        return nonAccurateLocationPointsArray;
    }

    public void setNonAccurateLocationPointsArray(List<Object> nonAccurateLocationPointsArray) {
        this.nonAccurateLocationPointsArray = nonAccurateLocationPointsArray;
    }

    public Fitness22 withNonAccurateLocationPointsArray(List<Object> nonAccurateLocationPointsArray) {
        this.nonAccurateLocationPointsArray = nonAccurateLocationPointsArray;
        return this;
    }

    public Long getPaceMinKM() {
        return paceMinKM;
    }

    public void setPaceMinKM(Long paceMinKM) {
        this.paceMinKM = paceMinKM;
    }

    public Fitness22 withPaceMinKM(Long paceMinKM) {
        this.paceMinKM = paceMinKM;
        return this;
    }

    public Long getStartDate() {
        return startDate;
    }

    public void setStartDate(Long startDate) {
        this.startDate = startDate;
    }

    public Fitness22 withStartDate(Long startDate) {
        this.startDate = startDate;
        return this;
    }

    public List<UserAction> getUserActions() {
        return userActions;
    }

    public void setUserActions(List<UserAction> userActions) {
        this.userActions = userActions;
    }

    public Fitness22 withUserActions(List<UserAction> userActions) {
        this.userActions = userActions;
        return this;
    }

    public Long getWorkoutDuration() {
        return workoutDuration;
    }

    public void setWorkoutDuration(Long workoutDuration) {
        this.workoutDuration = workoutDuration;
    }

    public Fitness22 withWorkoutDuration(Long workoutDuration) {
        this.workoutDuration = workoutDuration;
        return this;
    }

    public Long getWorkoutID() {
        return workoutID;
    }

    public void setWorkoutID(Long workoutID) {
        this.workoutID = workoutID;
    }

    public Fitness22 withWorkoutID(Long workoutID) {
        this.workoutID = workoutID;
        return this;
    }

    public String getWorkoutPlanID() {
        return workoutPlanID;
    }

    public void setWorkoutPlanID(String workoutPlanID) {
        this.workoutPlanID = workoutPlanID;
    }

    public Fitness22 withWorkoutPlanID(String workoutPlanID) {
        this.workoutPlanID = workoutPlanID;
        return this;
    }

}
