package org.nachosapps.weatherapplication.Common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Kamil on 2018-01-16.
 */

public class CommonHelpers {
    private static String WEATHER_API_KEY = "c3e953f80e601216ce93da748ad8b969";

    private static final long HOUR_IN_MILLIS = 6000 * 60;


    @NonNull
    public static String weatherRequestUrl(String lat, String lng) {
        String WEATHER_API_LINK = "http://api.openweathermap.org/data/2.5/weather";
        return WEATHER_API_LINK + String.format("?lat=%s&lon=%s&APPID=%s&units=metric", lat, lng,
                WEATHER_API_KEY);
    }

    public static String forecastRequestUrl(String lat, String lng) {
        String FORECAST_API_LINK = "http://api.openweathermap.org/data/2.5/forecast";
        return FORECAST_API_LINK + String.format("?lat=%s&lon=%s&APPID=%s&units=metric", lat,
                lng, WEATHER_API_KEY);
    }

    public static String unixTimeStampToDateTime(double unixTimeStamp) {
        @SuppressLint("SimpleDateFormat")
        DateFormat dateFormat = new SimpleDateFormat("HH:mm");
        Date date = new Date(convertTime(unixTimeStamp));
        return dateFormat.format(date);
    }

    public static String getDateNow() {
        DateFormat dateFormat = new SimpleDateFormat(" dd/MM HH:mm", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }


    public static long convertTime(double timeInMilis) {
        return (long) timeInMilis * 1000;
    }


    public static int getImage(String icon, Context context) {
        Resources resources = context.getResources();
        return resources.getIdentifier("i" + icon, "drawable", context.getPackageName());
    }

}
