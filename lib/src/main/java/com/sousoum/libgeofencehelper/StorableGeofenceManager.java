package com.sousoum.libgeofencehelper;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingApi;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Djavan on 15/05/2015.
 */
public class StorableGeofenceManager implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    /**
     * Class that manages addition and deletion of Geofences in the Google API Client. It uses a store to remember all geofences that are currently in the Google API Client.
     * The store is actually backed by the shared preferences
     */

    public interface StorableGeofenceManagerListener {
        /**
         * Called when a geofence has been, successfully or not, added to the Google API Client
         * @param geofence the geofence that has been added
         * @param status the status of the operation
         */
        void geofenceAddStatus(StorableGeofence geofence, Status status);

        /**
         * Called when a geofence has been, successfully or not, removed from the Google API Client
         * @param geofenceId the id of the geofence that has been removed
         * @param status the status of the operation
         */
        void geofenceRemoveStatus(String geofenceId, Status status);
    }

    private static final String TAG = "GeofenceManager";

    private static final String TO_ADD_STORE = "TO_ADD_STORE";
    private static final String TO_REMOVE_STORE = "TO_REMOVE_STORE";
    private static final String SYNCED_STORE = "SYNCED_STORE";

    private final Context mContext;
    private GeofencingApi mGeofencingAPI = LocationServices.GeofencingApi;
    private GoogleApiClient mGoogleApiClient;

    private StorableGeofenceManagerListener mListener;

    private GeofenceStore mToAddStore; // store of the geofence to add to the Google API Client
    private GeofenceStore mToRemoveStore; // store of the geofence to remove from the Google API Client
    private GeofenceStore mSyncedStore; // store that represent which geofences are in the Google API Client

    public StorableGeofenceManager(Context context) {
        mContext = context;

        mToAddStore = new GeofenceStore(context, TO_ADD_STORE);
        mToRemoveStore = new GeofenceStore(context, TO_REMOVE_STORE);
        mSyncedStore = new GeofenceStore(context, SYNCED_STORE);

        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    /**
     * Ask for a connection to the Google API Client
     */
    private void googleApiConnect() {
        if (mGoogleApiClient != null && !mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    /**
     * Set the listener. This listener will be informed when the geofences are modified in the google api client
     * @param listener a listener
     */
    public void setListener(StorableGeofenceManagerListener listener) {
        mListener = listener;
    }

    /**
     * Get the current listener
     * @return the current listener
     */
    public StorableGeofenceManagerListener getListener() {
        return mListener;
    }

    /**
     * Add a geofence to the store
     * This will also add the geofence to the google api client if connected. If not, it will trigger a connection
     * @param storableGeofence the geofence to store
     * @return true if add has been asked, false otherwise. false could be returned if the geofence is expired
     */
    public boolean addGeofence(@NonNull StorableGeofence storableGeofence) {
        boolean addedOngoing = false;
        if (! storableGeofence.isExpired())
        {
            mToAddStore.storeGeofence(storableGeofence);

            if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
            {
                ArrayList<Geofence> geofenceList = new ArrayList<>();
                Geofence geofence = storableGeofence.toGeofence();
                geofenceList.add(geofence);

                GeofenceAddStatus addStatus = new GeofenceAddStatus(storableGeofence);

                mGeofencingAPI.addGeofences(mGoogleApiClient, geofenceList, createRequestPendingIntent(storableGeofence)).setResultCallback(addStatus);
                Log.i(TAG, "Added " + storableGeofence);
            }
            else
            {
                googleApiConnect();
            }

            addedOngoing = true;
        }

        return addedOngoing;
    }

    /**
     * Ask to remove a geofence from the store.
     * If the Google API Client is not connected, trigger a connection
     * Else, remove from the Google API client. It will be removed from store if the operation is successful
     * @param geofenceId The id of the geofence to remove
     */
    public void removeGeofence(@NonNull String geofenceId) {

        mToRemoveStore.storeGeofenceId(geofenceId);

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {

            ArrayList<String> geofenceList = new ArrayList<>();
            geofenceList.add(geofenceId);

            GeofenceRemoveStatus removeStatus = new GeofenceRemoveStatus(geofenceId);

            mGeofencingAPI.removeGeofences(mGoogleApiClient, geofenceList).setResultCallback(removeStatus);
            Log.i(TAG, "Removed " + geofenceId);
        } else {
            googleApiConnect();
        }
    }

    /**
     * Ask to synchronize all stored geofences to the Google API Client
     */
    public void synchronizeAllGeofencesToGoogleApi() {
        Log.i(TAG, "Try to update list of geofences");
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {

            // first, add all (already) stored geofences, without listener
            ArrayList<StorableGeofence> storedGeofences = mSyncedStore.getAllGeofences();
            if (!storedGeofences.isEmpty()) {
                ArrayList<Geofence> geofenceList = new ArrayList<>();
                // for each geofence, add it to the Google API Client
                for (StorableGeofence storableGeofence : storedGeofences) {
                    if (! storableGeofence.isExpired()) {
                        Geofence geofence = storableGeofence.toGeofence();
                        geofenceList.clear();
                        geofenceList.add(geofence);

                        mGeofencingAPI.addGeofences(mGoogleApiClient, geofenceList, createRequestPendingIntent(storableGeofence));
                        Log.i(TAG, "Added " + storableGeofence);
                    } else {
                        // if the geofence has expired, add it to the list to delete
                        mToRemoveStore.storeGeofenceId(storableGeofence.getId());
                    }
                }
                Log.i(TAG, "All already stored geofences have been submitted to be synchronized with Google API Client");
            }

            // add all geofences from the to add list
            ArrayList<StorableGeofence> toAddGeofences = mToAddStore.getAllGeofences();
            if (!toAddGeofences.isEmpty()) {
                ArrayList<Geofence> geofenceList = new ArrayList<>();
                // for each geofence, add it to the Google API Client
                for (StorableGeofence storableGeofence : toAddGeofences) {
                    Geofence geofence = storableGeofence.toGeofence();
                    geofenceList.clear();
                    geofenceList.add(geofence);

                    GeofenceAddStatus addStatus = new GeofenceAddStatus(storableGeofence);

                    mGeofencingAPI.addGeofences(mGoogleApiClient, geofenceList, createRequestPendingIntent(storableGeofence)).setResultCallback(addStatus);
                    Log.i(TAG, "Added " + storableGeofence);
                }
                Log.i(TAG, "All geofences to add have been submitted to be synchronized with Google API Client");
            }

            // remove all geofences from the to remove list
            Set<String> toRemoveGeofences = mToRemoveStore.getAllGeofenceIds();
            if (!toRemoveGeofences.isEmpty()) {
                ArrayList<String> geofenceList = new ArrayList<>();
                // for each geofence, remove it to the Google API Client
                for (String geofenceId : toRemoveGeofences) {
                    geofenceList.clear();
                    geofenceList.add(geofenceId);

                    GeofenceRemoveStatus removeStatus = new GeofenceRemoveStatus(geofenceId);

                    mGeofencingAPI.removeGeofences(mGoogleApiClient, geofenceList).setResultCallback(removeStatus);
                    Log.i(TAG, "Removed " + geofenceId);
                }
                Log.i(TAG, "All geofences to remove have been submitted to be synchronized with Google API Client");
            }
        } else {
            googleApiConnect();
        }
    }

    /**
     * Create a pending intent from the storable geofence
     * @param storableGeofence The storable geofence which should contain the class name of the pending intent
     * @return The pending intent of the class if it has been succesfully loaded, or a DefaultTransitionsIntentService
     */
    private PendingIntent createRequestPendingIntent(StorableGeofence storableGeofence) {
        Class classOfPendingIntent = DefaultTransitionsIntentService.class;
        if (storableGeofence.getPendingIntentClassName() != null) {
            try {
                Class classOfPendingIntentTmp = Class.forName(storableGeofence.getPendingIntentClassName());
                if (classOfPendingIntentTmp != null) {
                    classOfPendingIntent = classOfPendingIntentTmp;
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        // Create an Intent pointing to the IntentService
        Intent intent = new Intent(mContext, classOfPendingIntent);

        /*
         * Return a PendingIntent to start the IntentService.
         * Always create a PendingIntent sent to Location Services
         * with FLAG_UPDATE_CURRENT, so that sending the PendingIntent
         * again updates the original. Otherwise, Location Services
         * can't match the PendingIntent to requests made with it.
         */
        return PendingIntent.getService(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Get all stored Geofence that are synced with Google API Client.
     * @return a list of StorableGeofence (can not be null)
     */
    public @NonNull
    ArrayList<StorableGeofence> getAllGeofences() {
        return mSyncedStore.getAllGeofences();
    }

    //region GoogleApiClient.ConnectionCallbacks
    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "GoogleApiClient connected");

        synchronizeAllGeofencesToGoogleApi();

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Google API client onConnectionSuspended");
    }
    //endregion GoogleApiClient.ConnectionCallbacks

    //region GoogleApiClient.OnConnectionFailedListener
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "Connection to Google API client failed with error code :" + connectionResult.getErrorCode());
    }
    //endregion GoogleApiClient.OnConnectionFailedListener

    private class GeofenceRemoveStatus implements ResultCallback<Status> {

        /**
         * Inner class that will responds to ResultCallback when a geofence will be, successfully or not, removed from the Google API Client
         */

        private String mGeofenceId;
        public GeofenceRemoveStatus(@NonNull String geofenceId) {
            mGeofenceId = geofenceId;
        }

        @Override
        public void onResult(Status status) {
            if (status.isSuccess()) {
                Log.i(TAG, "Removed successfully geofence " + mGeofenceId + " to the Google API");
                // since the operation is successful, remove from the local store
                mSyncedStore.removeGeofenceId(mGeofenceId);

                mToRemoveStore.removeGeofenceId(mGeofenceId);
            } else {
                Log.e(TAG, "Error : geofence not removed. Error is " + status.getStatusMessage() + "(code : " + status.getStatusCode() + ")");
            }

            if (mListener != null) {
                mListener.geofenceRemoveStatus(mGeofenceId, status);
            }
        }
    }

    private class GeofenceAddStatus implements ResultCallback<Status> {

        /**
         * Inner class that will responds to ResultCallback when a geofence will be, successfully or not, added to the Google API Client
         */

        private StorableGeofence mGeofence;

        public GeofenceAddStatus(@NonNull StorableGeofence geofence) {
            mGeofence = geofence;
        }

        @Override
        public void onResult(Status status) {
            if (status.isSuccess()) {
                Log.i(TAG, "Added successfully geofence " + mGeofence + " to the Google API");
                // since the operation is successful, remove from the local store
                mSyncedStore.storeGeofence(mGeofence);

                mToAddStore.removeGeofence(mGeofence);
            } else {
                Log.e(TAG, "Error : geofence not added. Error is " + status.getStatusMessage() + "(code : " + status.getStatusCode() + ")");
            }

            if (mListener != null) {
                mListener.geofenceAddStatus(mGeofence, status);
            }
        }
    }
}
