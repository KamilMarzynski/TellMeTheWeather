package org.nachosapps.weatherapplication.Common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Kamil on 2018-01-16.
 */

public class CommonHelpers {
    private static String API_KEY = "c3e953f80e601216ce93da748ad8b969";

    private static final long HOUR_IN_MILLIS = 6000 * 60;


    @NonNull
    public static String apiRequest(String lat, String lng) {
        String API_LINK = "http://api.openweathermap.org/data/2.5/weather";
        return API_LINK + String.format("?lat=%s&lon=%s&APPID=%s&units=metric", lat, lng, API_KEY);
    }

    public static String forecastApiRequest(String lat, String lng) {
        String FORECAST_API_LINK = "http://api.openweathermap.org/data/2.5/forecast";
        return FORECAST_API_LINK + String.format("?lat=%s&lon=%s&APPID=%s&units=metric", lat,
                lng, API_KEY);
    }

    public static String unixTimeStampToDateTime(double unixTimeStamp) {
        @SuppressLint("SimpleDateFormat")
        DateFormat dateFormat = new SimpleDateFormat("HH:mm");
        Date date = new Date(convertTime(unixTimeStamp));
        return dateFormat.format(date);
    }

    public static String getDateNow() {
        @SuppressLint("SimpleDateFormat")
        DateFormat dateFormat = new SimpleDateFormat(" dd/MM HH:mm");
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
