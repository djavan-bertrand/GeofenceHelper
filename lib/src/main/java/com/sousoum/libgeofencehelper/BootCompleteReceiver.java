package com.sousoum.libgeofencehelper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Djavan on 15/05/2015.
 */
public class BootCompleteReceiver extends BroadcastReceiver {

    /**
     * Broadcast receiver that listen to the boot complete.
     */

    /**
     * Synchronize all geofences locally stored to the Google API Client
     * @param context The Context in which the receiver is running.
     * @param intent The Intent being received.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        StorableGeofenceManager storableGeofenceManager = new StorableGeofenceManager(context);
        storableGeofenceManager.synchronizeAllGeofencesToGoogleApi();
    }
}
