package org.nachosapps.weatherapplication.Common;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.nachosapps.weatherapplication.Model.OpenWeatherMap;
import org.nachosapps.weatherapplication.R;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * Created by Kamil on 2018-01-16.
 */

public class Common {
    private static String API_KEY = "c3e953f80e601216ce93da748ad8b969";
    private static String API_LINK = "http://api.openweathermap.org/data/2.5/weather";

    private static final double COLD = 3;
    private static final double HOT = 18;
    private static final String NIGHT = "night";
    private static final String DAY = "day";
    private static final String sCOLD = "cold";
    private static final String sMODERATE = "moderate";
    private static final String sHOT = "hot";
    private static FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();


    @NonNull
    public static String apiRequest(String lat, String lng) {
        return API_LINK + String.format("?lat=%s&lon=%s&APPID=%s&units=metric", lat, lng, API_KEY);
    }

    public static String unixTimeStampToDateTime(double unixTimeStamp) {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm");
        Date date = new Date(convertTime(unixTimeStamp));
        return dateFormat.format(date);
    }

    public static String getDateNow() {
        DateFormat dateFormat = new SimpleDateFormat(" dd MM yyyy HH:mm");
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static String timeOfTheDay(double sunrise, double sunset) {
        Date mSunrise = new Date(convertTime(sunrise));
        Date mSunset = new Date(convertTime(sunset));
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        double currentTimeStamp = timestamp.getTime();
        Date mNow = new java.util.Date((long) (currentTimeStamp));
        if (mNow.after(mSunrise) && mNow.before(mSunset)) {
            return DAY;
        } else {
            return NIGHT;
        }
    }

    private static long convertTime(double timeInMilis) {
        return (long) timeInMilis * 1000;
    }

    public static String typeOfTemperature(double temp) {
        if (temp < COLD) {
            return sCOLD;
        } else if (temp >= COLD && temp <= HOT) {
            return sMODERATE;
        } else {
            return sHOT;
        }
    }

    public static String descritpionRandomizer(String description) {
        /*
        Description retrieved from database is in form " some kind of text matching current
        weather ; another text matching current weather ; ... ; ... ". In order to show other
        matching text to the user description randomizer takes full downloaded from database text
         and splits it where semicolon appears and returns random one from any amount of
         retrieved descriptions.
         */
        String[] separatedDescription = description.split(";");
        final String descriptions[] = new String[separatedDescription.length];
        for (int j = 0; j < separatedDescription.length; j++) {
            descriptions[j] = separatedDescription[j].trim();
        }
        Random generator = new Random();
        int randNumber = generator.nextInt(separatedDescription.length);
        return descriptions[randNumber];
    }

    public static void saveWeatherInfo(OpenWeatherMap openWeatherMap,
            Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                WeatherConstants.WEATHER_INFORMATION, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(WeatherConstants.CITY,
                String.format(context.getString(R.string.city_description),
                        openWeatherMap.getName(), openWeatherMap.getSys().getCountry())).apply();
        editor.putString(WeatherConstants.LAST_UPDATE,
                String.format(context.getString(R.string.update_description),
                        Common.getDateNow())).apply();
        editor.putString(WeatherConstants.CELSIUS,
                String.format(context.getString(R.string.temperature_description),
                        openWeatherMap.getMain().getTemp())).apply();
        editor.putString(WeatherConstants.DESCRIPTION,
                openWeatherMap.getWeather().get(0)
                        .getMain() + "_" + Common.timeOfTheDay(openWeatherMap.getSys()
                                .getSunrise(),
                        openWeatherMap.getSys().getSunset()) + "_" + Common.typeOfTemperature(
                        openWeatherMap.getMain().getTemp())).apply();
        Log.i(TAG,"Weather was saved to shared preferences");
    }

}
