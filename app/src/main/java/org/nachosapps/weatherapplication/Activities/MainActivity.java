package org.nachosapps.weatherapplication.Activities;

import static org.nachosapps.weatherapplication.Helpers.Weather.WeatherHelpers.parseForecastJson;
import static org.nachosapps.weatherapplication.Helpers.Weather.WeatherHelpers.parseWeatherJson;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import com.transitionseverywhere.ArcMotion;
import com.transitionseverywhere.ChangeBounds;

import org.nachosapps.weatherapplication.Behaviors.MoveUpwardBehavior;
import org.nachosapps.weatherapplication.BuildConfig;
import org.nachosapps.weatherapplication.Common.CommonHelpers;
import org.nachosapps.weatherapplication.Data.DatabaseUtils;
import org.nachosapps.weatherapplication.Data.GetWeather;
import org.nachosapps.weatherapplication.Helpers.Location.LocationHelpers;
import org.nachosapps.weatherapplication.Helpers.Weather.WeatherConstants;
import org.nachosapps.weatherapplication.Helpers.Weather.WeatherHelpers;
import org.nachosapps.weatherapplication.Interfaces.WeatherResponse;
import org.nachosapps.weatherapplication.Models.CurrentWeatherModel.OpenWeatherMap;
import org.nachosapps.weatherapplication.Models.ForecastModel.OpenWeatherMapForecast;
import org.nachosapps.weatherapplication.R;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    final int REQUEST_CODE_PERMISSIONS = 0;


    public Location mLastLocation;
    ImageView mainWeatherImage;
    TextView txtCity, txtLastUpdate, txtCelsius;
    TextView txtHumidity, txtSunrise, txtSunset, txtWind, txtPressure;
    FloatingActionButton fab;
    TabLayout tabLayout;
    boolean isFirstCreate;
    ViewPager viewPager;
    AppBarLayout mAppBarLayout;
    RelativeLayout detailWeather;
    CoordinatorLayout mainLayout;
    RelativeLayout upperLayout;
    RelativeLayout fabLayout;
    ProgressBar fabProgress;
    Handler delayHandler;
    boolean cancelFlag = false; //to check if async task GetWeather was cancelled
    RelativeLayout weatherLayout;
    OpenWeatherMap openWeatherMap = new OpenWeatherMap();
    OpenWeatherMapForecast openWeatherMapForecast = new OpenWeatherMapForecast();
    LocationRequest mLocationRequest;
    NestedScrollView mNestedScrollView;
    CollapsingToolbarLayout collapsingToolbar;
    boolean mRequestingLocationUpdates;
    private Snackbar mSnackbar;
    boolean isSnackbarShown = false;
    public static final int NUMBER_OF_FORECAST = 7;
    private static final long HOUR_IN_MILIS = 6000 * 60;
    public FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    String[] myPermissions = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                mLastLocation = location;
            }
        }
    };

    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);


        initViews();

        initLocationClient();

        if (!checkPermissions()) {
            requestPermissions();
        } else {
            LocationHelpers.createLocationRequest(mLocationCallback, this, mFusedLocationClient);
            getLastLocationAndShowWeather();
        }

    }

    private void showDetailsAndHideFab() {
        detailWeather.animate().alpha(1).setDuration(250);
        fab.animate().alpha(0).setDuration(250);
        fabProgress.animate().alpha(0).setDuration(250);
        fab.setClickable(false);
    }

    private void hideDetailsAndShowFab() {
        detailWeather.animate().alpha(0.2f).setDuration(250);
        fab.animate().alpha(1).setDuration(250);
        fabProgress.animate().alpha(1).setDuration(250);
        fab.setClickable(true);
    }

    private boolean isCollapsed(int verticalOffset) {
        return collapsingToolbar.getHeight() + verticalOffset
                < collapsingToolbar.getScrimVisibleHeightTrigger();
    }


    private void setViewsVisible(View... views) {
        for (View v : views) {
            v.setVisibility(View.VISIBLE);
        }
    }


    private void setViewsInvisible(View... views) {
        for (View v : views) {
            v.setVisibility(View.INVISIBLE);
        }
    }

    private void initLocationClient() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
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
        isFirstCreate = true;
        collapsingToolbar = findViewById(R.id.collapsingToolbar);
        detailWeather = findViewById(R.id.detailWeather);
        detailWeather.setAlpha(0.2f);
        fabLayout = findViewById(R.id.fabLayout);
        mainWeatherImage = findViewById(R.id.imageWeatherMain);
        mainLayout = findViewById(R.id.main_activity_container);
        weatherLayout = findViewById(R.id.weatherLayout);


        upperLayout = findViewById(R.id.upperLayout);
        txtCity = findViewById(R.id.txtCity);
        txtCelsius = findViewById(R.id.txtCelsius);
        fab = findViewById(R.id.fab);
        fab.setClickable(false);
        txtLastUpdate = findViewById(R.id.txtLastUpdate);

        viewPager = findViewById(R.id.viewpager);


        tabLayout = findViewById(R.id.sliding_tabs);
        tabLayout.setTranslationX(1000);
        tabLayout.setupWithViewPager(viewPager);

        mAppBarLayout = findViewById(R.id.mainAppbar);


        mNestedScrollView = findViewById(R.id.scroller);


        txtHumidity = findViewById(R.id.txtHumidity);
        txtWind = findViewById(R.id.txtWind);
        txtSunrise = findViewById(R.id.txtSunrise);
        txtSunset = findViewById(R.id.txtSunset);
        txtPressure = findViewById(R.id.txtPressure);

        fabProgress = findViewById(R.id.fabProgress);

        setViewsInvisible(detailWeather, weatherLayout, viewPager, mAppBarLayout,
                mNestedScrollView);
        setViewsVisible(fabProgress);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!checkPermissions()) {
                    requestPermissions();
                } else {

                    setViewsVisible(fabProgress);

                    TransitionManager.beginDelayedTransition(mainLayout, new Slide().setDuration
                            (250));

                    setViewsInvisible(weatherLayout, viewPager, detailWeather);

                    delayHandler = new Handler();

                    delayHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            LocationHelpers.createLocationRequest(mLocationCallback,
                                    getApplicationContext(),
                                    mFusedLocationClient);
                            getLastLocationAndShowWeather();
                            try {
                                viewPager.getAdapter().notifyDataSetChanged();
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }

                        }
                    }, 400);
                }
            }
        });

        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (isCollapsed(verticalOffset)) {
                    showDetailsAndHideFab();
                } else {
                    hideDetailsAndShowFab();
                }
            }
        });
    }

    void updateViews() {
        if (openWeatherMap == null) {
            loadSavedWeather();
        } else {

            if (isFirstCreate) {
                onFirstCreate();
            }

            TransitionManager.beginDelayedTransition(mainLayout, new Slide().setInterpolator
                    (new FastOutSlowInInterpolator()));

            setViewsInvisible(fabProgress);

            setViewsVisible(weatherLayout, viewPager, detailWeather);
            Log.e(TAG, "visible");

            txtLastUpdate.setText(
                    String.format(getString(R.string.update_description),
                            CommonHelpers.getDateNow()));

            txtCelsius.setText(String.format(getString(R.string.temperature_description),
                    openWeatherMap.getMain().getTemp()));

            Picasso.with(MainActivity.this).load(CommonHelpers.getImage(openWeatherMap.getWeather()
                    .get(0).getIcon(), MainActivity.this)).error(R.drawable.ierror).into(
                    mainWeatherImage);

            txtCity.setText(String.format(getString(R.string.city_description),
                    openWeatherMap.getName(),
                    openWeatherMap.getSys().getCountry()));


            txtHumidity.setText(String.format(getString(R.string.humidity_description),
                    openWeatherMap.getMain().getHumidity()));

            txtWind.setText(String.format(getString(R.string.wind_description),
                    openWeatherMap.getWind().getSpeed()));

            txtSunrise.setText(CommonHelpers.unixTimeStampToDateTime(openWeatherMap.getSys()
                    .getSunrise()));
            txtSunset.setText(CommonHelpers.unixTimeStampToDateTime(openWeatherMap.getSys()
                    .getSunset()));
            txtPressure.setText(String.format(getString(R.string.pressure_description),
                    openWeatherMap.getMain().getPressure()));


            final String images[] = WeatherHelpers.prepareImages(NUMBER_OF_FORECAST,
                    openWeatherMapForecast);
            final String dates[] = WeatherHelpers.prepareDates(NUMBER_OF_FORECAST,
                    openWeatherMapForecast);
            final String temperatures[] = WeatherHelpers.prepareTemperatures
                    (NUMBER_OF_FORECAST, openWeatherMapForecast, getApplicationContext());

            DatabaseUtils.connectToDatabaseAndPopulateFragments(mDatabase, openWeatherMap,
                    getSupportFragmentManager(), viewPager, this, images, temperatures, dates);
            fab.setClickable(true);


        }
    }

    private void loadSavedWeather() {
        SharedPreferences sharedPreferences = this.getSharedPreferences(
                WeatherConstants.WEATHER_INFORMATION,
                Context.MODE_PRIVATE);


        if (isFirstCreate) {
            onFirstCreate();
        }

        setViewsInvisible(fabProgress);
        TransitionManager.beginDelayedTransition(mainLayout, new Slide().setInterpolator(new
                FastOutSlowInInterpolator()));
        setViewsVisible(weatherLayout, viewPager, detailWeather);

        this.txtCity.setText(sharedPreferences.getString(WeatherConstants.CITY, null));
        this.txtCelsius.setText(sharedPreferences.getString(WeatherConstants.CELSIUS, null));
        this.txtLastUpdate.setText(
                sharedPreferences.getString(WeatherConstants.LAST_UPDATE,
                        getString(R.string.no_data)));
        Picasso.with(this).load(CommonHelpers.getImage(sharedPreferences.getString(WeatherConstants
                .ICON, "error"), this)).error(R.drawable
                .ierror).into(mainWeatherImage);

        DatabaseReference mRef = mDatabase.getReference(sharedPreferences.getString
                (WeatherConstants.DESCRIPTION, "could_not_load_weather"));


        String dates = sharedPreferences.getString(WeatherConstants.DATES, null);
        String temperatures = sharedPreferences.getString(WeatherConstants.TEMPERATURES, null);
        String images = sharedPreferences.getString(WeatherConstants.IMAGES, null);


        DatabaseUtils.connectToDatabaseAndPopulateFragments(mRef, getSupportFragmentManager(),
                viewPager, this, images, temperatures, dates, cancelFlag);

        loadDetailedWeatherView(weatherLayout, sharedPreferences);
        fab.setClickable(true);

    }

    void onFirstCreate() {
        int cx = mainLayout.getWidth() / 2;
        int cy = mainLayout.getHeight() / 2;

        float finalRadius = (float) Math.hypot(cx, cy);

        Animator anim =
                ViewAnimationUtils.createCircularReveal(mainLayout, cx, cy, 78,
                        finalRadius)
                        .setDuration(600);

        setViewsVisible(mAppBarLayout, mNestedScrollView);

        anim.start();

        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                animateFabArcMotion();

                tabLayout.animate().translationX(0).setDuration(300);
                TransitionManager.beginDelayedTransition(mainLayout, new Slide().setDuration(300));
                setViewsVisible(weatherLayout, viewPager);


            }
        });

        isFirstCreate = false;

    }

    private void animateFabArcMotion() {
        com.transitionseverywhere.TransitionManager.beginDelayedTransition(
                mainLayout,
                new ChangeBounds().setPathMotion(new ArcMotion()).setDuration(500));

        CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams(
                CoordinatorLayout.LayoutParams.WRAP_CONTENT, CoordinatorLayout
                .LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.BOTTOM | Gravity.END;
        params.setBehavior(new MoveUpwardBehavior());
        params.setMargins(16, 16, 16, 16);
        fabLayout.setLayoutParams(params);
    }

    void loadDetailedWeatherView(RelativeLayout detailWeather,
            SharedPreferences sharedPreferences) {

        String humidity = sharedPreferences.getString(WeatherConstants.HUMIDITY, null);
        String pressure = sharedPreferences.getString(WeatherConstants.PRESSURE, null);
        String sunrise = sharedPreferences.getString(WeatherConstants.SUNRISE, null);
        String sunset = sharedPreferences.getString(WeatherConstants.SUNSET, null);
        String wind = sharedPreferences.getString(WeatherConstants.WIND, null);


        if (isDetailedInfoSaved(sharedPreferences)) {
            txtPressure.setText(pressure);
            txtHumidity.setText(humidity);
            txtSunset.setText(sunset);
            txtSunrise.setText(sunrise);
            txtWind.setText(wind);
        } else {
            detailWeather.setVisibility(View.INVISIBLE);
        }

    }

    boolean isDetailedInfoSaved(SharedPreferences sharedPreferences) {
        String humidity = sharedPreferences.getString(WeatherConstants.HUMIDITY, null);
        String pressure = sharedPreferences.getString(WeatherConstants.PRESSURE, null);
        String sunrise = sharedPreferences.getString(WeatherConstants.SUNRISE, null);
        String sunset = sharedPreferences.getString(WeatherConstants.SUNSET, null);
        String wind = sharedPreferences.getString(WeatherConstants.WIND, null);

        return humidity != null && pressure != null && sunrise != null && sunset != null && wind !=
                null;

    }

    @SuppressWarnings("MissingPermission")
    private void getLastLocationAndShowWeather() {
        mFusedLocationClient.getLastLocation()
                .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            mLastLocation = task.getResult();
                            new GetWeather(new WeatherResponse() {
                                @Override
                                public void processFinish(String[] output) {
                                    if (output != null) {
                                        openWeatherMap = parseWeatherJson(output[0]);
                                        openWeatherMapForecast = parseForecastJson(output[1]);
                                        WeatherHelpers.saveWeatherInfo(openWeatherMap,
                                                openWeatherMapForecast, getApplicationContext());
                                        updateViews();
                                    } else {
                                        cancelFlag = true;
                                        showSnack(R.string.no_internet, R.string.settings,
                                                new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        // Build intent that displays the App
                                                        // settings screen.
                                                        Intent intent = new Intent();
                                                        intent.setAction(
                                                                Settings.ACTION_WIRELESS_SETTINGS);
                                                        startActivity(intent);
                                                    }
                                                });
                                        loadSavedWeather();
                                    }
                                }
                            }).execute(CommonHelpers
                                            .apiRequest(
                                                    String.valueOf(mLastLocation.getLatitude()),
                                                    String.valueOf(mLastLocation.getLongitude())),
                                    CommonHelpers.forecastApiRequest(
                                            String.valueOf(mLastLocation.getLatitude()),
                                            String.valueOf(mLastLocation.getLongitude())));
                        } else {
                            loadSavedWeather();
                            showSnack(R.string.no_location_detected, R.string.settings,
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

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            showSnack(R.string.permission_rationale, android.R.string.ok,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            startLocationPermissionRequest();
                        }
                    });

        } else {
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
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
                LocationHelpers.createLocationRequest(mLocationCallback, this,
                        mFusedLocationClient);
                getLastLocationAndShowWeather();
            } else {
                showSnack(R.string.permission_denied_explanation, R.string.settings,
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
     * Shows a {@link Snackbar}.
     *
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * @param actionStringId   The text of the action item.
     * @param listener         The listener associated with the Snackbar action.
     */
    private void showSnack(final int mainTextStringId, final int actionStringId,
            View.OnClickListener listener) {
        View container = findViewById(R.id.main_activity_container);
        if (!isSnackbarShown) {
            Snackbar.make(container,
                    getString(mainTextStringId),
                    Snackbar.LENGTH_LONG).setAction(getString
                    (actionStringId), listener).setDuration(18000).show();
            isSnackbarShown = true;
        }
    }


}

