package org.nachosapps.weatherapplication.Helpers.location;

import static android.text.format.DateUtils.HOUR_IN_MILLIS;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Looper;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;

/**
 * Created by Kamil on 2018-03-06.
 */

public class LocationHelpers {


    public static void createLocationRequest(LocationCallback mLocationCallback, Context context,
            FusedLocationProviderClient mFusedLocationClient) {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(HOUR_IN_MILLIS); // one hour interval
        locationRequest.setFastestInterval(HOUR_IN_MILLIS);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.requestLocationUpdates(locationRequest, mLocationCallback,
                    Looper.myLooper());
        }
    }
}
