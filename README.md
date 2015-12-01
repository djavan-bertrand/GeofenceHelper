# GeofenceHelper

This library helps you to use the geofencing on Android.

It stores your geofences, add them to the Google API and informs the caller about the status of the operation.

An example is also provided. It shows you how to create, add and delete a geofence.

The status of this library is currently in beta.

## Import the library to your project 

First add the [Jitpack](https://jitpack.io/) repository to your project build.gradle file:

```
repositories {
    // ...
    maven { url "https://jitpack.io" }
}
```

Then add the dependency to the GeofenceHelper library in the build.gradle.
 
```
dependencies {
    // ...
    compile 'com.github.djavan-bertrand:GeofenceHelper:0.0.1'        
}
```

Adding this dependency will also add the following permissions to your apk:

```
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
```

## How to use

A working example is available [in the project](https://github.com/djavan-bertrand/GeofenceHelper/tree/master/example).

Here are the main points:

#### Declare a StorableGeofenceManager.

```
private StorableGeofenceManager mGeofenceManager;
```

#### Then create it and declare your class as a listener
```
mGeofenceManager = new StorableGeofenceManager(this);
mGeofenceManager.setListener(this);
```

#### Add a geofence

```
StorableGeofence storableGeofence = new StorableGeofence(
                geoId,
                receiverClassName,
                latitude,
                longitude,
                radius,
                Geofence.NEVER_EXPIRE,
                Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT,
                additionalData);
boolean addedOnGoing = mGeofenceManager.addGeofence(storableGeofence);
```

*receiverClassName* is the name of the class that will be called when the geofence is triggered by Android. It should inherits from IntentService.<br/>
*additionalData* is an HashMap<String, Object> which provides additional data. The values should be of the following types: String, Long, Integer, Double Boolean or Float.

*addedOnGoing* is the immediate result of the call. It is true if add has been asked, false otherwise.

After this call, if *addedOnGoing* is true, the *geofenceAddStatus* callback will be called to inform you about the status of the Geofence.

## Questions

Feel free to ask your questions to [@Djava7](https://twitter.com/Djava7).<br/>
You can also [open an issue on Github](https://github.com/djavan-bertrand/GeofenceHelper/issues/new).
