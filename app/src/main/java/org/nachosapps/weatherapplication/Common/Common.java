package org.nachosapps.weatherapplication.Common;

import android.support.annotation.NonNull;

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

    @NonNull
    public static String apiRequest(String lat, String lng) {
        return API_LINK + String.format("?lat=%s&lon=%s&APPID=%s&units=metric", lat, lng, API_KEY);
    }

    public static String unixTimeStampToDateTime(double unixTimeStamp) {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm");
        Date date = new Date((long) unixTimeStamp * 1000);
        return dateFormat.format(date);
    }

    public static String getDateNow() {
        DateFormat dateFormat = new SimpleDateFormat(" dd MM yyyy HH:mm");
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static String timeOfTheDay(double sunrise, double sunset) {
        Date mSunrise = new Date ((long) (sunrise*1000));
        Date mSunset = new Date ((long) (sunset*1000));
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        double currentTimeStamp = timestamp.getTime();
        Date mNow = new java.util.Date((long) (currentTimeStamp));
        if (mNow.after(mSunrise) && mNow.before(mSunset)) {
            return "day";
        } else {
            return "night";
        }
    }

    public static String typeOfTemperature(double temp) {
        if (temp < COLD) {
            return "cold";
        } else if (temp >= COLD && temp <= HOT) {
            return "moderate";
        } else {
            return "hot";
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
}
