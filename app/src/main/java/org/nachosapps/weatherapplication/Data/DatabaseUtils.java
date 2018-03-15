package org.nachosapps.weatherapplication.Data;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.nachosapps.weatherapplication.Fragments.PopulateFragments;
import org.nachosapps.weatherapplication.Models.CurrentWeatherModel.OpenWeatherMap;
import org.nachosapps.weatherapplication.R;
import org.nachosapps.weatherapplication.Helpers.Weather.WeatherHelpers;

/**
 * Created by Kamil on 2018-03-06.
 */

public class DatabaseUtils {

                    /*
            Each field in database has certain formula depending on weather. The formula
            looks as follows: typeOfWeather_dayOrNight_typeOfTemperature(cold/moderate/hot).
                    Each time the view is updated call is made and users gets new description
            matching current weather and daytime. Each field in database contains more than
            one description, which is chosen randomly when it comes to update views.
                 */

    public static void connectToDatabaseAndPopulateFragments(FirebaseDatabase mDatabase,
            OpenWeatherMap openWeatherMap, final FragmentManager fragmentManager,
            final ViewPager viewPager, final Context
            context, final String[] images, final String[] temperatures, final String[] dates) {


        DatabaseReference mRef = mDatabase.getReference(openWeatherMap.getWeather().get(0)
                .getMain() + "_" + WeatherHelpers.timeOfTheDay(openWeatherMap.getSys().getSunrise(),
                openWeatherMap.getSys().getSunset()) + "_" + WeatherHelpers.typeOfTemperature(
                openWeatherMap.getMain().getTemp()));


        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String description = dataSnapshot.getValue(String.class);
                PopulateFragments.populateFragments(fragmentManager, viewPager,
                        context, description, images, temperatures, dates);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public static void connectToDatabaseAndPopulateFragments(DatabaseReference mRef,
            final FragmentManager fragmentManager,
            final ViewPager viewPager, final Context
            context, final String images, final String temperatures, final String dates,
            boolean cancelFlag) {

        if (forecastDataExist(dates, temperatures, images)) {

            final String[] datesArray = dates.split(",");
            final String[] temperaturesArray = temperatures.split(",");
            final String[] imagesArray = images.split(",");

            if (cancelFlag) {
                PopulateFragments.populateFragments(fragmentManager, viewPager, context, context.getString(R.string
                                .need_internet_connection), imagesArray, temperaturesArray,
                        datesArray);
            } else {
                mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String description = dataSnapshot.getValue(String.class);
                        PopulateFragments.populateFragments(fragmentManager,
                                viewPager, context,
                                description, imagesArray,
                                temperaturesArray, datesArray);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        PopulateFragments.populateFragments
                                (fragmentManager, viewPager, context,
                                        context.getString(R.string
                                                .need_internet_connection), imagesArray,
                                        temperaturesArray, datesArray);
                    }
                });
            }
        } else {
            if (cancelFlag) {
                PopulateFragments.populateFragments(fragmentManager,
                        viewPager, context, context.getString(R.string.need_internet_connection));
            } else {
                mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String description = dataSnapshot.getValue(String.class);
                        PopulateFragments.populateFragments(fragmentManager,
                                viewPager, context, description);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        PopulateFragments.populateFragments
                                (fragmentManager,
                                        viewPager, context,
                                        context.getString(R.string.need_internet_connection));
                    }
                });
            }

        }
    }

    private static boolean forecastDataExist(String dates, String temperatures, String images) {
        return dates != null && temperatures != null && images != null;
    }
}
