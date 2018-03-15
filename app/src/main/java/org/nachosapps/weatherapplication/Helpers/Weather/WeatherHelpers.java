package org.nachosapps.weatherapplication.Helpers.Weather;

import static org.nachosapps.weatherapplication.Activities.MainActivity.NUMBER_OF_FORECAST;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.nachosapps.weatherapplication.Common.CommonHelpers;
import org.nachosapps.weatherapplication.Models.ForecastModel.OpenWeatherMapForecast;
import org.nachosapps.weatherapplication.Models.CurrentWeatherModel.OpenWeatherMap;
import org.nachosapps.weatherapplication.R;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Random;

/**
 * Created by Kamil on 2018-02-28.
 */

public class WeatherHelpers {

    private static final double COLD = 3;
    private static final double HOT = 18;
    private static final String NIGHT = "night";
    private static final String DAY = "day";
    private static final String sCOLD = "cold";
    private static final String sMODERATE = "moderate";
    private static final String sHOT = "hot";

    public static void saveWeatherInfo(OpenWeatherMap openWeatherMap, OpenWeatherMapForecast
            mOpenWeatherMapForecast, Context context) {
        if (openWeatherMap != null && mOpenWeatherMapForecast != null) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(
                    WeatherConstants.WEATHER_INFORMATION, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(WeatherConstants.CITY,
                    String.format(context.getString(R.string.city_description),
                            openWeatherMap.getName(),
                            openWeatherMap.getSys().getCountry())).apply();
            editor.putString(WeatherConstants.LAST_UPDATE,
                    String.format(context.getString(R.string.update_description),
                            CommonHelpers.getDateNow())).apply();
            editor.putString(WeatherConstants.CELSIUS,
                    String.format(context.getString(R.string.temperature_description),
                            openWeatherMap.getMain().getTemp())).apply();
            editor.putString(WeatherConstants.DESCRIPTION,
                    openWeatherMap.getWeather().get(0)
                            .getMain() + "_" + timeOfTheDay(openWeatherMap.getSys()
                                    .getSunrise(),
                            openWeatherMap.getSys().getSunset()) + "_" + typeOfTemperature(
                            openWeatherMap.getMain().getTemp())).apply();
            editor.putString(WeatherConstants.ICON,
                    openWeatherMap.getWeather().get(0).getIcon()).apply();

            editor.putString(WeatherConstants.SUNRISE,
                    CommonHelpers.unixTimeStampToDateTime(openWeatherMap.getSys()
                            .getSunrise())).apply();

            editor.putString(WeatherConstants.SUNSET, CommonHelpers.unixTimeStampToDateTime(openWeatherMap
                    .getSys()
                    .getSunset())).apply();

            editor.putString(WeatherConstants.HUMIDITY, String.format(context.getString(R.string
                    .humidity_description), openWeatherMap.getMain()
                    .getHumidity())).apply();

            editor.putString(WeatherConstants.PRESSURE, String.format(context.getString(R.string
                    .pressure_description), openWeatherMap.getMain()
                    .getPressure())).apply();

            editor.putString(WeatherConstants.WIND, String.format(context.getString(R.string
                    .wind_description), openWeatherMap.getWind().getSpeed())).apply();

            editor.putString(WeatherConstants.TIME_OF_THE_DAY,WeatherHelpers.timeOfTheDay(openWeatherMap.getSys().getSunrise(),
                    openWeatherMap.getSys().getSunset())).apply();

            final String images[] = WeatherHelpers.prepareImages(NUMBER_OF_FORECAST,
                    mOpenWeatherMapForecast);
            final String dates[] = WeatherHelpers.prepareDates(NUMBER_OF_FORECAST,
                    mOpenWeatherMapForecast);
            final String temperatures[] = WeatherHelpers.prepareTemperatures
                    (NUMBER_OF_FORECAST, mOpenWeatherMapForecast, context);

            StringBuilder sbImages = new StringBuilder();
            StringBuilder sbDates = new StringBuilder();
            StringBuilder sbTemperatures = new StringBuilder();

            for (int i = 0; i < images.length; i++) {
                sbImages.append(images[i]).append(",");
                sbDates.append(dates[i]).append(",");
                sbTemperatures.append(temperatures[i]).append(",");
            }

            editor.putString(WeatherConstants.DATES, sbDates.toString()).apply();
            editor.putString(WeatherConstants.IMAGES, sbImages.toString()).apply();
            editor.putString(WeatherConstants.TEMPERATURES, sbTemperatures.toString()).apply();

        }
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

    public static String descriptionPicker(String description, Context mContext) {
        /*
        Description retrieved from database is in form " some kind of text matching current
        weather ; another text matching current weather ; ... ; ... ". In order to show other
        matching text to the user description randomizer takes full downloaded from database text
         and splits it where semicolon appears and returns random one from any amount of
         retrieved descriptions.
         */
        if(description != null) {
            String[] separatedDescription = description.split(";");
            final String descriptions[] = new String[separatedDescription.length];
            for (int j = 0; j < separatedDescription.length; j++) {
                descriptions[j] = separatedDescription[j].trim();
            }
            Random generator = new Random();
            int randNumber = generator.nextInt(separatedDescription.length);
            return descriptions[randNumber];
        }else
            return mContext.getString(R.string.dontKnowWeather);
    }


    public static String timeOfTheDay(double sunrise, double sunset) {
        Date mSunrise = new Date(CommonHelpers.convertTime(sunrise));
        Date mSunset = new Date(CommonHelpers.convertTime(sunset));
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        double currentTimeStamp = timestamp.getTime();
        Date mNow = new java.util.Date((long) (currentTimeStamp));
        if (mNow.after(mSunrise) && mNow.before(mSunset)) {
            return DAY;
        } else {
            return NIGHT;
        }
    }


    public static String[] prepareImages(int numberOfForecast,
            OpenWeatherMapForecast mOpenWeatherMapForecast) {
        String[] images = new String[numberOfForecast];
        for (int i = 0; i < numberOfForecast; i++) {
            images[i] = mOpenWeatherMapForecast.getWeatherList().get(
                    i).getForecastWeathers().get
                    (0).getIcon();
        }
        return images;
    }

    public static String[] prepareDates(int numberOfForecast,
            OpenWeatherMapForecast mOpenWeatherMapForecast) {
        String[] dates = new String[numberOfForecast];
        for (int i = 0; i < numberOfForecast; i++) {
            dates[i] = mOpenWeatherMapForecast.getWeatherList().get(i).getDt_txt();
        }
        return dates;
    }

    public static String[] prepareTemperatures(int numberOfForecast,
            OpenWeatherMapForecast mOpenWeatherMapForecast, Context context) {
        String[] temperatures = new String[numberOfForecast];
        for (int i = 0; i < numberOfForecast; i++) {
            temperatures[i] = String.format(context.getString(R.string
                            .temperature_description),
                    mOpenWeatherMapForecast.getWeatherList().get(
                            i).getMain().getTemp());
        }
        return temperatures;
    }

    public static OpenWeatherMap parseWeatherJson(String json) {
        Gson gson = new Gson();
        Type mType = new TypeToken<OpenWeatherMap>() {
        }.getType();
        return gson.fromJson(json, mType);
    }

    public static OpenWeatherMapForecast parseForecastJson(String json) {
        Gson gson = new Gson();
        Type mType = new TypeToken<OpenWeatherMapForecast>() {
        }.getType();
        return gson.fromJson(json, mType);
    }

}
