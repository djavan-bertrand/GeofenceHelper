package com.sousoum.libgeofencehelper;

import com.google.android.gms.location.Geofence;

import java.util.Date;

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
    private long mExpirationDuration;
    private int mTransitionType;
    private long mExpirationDateInMs;

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
     * @param expiration Geofence expiration duration.
     * @param transition Type of Geofence transition.
     */
    public StorableGeofence(String geofenceId, String pendingIntentClassName, double latitude, double longitude, float radius,
                            long expiration, int transition) {
        // Set the instance fields from the constructor.
        this.mRequestId = geofenceId;
        this.mPendingIntentClassName = pendingIntentClassName;
        this.mLatitude = latitude;
        this.mLongitude = longitude;
        this.mRadius = radius;
        this.mExpirationDuration = expiration;
        this.mTransitionType = transition;
        if (mExpirationDuration != Geofence.NEVER_EXPIRE)
        {
            long nowInMs = new Date().getTime();
            mExpirationDateInMs = nowInMs + mExpirationDuration;
        } else {
            mExpirationDateInMs = 0;
        }
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
                .setTransitionTypes(mTransitionType)
                .setCircularRegion(mLatitude, mLongitude, mRadius)
                .setExpirationDuration(mExpirationDuration)
                .build();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Geofence ").append(mRequestId).append(" : \n");
        builder.append("\t(").append(mLatitude).append(", ").append(mLongitude).append(")\n");
        builder.append("\tradius : ").append(mRadius).append("\n");
        builder.append("\texpiration : ").append(mExpirationDuration).append("\n");
        builder.append("\texpirationDateInMS : ").append(mExpirationDateInMs).append("\n");
        builder.append("\tTransition : ").append(mTransitionType).append("\n");
        builder.append("\tReceiver : ").append(mPendingIntentClassName).append("\n");
        return builder.toString();
    }
}
