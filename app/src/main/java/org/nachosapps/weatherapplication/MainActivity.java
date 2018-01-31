package org.nachosapps.weatherapplication;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import org.nachosapps.weatherapplication.Common.Common;
import org.nachosapps.weatherapplication.Helper.Helper;
import org.nachosapps.weatherapplication.Model.OpenWeatherMap;

import java.lang.reflect.Type;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName ();

    TextView txtCity, txtLastUpdate, txtDescription, txtHumidity, txtTime, txtCelsius;
    ImageView imageView;
    Button btnRefresh;
    OpenWeatherMap openWeatherMap = new OpenWeatherMap ();
    LocationRequest mLocationRequest;

    boolean mRequestingLocationUpdates;

    private FusedLocationProviderClient mFusedLocationClient;
    protected Location mLastLocation;

    final int REQUEST_CODE_PERMISSIONS = 0;
    String[] myPermissions = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_main );

        initViews ();


        if (!checkPermissions ()) {
            requestPermissions ();
        } else {
            createLocationRequest ();
            getLastLocation ();
        }

        btnRefresh.setOnClickListener ( new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                getLastLocation ();
                Toast.makeText ( getApplicationContext (), "Refreshed weather", Toast.LENGTH_SHORT ).show ();
            }
        } );
    }

    private void initViews() {
        //Control
        txtCity = findViewById ( R.id.txtCity );
        txtCelsius = findViewById ( R.id.txtCelsius );
        txtDescription = findViewById ( R.id.txtDescription );
        txtHumidity = findViewById ( R.id.txtHumidity );
        txtLastUpdate = findViewById ( R.id.txtLastUpdate );
        txtTime = findViewById ( R.id.txtTime );
        imageView = findViewById ( R.id.imageView );
        btnRefresh = findViewById ( R.id.btnRefresh );
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient ( this );
    }

    LocationCallback mLocationCallback = new LocationCallback () {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations ()) {
                Log.i ( TAG, "Location: " + location.getLatitude () + " " + location.getLongitude () );
                mLastLocation = location;
            }
            new GetWeather ().execute ( Common.apiRequest ( String.valueOf ( locationResult.getLastLocation ().getLatitude () ), String.valueOf ( locationResult.getLastLocation ().getLongitude () ) ) );
        }
    };

    void createLocationRequest() {
        mLocationRequest = new LocationRequest ();
        mLocationRequest.setInterval ( 60000 * 60 ); // one hour interval
        mLocationRequest.setFastestInterval ( 60000 * 60 );
        mLocationRequest.setPriority ( LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY );
        if (ContextCompat.checkSelfPermission ( this,
                Manifest.permission.ACCESS_FINE_LOCATION )
                == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.requestLocationUpdates ( mLocationRequest, mLocationCallback, Looper.myLooper () );
        }
    }

    private void loadSavedWeather() {
        SharedPreferences sharedPreferences = this.getSharedPreferences ( "weatherInformation", Context.MODE_PRIVATE );

        this.txtCity.setText ( sharedPreferences.getString ( "city", "Loading" ) );
        this.txtDescription.setText ( sharedPreferences.getString ( "description", null ) );
        this.txtCelsius.setText ( sharedPreferences.getString ( "celsius", null ) );
        this.txtHumidity.setText ( sharedPreferences.getString ( "humidity", null ) );
        this.txtLastUpdate.setText ( sharedPreferences.getString ( "lastUpdate", null ) );
        this.txtTime.setText ( sharedPreferences.getString ( "time", null ) );
        Picasso.with ( MainActivity.this )
                .load ( sharedPreferences.getString ( "image", null ) )
                .into ( imageView );
    }


    @SuppressWarnings("MissingPermission")
    private void getLastLocation() {
        mFusedLocationClient.getLastLocation ()
                .addOnCompleteListener ( this, new OnCompleteListener <Location> () {
                    @Override
                    public void onComplete(@NonNull Task <Location> task) {
                        if (task.isSuccessful () && task.getResult () != null) {
                            mLastLocation = task.getResult ();
                            new GetWeather ().execute ( String.valueOf ( mLastLocation.getLatitude () ), String.valueOf ( mLastLocation.getLongitude () ) );
                        } else {
                            Log.w ( TAG, "getLastLocation:exception", task.getException () );
                            showSnackbar ( getString ( R.string.no_location_detected ) );
                        }
                    }
                } );
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission ( this,
                Manifest.permission.ACCESS_COARSE_LOCATION );
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale ( this,
                        Manifest.permission.ACCESS_COARSE_LOCATION );

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i ( TAG, "Displaying permission rationale to provide additional context." );

            showSnackbar ( R.string.permission_rationale, android.R.string.ok,
                    new View.OnClickListener () {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            startLocationPermissionRequest ();
                        }
                    } );

        } else {
            Log.i ( TAG, "Requesting permission" );
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            startLocationPermissionRequest ();
        }
    }

    private void startLocationPermissionRequest() {
        ActivityCompat.requestPermissions ( MainActivity.this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_CODE_PERMISSIONS );
    }


    @Override
    protected void onPause() {
        super.onPause ();
        stopLocationUpdates ();
    }

    private void stopLocationUpdates() {
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates ( mLocationCallback );
        }
    }

    @Override
    protected void onResume() {
        super.onResume ();
        if (mRequestingLocationUpdates) {
            startLocationUpdates ();
        }
    }

    /**
     * Shows a {@link Snackbar} using {@code text}.
     *
     * @param text The Snackbar text.
     */
    private void showSnackbar(final String text) {
        View container = findViewById ( R.id.main_activity_container );
        if (container != null) {
            Snackbar.make ( container, text, Snackbar.LENGTH_LONG ).show ();
        }
    }

    /**
     * Shows a {@link Snackbar}.
     *
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * @param actionStringId   The text of the action item.
     * @param listener         The listener associated with the Snackbar action.
     */
    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make ( findViewById ( android.R.id.content ),
                getString ( mainTextStringId ),
                Snackbar.LENGTH_INDEFINITE )
                .setAction ( getString ( actionStringId ), listener ).show ();
    }

    private void startLocationUpdates() {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            int fineLocationPermission = ActivityCompat.checkSelfPermission ( this, Manifest.permission.ACCESS_FINE_LOCATION );
            int coarseLocationPermission = ActivityCompat.checkSelfPermission ( this, Manifest.permission.ACCESS_COARSE_LOCATION );

            if (fineLocationPermission != PackageManager.PERMISSION_GRANTED
                    && coarseLocationPermission != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions ( myPermissions, REQUEST_CODE_PERMISSIONS
                );
                mFusedLocationClient.requestLocationUpdates ( mLocationRequest, mLocationCallback, Looper.myLooper () );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i ( TAG, "onRequestPermissionResult" );
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i ( TAG, "User interaction was cancelled." );
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
                createLocationRequest ();
                getLastLocation ();
            } else {
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                showSnackbar ( R.string.permission_denied_explanation, R.string.settings,
                        new View.OnClickListener () {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent ();
                                intent.setAction (
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS );
                                Uri uri = Uri.fromParts ( "package",
                                        BuildConfig.APPLICATION_ID, null );
                                intent.setData ( uri );
                                intent.setFlags ( Intent.FLAG_ACTIVITY_NEW_TASK );
                                startActivity ( intent );
                            }
                        } );
            }
        }
    }


    private class GetWeather extends AsyncTask <String, Integer, String> {
        ProgressDialog pd = new ProgressDialog ( MainActivity.this );

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate ( values );
            pd.setTitle ( "Please wait..." );
            pd.show ();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute ();
            pd.setTitle ( "Please wait..." );
            pd.show ();
        }

        @Override
        protected void onPostExecute(String json) {
            super.onPostExecute ( json );
            pd.dismiss ();
            if (json.contains ( "Error: Not found city" )) {
                errorLocationNotFound ();
                return;
            }
            openWeatherMap = parseWeatherJson ( json );
            updateViews ();
            saveWeatherInfo ();

        }

        @Override
        protected String doInBackground(String... strings) {
            String urlString = strings[0];
            return new Helper ().getHTTPData ( urlString );
        }

        private OpenWeatherMap parseWeatherJson(String json) {
            Gson gson = new Gson ();
            Type mType = new TypeToken <OpenWeatherMap> () {
            }.getType ();
            return gson.fromJson ( json, mType );
        }

        void errorLocationNotFound() {
            Toast.makeText ( getApplicationContext (), "Location not found!", Toast.LENGTH_SHORT ).show ();
        }

        void updateViews() {
            if (openWeatherMap.getSys ().getCountry () != null) {
                txtCity.setText ( String.format ( "%s, %s", openWeatherMap.getName (), openWeatherMap.getSys ().getCountry () ) );
                txtLastUpdate.setText ( String.format ( "Last Updated: %s", Common.getDateNow () ) );
                txtDescription.setText ( String.format ( "Weather: %s", openWeatherMap.getWeather ().get ( 0 ).getDescription () ) );
                txtHumidity.setText ( String.format ( "Humidity: %d%%", openWeatherMap.getMain ().getHumidity () ) );
                txtTime.setText ( String.format ( "Sunrise/ Sunset: %s/%s", Common.unixTimeStampToDateTime ( openWeatherMap.getSys ().getSunrise () ), Common.unixTimeStampToDateTime ( openWeatherMap.getSys ().getSunset () ) ) );
                txtCelsius.setText ( String.format ( "Temperature: %.2f °C", openWeatherMap.getMain ().getTemp () ) );
                Picasso.with ( MainActivity.this )
                        .load ( Common.getImage ( openWeatherMap.getWeather ().get ( 0 ).getIcon () ) )
                        .into ( imageView );
            } else {
                Toast.makeText ( getApplicationContext (), "Could not find location", Toast.LENGTH_SHORT ).show ();

                loadSavedWeather ();
            }
        }

        private void saveWeatherInfo() {
            SharedPreferences sharedPreferences = getApplicationContext ().getSharedPreferences ( "weatherInformation", Context.MODE_PRIVATE );
            SharedPreferences.Editor editor = sharedPreferences.edit ();
            editor.putString ( "city", String.format ( "%s, %s", openWeatherMap.getName (), openWeatherMap.getSys ().getCountry () ) );
            editor.putString ( "lastUpdate", String.format ( "Last Updated: %s", Common.getDateNow () ) );
            editor.putString ( "description", String.format ( "Weather: %s", openWeatherMap.getWeather ().get ( 0 ).getDescription () ) );
            editor.putString ( "humidity", String.format ( "Humidity: %d%%", openWeatherMap.getMain ().getHumidity () ) );
            editor.putString ( "time", String.format ( "Sunrise/ Sunset: %s/%s", Common.unixTimeStampToDateTime ( openWeatherMap.getSys ().getSunrise () ), Common.unixTimeStampToDateTime ( openWeatherMap.getSys ().getSunset () ) ) );
            editor.putString ( "celsius", String.format ( "Temperature: %.2f °C", openWeatherMap.getMain ().getTemp () ) );
            editor.putString ( "image", Common.getImage ( openWeatherMap.getWeather ().get ( 0 ).getIcon () ) );
            editor.apply ();
        }
    }
}
