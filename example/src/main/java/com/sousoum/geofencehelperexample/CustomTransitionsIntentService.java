package com.sousoum.geofencehelperexample;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

/**
 * Created by Djavan on 13/12/2014.
 */
public class CustomTransitionsIntentService extends IntentService {

    public CustomTransitionsIntentService() {
        super("CustomTransitionsIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String notifText = "Not a geo event";
        GeofencingEvent geoEvent = GeofencingEvent.fromIntent(intent);
        if (geoEvent != null) {
            if (geoEvent.hasError()) {
                notifText = "Error : " + geoEvent.getErrorCode();
            } else {
                int transition = geoEvent.getGeofenceTransition();
                String transitionStr;
                switch (transition) {
                    case Geofence.GEOFENCE_TRANSITION_ENTER:
                        transitionStr = "Enter-";
                        break;
                    case Geofence.GEOFENCE_TRANSITION_EXIT:
                        transitionStr = "Exit-";
                        break;
                    case Geofence.GEOFENCE_TRANSITION_DWELL:
                        transitionStr = "Dwell-";
                        break;
                    default:
                        transitionStr = "Unknown-";
                }

                List<Geofence> triggeringGeo = geoEvent.getTriggeringGeofences();

                StringBuilder strBuilder = new StringBuilder();
                strBuilder.append(transitionStr);
                for (int i = 0; i < triggeringGeo.size(); i++) {
                    Geofence geo = triggeringGeo.get(i);
                    strBuilder.append(geo.getRequestId());
                    strBuilder.append("-");
                }
                notifText = strBuilder.toString();
            }
        }

        sendNotification(notifText);
    }

    private void sendNotification(String text) {
        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        // Set the notification contents
        builder.setSmallIcon(com.sousoum.libgeofencehelper.R.drawable.default_notif)
                .setContentTitle("Custom")
                .setContentText(text);

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Issue the notification
        mNotificationManager.notify(0, builder.build());
    }
}
