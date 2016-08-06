package com.sousoum.libgeofencehelper;

import com.google.android.gms.location.Geofence;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by Djavan on 15/05/2015.
 */
public class StorableGeofence {

    /**
     * A geofence that can be locally stored (for example in the shared preferences)
     */

    // Instance variables
    private final String mRequestId;
    private final String mPendingIntentClassName;
    private final double mLatitude;
    private final double mLongitude;
    private final float mRadius;
    private final int mLoiteringDelay;
    private final long mExpirationDuration;
    private final int mTransitionType;
    private final long mExpirationDateInMs;
    private final HashMap<String, Object> mAdditionalData;

    /**
     * Create a storable geofence.<br/>
     *
     * Note: If you want to create a dwell geofence, you would certainly prefer {@link StorableGeofence#StorableGeofence(String, String, double, double, float, long, int, int, HashMap)}
     *
     * @param geofenceId The Geofence's request ID.
     * @param pendingIntentClassName full class name of the pending intent that should be triggered when the geofence is activated.
     *                               This class should inherit from IntentService
     *                               Can be get with YOUR_CLASS.class.getName()
     *                               If this class is not correct, a DefaultTransitionsIntentService will be called
     * @param latitude Latitude of the Geofence's center in degrees.
     * @param longitude Longitude of the Geofence's center in degrees.
     * @param radius Radius of the geofence circle in meters.
     * @param expiration Geofence expiration duration, pass {@link Geofence#NEVER_EXPIRE} if you don't want an expiration date.
     * @param transition Type of Geofence transition.
     * @param additionalData Additional data you want to pass. It maps a String to an Object. This Object should be either a String, Long, Integer, Double Boolean or Float
     */
    public StorableGeofence(String geofenceId, String pendingIntentClassName, double latitude, double longitude, float radius,
                            long expiration, int transition, HashMap<String, Object> additionalData) {
        // Set the instance fields from the constructor.
        this.mRequestId = geofenceId;
        this.mPendingIntentClassName = pendingIntentClassName;
        this.mLatitude = latitude;
        this.mLongitude = longitude;
        this.mRadius = radius;
        this.mExpirationDuration = expiration;
        this.mLoiteringDelay = 0;
        this.mTransitionType = transition;
        if (mExpirationDuration != Geofence.NEVER_EXPIRE)
        {
            long nowInMs = new Date().getTime();
            mExpirationDateInMs = nowInMs + mExpirationDuration;
        } else {
            mExpirationDateInMs = 0;
        }
        this.mAdditionalData = additionalData;
    }

    /**
     * Create a storable geofence
     * @param geofenceId The Geofence's request ID.
     * @param pendingIntentClassName full class name of the pending intent that should be triggered when the geofence is activated.
     *                               This class should inherit from IntentService
     *                               Can be get with YOUR_CLASS.class.getName()
     *                               If this class is not correct, a DefaultTransitionsIntentService will be called
     * @param latitude Latitude of the Geofence's center in degrees.
     * @param longitude Longitude of the Geofence's center in degrees.
     * @param radius Radius of the geofence circle in meters.
     * @param expiration Geofence expiration duration, pass {@link Geofence#NEVER_EXPIRE} if you don't want an expiration date.
     * @param loiteringDelay Sets the delay between GEOFENCE_TRANSITION_ENTER and GEOFENCE_TRANSITION_DWELLING in milliseconds
     *                       This value is ignored if the transition types don't include a GEOFENCE_TRANSITION_DWELL filter.
     * @param transition Type of Geofence transition.
     * @param additionalData Additional data you want to pass. It maps a String to an Object. This Object should be either a String, Long, Integer, Double Boolean or Float
     */
    public StorableGeofence(String geofenceId, String pendingIntentClassName, double latitude, double longitude, float radius,
                            long expiration, int loiteringDelay, int transition, HashMap<String, Object> additionalData) {
        // Set the instance fields from the constructor.
        this.mRequestId = geofenceId;
        this.mPendingIntentClassName = pendingIntentClassName;
        this.mLatitude = latitude;
        this.mLongitude = longitude;
        this.mRadius = radius;
        this.mExpirationDuration = expiration;
        this.mLoiteringDelay = loiteringDelay;
        this.mTransitionType = transition;
        if (mExpirationDuration != Geofence.NEVER_EXPIRE)
        {
            long nowInMs = new Date().getTime();
            mExpirationDateInMs = nowInMs + mExpirationDuration;
        } else {
            mExpirationDateInMs = 0;
        }
        this.mAdditionalData = additionalData;
    }

    // Instance field getters.
    public String getId() {
        return mRequestId;
    }
    public String getPendingIntentClassName() {
        return mPendingIntentClassName;
    }
    public double getLatitude() {
        return mLatitude;
    }
    public double getLongitude() {
        return mLongitude;
    }
    public float getRadius() {
        return mRadius;
    }
    public HashMap<String, Object> getAdditionalData()
    {
        return mAdditionalData;
    }

    /**
     * Return the real expiration duration
     * @return the time in milli until the expiration date
     */
    public long getExpirationDuration() {
        long expirationDuration = mExpirationDuration;
        if (mExpirationDuration != Geofence.NEVER_EXPIRE)
        {
            long nowInMs = new Date().getTime();
            expirationDuration = nowInMs - mExpirationDateInMs;
        }

        return expirationDuration;
    }
    public int getLoiteringDelay() {
        return mLoiteringDelay;
    }
    public int getTransitionType() {
        return mTransitionType;
    }
    public long getExpirationDateInMs()
    {
        return mExpirationDateInMs;
    }

    public boolean isExpired()
    {
        boolean isExpired = false;
        if (mExpirationDuration != Geofence.NEVER_EXPIRE)
        {
            long nowInMs = new Date().getTime();
            if (nowInMs > mExpirationDateInMs) {
                isExpired = true;
            }
        }

        return isExpired;
    }

    /**
     * Creates a Location Services Geofence object from a StorableGeofence.
     * @return A Geofence object.
     */
    public Geofence toGeofence() {
        // Build a new Geofence object.
        return new Geofence.Builder()
                .setRequestId(mRequestId)
                .setLoiteringDelay(mLoiteringDelay)
                .setTransitionTypes(mTransitionType)
                .setCircularRegion(mLatitude, mLongitude, mRadius)
                .setExpirationDuration(mExpirationDuration)
                .build();
    }

    @Override
    public String toString() {
        String str = "Geofence " + mRequestId + " : \n";
        str += "\t(" + mLatitude + ", " + mLongitude + ")\n";
        str += "\tRadius : " + mRadius + "\n";
        str += "\tExpiration : " + mExpirationDuration + "\n";
        str += "\texpirationDateInMS : " + mExpirationDateInMs + "\n";
        str += "\tloiteringDelay : " + mLoiteringDelay + "\n";
        str += "\tTransition : " + mTransitionType + "\n";
        str += "\tAdditional data : " + mAdditionalData + "\n";
        str += "\tReceiver : " + mPendingIntentClassName + "\n";
        return str;
    }
}
