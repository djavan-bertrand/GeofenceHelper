package com.sousoum.geofencehelperexample;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.sousoum.libgeofencehelper.StorableGeofence;
import com.sousoum.libgeofencehelper.StorableGeofenceManager;

import java.security.Provider;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity implements LocationListener, StorableGeofenceManager.StorableGeofenceManagerListener {

    private static final String TAG = "MainActivity";

    private static final String GEOFENCE_ID_FOR_DEFAULT_RECEIVER = "DefaultReceiverGeofence";
    private static final String GEOFENCE_ID_FOR_CUSTOM_RECEIVER = "CustomReceiverGeofence";

    private LocationManager mLocationManager;
    private StorableGeofenceManager mGeofenceManager;
    private Location mCurrentLocation;

    private Button mDefaultBt;
    private Button mCustomBt;
    private Button mDeleteBt;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDefaultBt = (Button) findViewById(R.id.defaultBt);
        mCustomBt = (Button) findViewById(R.id.customBt);
        mDeleteBt = (Button) findViewById(R.id.deleteBt);
        mTextView = (TextView) findViewById(R.id.textView);

        mGeofenceManager = new StorableGeofenceManager(this);
        mGeofenceManager.setListener(this);

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // get the last known location with best accuracy
        List<String> providers = mLocationManager.getProviders(true);
        for (String provider : providers) {
            Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (mCurrentLocation == null || l.getAccuracy() < mCurrentLocation.getAccuracy()) {
                mCurrentLocation = l;
            }
        }

        updateUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                2000,
                1, this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        mLocationManager.removeUpdates(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void updateUI() {
        if (mCurrentLocation != null) {
            mDefaultBt.setEnabled(true);
            mCustomBt.setEnabled(true);
            mDeleteBt.setEnabled(true);
            mTextView.setText("");
        } else {
            mDefaultBt.setEnabled(false);
            mCustomBt.setEnabled(false);
            mDeleteBt.setEnabled(false);
            mTextView.setText("Getting your current location...");
        }
    }

    public void onCustomClicked(View view) {
        addGeofence(false);
    }

    public void onDefaultClicked(View view) {
        addGeofence(true);
    }

    public void onDeleteAllClicked(View view) {
        ArrayList<StorableGeofence> storedGeo = mGeofenceManager.getAllGeofences();
        for (StorableGeofence geo : storedGeo) {
            mGeofenceManager.removeGeofence(geo.getId());
        }
    }

    private void addGeofence(boolean defaultReceiver) {
        String geoId;
        String receiverClassName;
        if (defaultReceiver) {
            geoId = GEOFENCE_ID_FOR_DEFAULT_RECEIVER;
            receiverClassName = null;
        } else {
            geoId = GEOFENCE_ID_FOR_CUSTOM_RECEIVER;
            receiverClassName = CustomTransitionsIntentService.class.getName();
        }

        StorableGeofence storableGeofence = new StorableGeofence(
                geoId,
                receiverClassName,
                mCurrentLocation.getLatitude(),
                mCurrentLocation.getLongitude(),
                200,
                Geofence.NEVER_EXPIRE,
                Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT);
        boolean addedOnGoing = mGeofenceManager.addGeofence(storableGeofence);
        if (!addedOnGoing) {
            Log.e(TAG, "Addition of geofence has been refused " + storableGeofence);
        }
    }

    //region StorableGeofenceListener
    @Override
    public void geofenceAddStatus(StorableGeofence geofence, Status status) {
        if (geofence != null) {
            if (status.isSuccess()) {
                Toast.makeText(this, "Geofence " + geofence.getId() + " has been added", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error when adding " + geofence.getId() + " : " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void geofenceRemoveStatus(String geofenceId, Status status) {
        if (status.isSuccess()) {
            Toast.makeText(this, "Geofence " + geofenceId + " has been removed", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error when removing " + geofenceId + " : " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    //endregion StorableGeofenceListener

    //region Location listener
    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "Location changed : " + location);
        if (location != null) {
            mCurrentLocation = location;
            updateUI();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {
        Toast.makeText(this, "Please enable your gps", Toast.LENGTH_SHORT).show();
    }
    //endregion Location listener
}
