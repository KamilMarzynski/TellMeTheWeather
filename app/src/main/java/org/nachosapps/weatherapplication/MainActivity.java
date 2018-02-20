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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.nachosapps.weatherapplication.Common.Common;
import org.nachosapps.weatherapplication.HttpClient.HttpClient;
import org.nachosapps.weatherapplication.Model.OpenWeatherMap;

import java.lang.reflect.Type;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    final int REQUEST_CODE_PERMISSIONS = 0;


    protected Location mLastLocation;
    TextView txtCity, txtLastUpdate, txtDescription, txtCelsius;
    Button btnRefresh;
    OpenWeatherMap openWeatherMap = new OpenWeatherMap();
    LocationRequest mLocationRequest;
    boolean mRequestingLocationUpdates;
    private Snackbar mSnackbar;
    boolean isSnackbarShown = false;
    FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    String[] myPermissions = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                Log.i(TAG, "Location: " + location.getLatitude() + " " + location.getLongitude());
                mLastLocation = location;
            }
            new GetWeather().execute(Common.apiRequest(
                    String.valueOf(mLastLocation.getLatitude()),
                    String.valueOf(mLastLocation.getLongitude())));
        }
    };

    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        // loadSavedWeather(); should be called here?

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        if (!checkPermissions()) {
            requestPermissions();
        } else {
            createLocationRequest();
            getLastLocation();
        }

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissSnack();
                if (!checkPermissions()) {
                    requestPermissions();
                } else {
                    if (mLastLocation != null) {
                        //Refresh weather only when there was any known location. Without this
                        // conditional statement application is crashing in rare occasions when
                        // user tries to refresh weather moments after enabling localization
                        // function.
                        createLocationRequest();
                        getLastLocation();
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string
                                .dont_know_location), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!checkPermissions()) {
            requestPermissions();
        } else {
            createLocationRequest();
            getLastLocation();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        dismissSnack();
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
        //After prompting user to allow localization onResume will be called and this should
        // allow to immediately show weather
        if (!checkPermissions()) {
            requestPermissions();
        } else {
            createLocationRequest();
            getLastLocation();
        }
    }

    private void startLocationUpdates() {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            int fineLocationPermission = ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            int coarseLocationPermission = ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION);

            if (fineLocationPermission != PackageManager.PERMISSION_GRANTED
                    && coarseLocationPermission != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(myPermissions, REQUEST_CODE_PERMISSIONS
                );
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback,
                        Looper.myLooper());
            }
        }
    }

    private void initViews() {
        txtCity = findViewById(R.id.txtCity);
        txtCelsius = findViewById(R.id.txtCelsius);
        txtDescription = findViewById(R.id.txtDescription);
        txtLastUpdate = findViewById(R.id.txtLastUpdate);
        btnRefresh = findViewById(R.id.btnRefresh);
    }

    void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(60000 * 60); // one hour interval
        mLocationRequest.setFastestInterval(60000 * 60);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback,
                    Looper.myLooper());
        }
    }

    private void loadSavedWeather() {
        SharedPreferences sharedPreferences = this.getSharedPreferences(
                getString(R.string.weather_information),
                Context.MODE_PRIVATE);

        this.txtCity.setText(sharedPreferences.getString(getString(R.string.TAG_city), null));
        this.txtCelsius.setText(sharedPreferences.getString(getString(R.string.TAG_celsius), null));
        this.txtLastUpdate.setText(
                sharedPreferences.getString(getString(R.string.TAG_lastUpdate), getString(R.string.no_data)));

        DatabaseReference mRef = mDatabase.getReference(sharedPreferences.getString(getString(R
                .string.TAG_description), "could_not_load_weather"));

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String description = dataSnapshot.getValue(String.class);
                txtDescription.setText(Common.descritpionRandomizer(description));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        Log.i("SharedPreferences", "Weather was loaded from shared preferences");
    }


    @SuppressWarnings("MissingPermission")
    private void getLastLocation() {
        mFusedLocationClient.getLastLocation()
                .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            mLastLocation = task.getResult();
                            new GetWeather().execute(String.valueOf(mLastLocation.getLatitude()),
                                    String.valueOf(mLastLocation.getLongitude()));
                        } else {
                            Log.w(TAG, "getLastLocation:exception", task.getException());
                            loadSavedWeather();
                            showSnackbar(R.string.no_location_detected, R.string.settings,
                                    new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            // Build intent that displays the App settings screen.
                                            Intent intent = new Intent();
                                            intent.setAction(
                                                    Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                            startActivity(intent);
                                        }
                                    });
                        }
                    }
                });
    }


    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION);
        loadSavedWeather();

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            showSnackbar(R.string.permission_rationale, android.R.string.ok,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            startLocationPermissionRequest();
                        }
                    });

        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            startLocationPermissionRequest();
        }
    }

    private void startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_CODE_PERMISSIONS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else // Permission denied.
