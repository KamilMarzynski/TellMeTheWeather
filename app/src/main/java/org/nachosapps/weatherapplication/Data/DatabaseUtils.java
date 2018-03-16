package org.nachosapps.weatherapplication.Data;

import static com.crashlytics.android.core.CrashlyticsCore.TAG;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.nachosapps.weatherapplication.Fragments.PopulateFragments;
import org.nachosapps.weatherapplication.Helpers.weather.WeatherHelpers;
import org.nachosapps.weatherapplication.Interfaces.DatabaseResponse;
import org.nachosapps.weatherapplication.Models.currentWeatherModel.OpenWeatherMap;
import org.nachosapps.weatherapplication.R;

/**
 * Created by Kamil on 2018-03-06.
 */

public class DatabaseUtils {

    private static FirebaseDatabase database = FirebaseDatabase.getInstance();
                    /*
            Each field in database has certain formula depending on weather. The formula
            looks as follows: typeOfWeather_dayOrNight_typeOfTemperature(cold/moderate/hot).
                    Each time the view is updated call is made and users gets new description
            matching current weather and daytime. Each field in database contains more than
            one description, which is chosen randomly when it comes to update views.
                 */


    public static DatabaseReference getReference(String typeOfWeather, String timeOfDay, String
            typeOfTemp) {
        DatabaseReference ref = database.getReference(
                typeOfWeather + "_" + timeOfDay + "_" + typeOfTemp);
        return ref;
    }

    public static void connectToDatabase(DatabaseReference ref, final DatabaseResponse delegate) {
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String description = dataSnapshot.getValue(String.class);
                Log.e(TAG, description);
                if (description != null) {
                    delegate.descriptionFetched(description);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
