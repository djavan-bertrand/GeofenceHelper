package com.sousoum.libgeofencehelper;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.location.Geofence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Djavan on 01/07/2015.
 */

class GeofenceStore {

    /**
     * Package local class that stores geofence
     * This implementation stores the geofences in the preferences
     */

    private static final String TAG = "GeofenceStore";

    private static final String SHARED_PREFS = "GeofenceHelperLibStore";

    private static final String GEOFENCE_ID_SET_KEY = "GEOFENCE_ID_SET_KEY";
    private static final String PENDING_INTENT_CLASS_KEY = "PENDING_INTENT_CLASS_KEY";
    private static final String LATITUDE_KEY = "LATITUDE_KEY";
    private static final String LONGITUDE_KEY = "LONGITUDE_KEY";
    private static final String RADIUS_KEY = "RADIUS_KEY";
    private static final String EXPIRATION_KEY = "EXPIRATION_KEY";
    private static final String TRANSITION_KEY = "TRANSITION_KEY";
    private static final String EXPIRATION_DATE_KEY = "EXPIRATION_DATE_KEY";
    private static final String ADDITIONAL_DATA_KEY = "ADDITIONAL_DATA_KEY";
    private static final String ADDITIONAL_DATA_TYPE_KEY = "ADDITIONAL_DATA_TYPE_KEY";

    private static final double NOT_VALID_POSITION = 500;

    private final String mTag;
    private final String mPrefix;
    private final SharedPreferences mPrefs;

    public GeofenceStore(@NonNull Context context, @NonNull String prefix) {
        mPrefix = prefix;

        mPrefs = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);