// Notify the user via a SnackBar that they have rejected a core permission for the
// app, which makes the Activity useless.
// Additionally, it is important to remember that a permission might have been
// rejected without asking the user for permission (device policy or "Never ask
// again" prompts). Therefore, a user interface affordance is typically implemented
// when permissions are denied. Otherwise, your app could appear unresponsive to
// touches or interactions which have required permissions.
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted.
                    createLocationRequest();
                    getLastLocation();
                } else {
                    showSnackbar(R.string.permission_denied_explanation, R.string.settings,
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    // Build intent that displays the App settings screen.
                                    Intent intent = new Intent();
                                    intent.setAction(
                                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package",
                                            BuildConfig.APPLICATION_ID, null);
                                    intent.setData(uri);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                }
                            });
                }
        }
    }


    private void dismissSnack() {
        isSnackbarShown = false;
        if (mSnackbar != null) {
            if (mSnackbar.isShown()) {
                mSnackbar.dismiss();
            }
        }
    }

    /**
     * Shows a {@link Snackbar} using {@code text}.
     *
     * @param text The Snackbar text.
     */
    private void showSnackbar(final String text) {
        View container = findViewById(R.id.main_activity_container);
        dismissSnack();
        if (container != null && isSnackbarShown == false) {
            mSnackbar.make(container, text, Snackbar.LENGTH_LONG).show();
            isSnackbarShown = true;
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
        View container = findViewById(R.id.main_activity_container);
        if (isSnackbarShown == false) {
            mSnackbar.make(container,
                    getString(mainTextStringId),
                    Snackbar.LENGTH_LONG).setAction(getString
                    (actionStringId), listener).setDuration(18000).show();
            isSnackbarShown = true;
        }
    }


    private class GetWeather extends AsyncTask<String, Integer, String> {
        ProgressDialog pd = new ProgressDialog(MainActivity.this);

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            pd.setTitle("Please wait...");
            pd.show();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd.setTitle("Please wait...");
            pd.show();
        }

        @Override
        protected void onPostExecute(String json) {
            super.onPostExecute(json);
            pd.dismiss();
            openWeatherMap = parseWeatherJson(json);
            updateViews();
            saveWeatherInfo();
        }

        @Override
        protected String doInBackground(String... strings) {
            String urlString = strings[0];
            return new HttpClient().getHTTPData(urlString, getApplicationContext());
        }

        private OpenWeatherMap parseWeatherJson(String json) {
            Gson gson = new Gson();
            Type mType = new TypeToken<OpenWeatherMap>() {
            }.getType();
            return gson.fromJson(json, mType);
        }

        void updateViews() {
            if (openWeatherMap.getSys().getCountry() != null) {

                txtCity.setText(String.format(getString(R.string.city_description),
                        openWeatherMap.getName(),
                        openWeatherMap.getSys().getCountry()));

                txtLastUpdate.setText(
                        String.format(getString(R.string.update_description), Common.getDateNow()));

                txtCelsius.setText(
                        String.format(getString(R.string.temperature_description),
                                openWeatherMap.getMain().getTemp()));

                /*
                Each field in database has certain formula depending on weather. The formula
                looks as follows: typeOfWeather_dayOrNight_typeOfTemperature(cold/moderate/hot).
                Each time the view is updated call is made and users gets new description
                matching current weather and daytime. Each field in database contains more than
                one description, which is chosen randomly when it comes to update views.
                 */

                DatabaseReference mRef = mDatabase.getReference(openWeatherMap.getWeather().get(0)
                        .getMain() + "_" + Common.timeOfTheDay(openWeatherMap.getSys().getSunrise(),
                        openWeatherMap.getSys().getSunset()) + "_" + Common.typeOfTemperature(
                        openWeatherMap.getMain().getTemp()));

                mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String description = dataSnapshot.getValue(String.class);
                        txtDescription.setText(Common.descritpionRandomizer(description));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            } else {
                loadSavedWeather();
            }
        }

        private void saveWeatherInfo() {
            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(
                    getString(R.string.weather_information), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(getString(R.string.TAG_city),
                    String.format(getString(R.string.city_description), openWeatherMap.getName(),
                            openWeatherMap.getSys().getCountry()));
            editor.putString(getString(R.string.TAG_lastUpdate),
                    String.format(getString(R.string.update_description), Common.getDateNow()));
            editor.putString(getString(R.string.TAG_celsius),
                    String.format(getString(R.string.temperature_description),
                            openWeatherMap.getMain().getTemp()));
            editor.putString(getString(R.string.TAG_description),
                    openWeatherMap.getWeather().get(0)
                            .getMain() + "_" + Common.timeOfTheDay(openWeatherMap.getSys()
                                    .getSunrise(),
                            openWeatherMap.getSys().getSunset()) + "_" + Common.typeOfTemperature(
                            openWeatherMap.getMain().getTemp()));
        }
    }
}
