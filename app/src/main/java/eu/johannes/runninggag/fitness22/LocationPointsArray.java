
package eu.johannes.runninggag.fitness22;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LocationPointsArray {

    @SerializedName("mAccuracy")
    @Expose
    private Double mAccuracy;
    @SerializedName("mAltitude")
    @Expose
    private Double mAltitude;
    @SerializedName("mBearing")
    @Expose
    private Double mBearing;
    @SerializedName("mLatitude")
    @Expose
    private Double mLatitude;
    @SerializedName("mLongitude")
    @Expose
    private Double mLongitude;
    @SerializedName("mSpeed")
    @Expose
    private Double mSpeed;
    @SerializedName("mTime")
    @Expose
    private Long mTime;
    @SerializedName("pointMode")
    @Expose
    private Long pointMode;
    @SerializedName("provider")
    @Expose
    private String provider;
    @SerializedName("receivedInPauseMode")
    @Expose
    private Boolean receivedInPauseMode;

    public Double getMAccuracy() {
        return mAccuracy;
    }

    public void setMAccuracy(Double mAccuracy) {
        this.mAccuracy = mAccuracy;
    }

    public LocationPointsArray withMAccuracy(Double mAccuracy) {
        this.mAccuracy = mAccuracy;
        return this;
    }

    public Double getMAltitude() {
        return mAltitude;
    }

    public void setMAltitude(Double mAltitude) {
        this.mAltitude = mAltitude;
    }

    public LocationPointsArray withMAltitude(Double mAltitude) {
        this.mAltitude = mAltitude;
        return this;
    }

    public Double getMBearing() {
        return mBearing;
    }

    public void setMBearing(Double mBearing) {
        this.mBearing = mBearing;
    }

    public LocationPointsArray withMBearing(Double mBearing) {
        this.mBearing = mBearing;
        return this;
    }

    public Double getMLatitude() {
        return mLatitude;
    }

    public void setMLatitude(Double mLatitude) {
        this.mLatitude = mLatitude;
    }

    public LocationPointsArray withMLatitude(Double mLatitude) {
        this.mLatitude = mLatitude;
        return this;
    }

    public Double getMLongitude() {
        return mLongitude;
    }

    public void setMLongitude(Double mLongitude) {
        this.mLongitude = mLongitude;
    }

    public LocationPointsArray withMLongitude(Double mLongitude) {
        this.mLongitude = mLongitude;
        return this;
    }

    public Double getMSpeed() {
        return mSpeed;
    }

    public void setMSpeed(Double mSpeed) {
        this.mSpeed = mSpeed;
    }

    public LocationPointsArray withMSpeed(Double mSpeed) {
        this.mSpeed = mSpeed;
        return this;
    }

    public Long getMTime() {
        return mTime;
    }

    public void setMTime(Long mTime) {
        this.mTime = mTime;
    }

    public LocationPointsArray withMTime(Long mTime) {
        this.mTime = mTime;
        return this;
    }

    public Long getPointMode() {
        return pointMode;
    }

    public void setPointMode(Long pointMode) {
        this.pointMode = pointMode;
    }

    public LocationPointsArray withPointMode(Long pointMode) {
        this.pointMode = pointMode;
        return this;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public LocationPointsArray withProvider(String provider) {
        this.provider = provider;
        return this;
    }

    public Boolean getReceivedInPauseMode() {
        return receivedInPauseMode;
    }

    public void setReceivedInPauseMode(Boolean receivedInPauseMode) {
        this.receivedInPauseMode = receivedInPauseMode;
    }

    public LocationPointsArray withReceivedInPauseMode(Boolean receivedInPauseMode) {
        this.receivedInPauseMode = receivedInPauseMode;
        return this;
    }

}