        mTag = "Store " + prefix;
    }


    /**
     * Stores immediately the given geofence to the store
     * @param geofence The geofence to store
     */
    public void storeGeofence(@NonNull StorableGeofence geofence) {
        Set<String> setTmp = mPrefs.getStringSet(mPrefix + GEOFENCE_ID_SET_KEY, null);
        HashSet<String> geofenceIdSet;
        if (setTmp == null) {
            geofenceIdSet = new HashSet<>();
        } else {
            geofenceIdSet = new HashSet<>(setTmp);
        }

        SharedPreferences.Editor editor = mPrefs.edit();

        String prefix = mPrefix + geofence.getId();
        editor.putString(prefix + PENDING_INTENT_CLASS_KEY, geofence.getPendingIntentClassName());
        editor.putLong(prefix + LATITUDE_KEY, Double.doubleToRawLongBits(geofence.getLatitude()));
        editor.putLong(prefix + LONGITUDE_KEY, Double.doubleToRawLongBits(geofence.getLongitude()));
        editor.putFloat(prefix + RADIUS_KEY, geofence.getRadius());
        editor.putLong(prefix + EXPIRATION_KEY, geofence.getExpirationDuration());
        editor.putInt(prefix + TRANSITION_KEY, geofence.getTransitionType());
        editor.putLong(prefix + EXPIRATION_DATE_KEY, geofence.getExpirationDateInMs());

        // for each additional data, store its value and its type
        if (geofence.getAdditionalData() != null) {
            Set<String> keySet = geofence.getAdditionalData().keySet();
            for (String key : keySet) {
                if (key != null && !key.equals("")) {
                    Object value = geofence.getAdditionalData().get(key);
                    if (value != null) {
                        Class valueClass = value.getClass();
                        if (valueClass.equals(String.class)) {
                            editor.putString(prefix + ADDITIONAL_DATA_KEY + key, (String)value);
                            editor.putString(prefix + ADDITIONAL_DATA_TYPE_KEY + key, String.class.toString());
                        } else if (valueClass.equals(Long.class)) {
                            editor.putLong(prefix + ADDITIONAL_DATA_KEY + key, (Long) value);
                            editor.putString(prefix + ADDITIONAL_DATA_TYPE_KEY + key, Long.class.toString());
                        } else if (valueClass.equals(Integer.class)) {
                            editor.putInt(prefix + ADDITIONAL_DATA_KEY + key, (Integer) value);
                            editor.putString(prefix + ADDITIONAL_DATA_TYPE_KEY + key, Integer.class.toString());
                        } else if (valueClass.equals(Float.class)) {
                            editor.putFloat(prefix + ADDITIONAL_DATA_KEY + key, (Float) value);
                            editor.putString(prefix + ADDITIONAL_DATA_TYPE_KEY + key, Float.class.toString());
                        } else if (valueClass.equals(Boolean.class)) {
                            editor.putBoolean(prefix + ADDITIONAL_DATA_KEY + key, (Boolean) value);
                            editor.putString(prefix + ADDITIONAL_DATA_TYPE_KEY + key, Boolean.class.toString());
                        } else {
                            Log.e(TAG, "Bad additional info data", new Exception("Bad additional info data type"));
                        }
                    }
                } else {
                    Log.e(TAG, "Bad additional info data", new Exception("Bad additional info data key : " + key));
                }
            }
            editor.putStringSet(prefix + ADDITIONAL_DATA_KEY, keySet);
        }

        geofenceIdSet.add(geofence.getId());
        editor.putStringSet(mPrefix + GEOFENCE_ID_SET_KEY, geofenceIdSet);

        editor.apply();
    }

    /**
     * Remove immediately the given geofence from the store
     * @param geofence the geofence to remove
     */
    public void removeGeofence(@NonNull StorableGeofence geofence) {
        Set<String> geofenceIdSet = mPrefs.getStringSet(mPrefix + GEOFENCE_ID_SET_KEY, null);
        if (geofenceIdSet != null && geofenceIdSet.contains(geofence.getId())) {

            SharedPreferences.Editor editor = mPrefs.edit();

            String prefix = mPrefix + geofence.getId();
            editor.remove(prefix + PENDING_INTENT_CLASS_KEY);
            editor.remove(prefix + LATITUDE_KEY);
            editor.remove(prefix + LONGITUDE_KEY);
            editor.remove(prefix + RADIUS_KEY);
            editor.remove(prefix + EXPIRATION_KEY);
            editor.remove(prefix + TRANSITION_KEY);
            editor.remove(prefix + EXPIRATION_DATE_KEY);
            Set<String> keySet = mPrefs.getStringSet(prefix + ADDITIONAL_DATA_KEY, null);
            if (keySet != null) {
                for (String key : keySet) {
                    editor.remove(prefix + ADDITIONAL_DATA_KEY + key);
                    editor.remove(prefix + ADDITIONAL_DATA_TYPE_KEY + key);
                }
            }
            editor.remove(prefix + ADDITIONAL_DATA_KEY);

            geofenceIdSet.remove(geofence.getId());
            editor.putStringSet(GEOFENCE_ID_SET_KEY, geofenceIdSet);

            editor.apply();
        } else {
            Log.e(mTag, "removeGeofenceFromStore : empty list or geofence id is not registered");
        }
    }

    /**
     * Stores immediately the given geofence id to the store
     * @param geofenceId the id of the geofence to add
     */
    public void storeGeofenceId(@NonNull String geofenceId) {
        Set<String> setTmp = mPrefs.getStringSet(mPrefix + GEOFENCE_ID_SET_KEY, null);
        HashSet<String> geofenceIdSet;
        if (setTmp == null) {
            geofenceIdSet = new HashSet<>();
        } else {
            geofenceIdSet = new HashSet<>(setTmp);
        }

        SharedPreferences.Editor editor = mPrefs.edit();

        geofenceIdSet.add(geofenceId);
        editor.putStringSet(mPrefix + GEOFENCE_ID_SET_KEY, geofenceIdSet);

        editor.apply();
    }

    /**
     * Remove a geofence from the remove list
     * @param geofenceId the id of the geofence to remove
     */
    public void removeGeofenceId(@NonNull String geofenceId) {
        Set<String> geofenceIdSet = mPrefs.getStringSet(mPrefix + GEOFENCE_ID_SET_KEY, null);
        if (geofenceIdSet != null && geofenceIdSet.contains(geofenceId)) {

            SharedPreferences.Editor editor = mPrefs.edit();

            geofenceIdSet.remove(geofenceId);
            editor.putStringSet(mPrefix + GEOFENCE_ID_SET_KEY, geofenceIdSet);

            editor.apply();
        } else {
            Log.e(mTag, "removeGeofenceId : empty list or geofence id is not registered");
        }
    }

    /**
     * Get all stored Geofence.
     * @return a list of StorableGeofence (can not be null)
     */
    public @NonNull
    ArrayList<StorableGeofence> getAllGeofences() {
        ArrayList<StorableGeofence> geofenceList = new ArrayList<>();

        Set<String> setTmp = mPrefs.getStringSet(mPrefix + GEOFENCE_ID_SET_KEY, null);
        HashSet<String> geofenceIdSet;
        if (setTmp == null) {
            geofenceIdSet = new HashSet<>();
        } else {
            geofenceIdSet = new HashSet<>(setTmp);
        }

        for (String geofenceId : geofenceIdSet) {
            StorableGeofence storableGeofence = getStoredGeofence(geofenceId);

            if (storableGeofence != null) {
                geofenceList.add(storableGeofence);
            }
        }

        return geofenceList;
    }

    /**
     * Get all stored Geofence ids.
     * @return a set of String (can not be null)
     */
    public @NonNull
    Set<String> getAllGeofenceIds() {
        Set<String> setTmp = mPrefs.getStringSet(mPrefix + GEOFENCE_ID_SET_KEY, null);
        HashSet<String> geofenceIdSet;
        if (setTmp == null) {
            geofenceIdSet = new HashSet<>();
        } else {
            geofenceIdSet = new HashSet<>(setTmp);
        }

        return geofenceIdSet;
    }

    /**
     * Get a stored geofence with its id
     * @param geofenceId The id of the geofence to search
     * @return a StorableGeofence which corresponds to the given id. Null if the id doesn't exist in the list
     */
    private StorableGeofence getStoredGeofence(@NonNull String geofenceId) {
        StorableGeofence storableGeofence = null;

        Set<String> geofenceIdSet = mPrefs.getStringSet(mPrefix + GEOFENCE_ID_SET_KEY, null);
        if (geofenceIdSet != null && geofenceIdSet.contains(geofenceId)) {
            String prefix = mPrefix + geofenceId;
            String pendingIntentClassName = mPrefs.getString(prefix + PENDING_INTENT_CLASS_KEY, DefaultTransitionsIntentService.class.getName());
            double latitude = Double.longBitsToDouble(mPrefs.getLong(prefix + LATITUDE_KEY, Double.doubleToRawLongBits(NOT_VALID_POSITION)));
            double longitude = Double.longBitsToDouble(mPrefs.getLong(prefix + LONGITUDE_KEY, Double.doubleToRawLongBits(NOT_VALID_POSITION)));
            float radius = mPrefs.getFloat(prefix + RADIUS_KEY, 100);
            long expiration = mPrefs.getLong(prefix + EXPIRATION_KEY, Geofence.NEVER_EXPIRE);
            int transition = mPrefs.getInt(prefix + TRANSITION_KEY, Geofence.GEOFENCE_TRANSITION_ENTER);
            HashMap<String, Object> additionalInfo = new HashMap<>();
            Set<String> keySet = mPrefs.getStringSet(prefix + ADDITIONAL_DATA_KEY, null);
            if (keySet != null) {
                for (String key : keySet) {
                    Object value = null;
                    String type = mPrefs.getString(prefix + ADDITIONAL_DATA_TYPE_KEY + key, null);
                    if (type != null) {
                        if (type.equals(String.class.toString())) {
                            value = mPrefs.getString(prefix + ADDITIONAL_DATA_KEY + key, null);
                        } else if (type.equals(Long.class.toString())) {
                            value = mPrefs.getLong(prefix + ADDITIONAL_DATA_KEY + key, 0);
                        } else if (type.equals(Integer.class.toString())) {
                            value =  mPrefs.getInt(prefix + ADDITIONAL_DATA_KEY + key, 0);
                        } else if (type.equals(Float.class.toString())) {
                            value = mPrefs.getFloat(prefix + ADDITIONAL_DATA_KEY + key, 0);
                        } else if (type.equals(Boolean.class.toString())) {
                            value =  mPrefs.getBoolean(prefix + ADDITIONAL_DATA_KEY + key, false);
                        }
                    }
                    if (value != null) {
                        additionalInfo.put(key, value);
                    }
                }
            }

            storableGeofence = new StorableGeofence(geofenceId, pendingIntentClassName, latitude, longitude, radius, expiration, transition, additionalInfo);
        }

        return storableGeofence;
    }

}
