package org.nachosapps.weatherapplication.Common;

import android.support.annotation.NonNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Kamil on 2018-01-16.
 */

public class Common {
    private static String API_KEY = "c3e953f80e601216ce93da748ad8b969";
    private static String API_LINK = "http://api.openweathermap.org/data/2.5/weather";

    @NonNull
    public static String apiRequest(String lat, String lng){
        return API_LINK + String.format("?lat=%s&lon=%s&APPID=%s&units=metric", lat, lng, API_KEY);
    }

    public static String unixTimeStampToDateTime(double unixTimeStamp){
        DateFormat dateFormat = new SimpleDateFormat ( "HH:mm" );
        Date date = new Date ( (long)unixTimeStamp*1000 );
        return dateFormat.format ( date );
    }

    public static String getImage(String icon){
        return String.format ( "http://openweathermap.org/img/w/%s.png", icon );
    }

    public static String getDateNow(){
        DateFormat dateFormat = new SimpleDateFormat ( " dd MMMMM yyyy HH:mm" );
        Date date = new Date();
        return dateFormat.format ( date );
    }
}
