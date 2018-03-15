package org.nachosapps.weatherapplication.Models.ForecastModel;

import java.util.List;

/**
 * Created by Kamil on 2018-02-27.
 */

public class OpenWeatherMapForecast {
    private List<ForecastFullWeatherInformation> list;
    private ForecastCity city;
    private int cod;
    private double message;
    private int cnt;

    public List<ForecastFullWeatherInformation> getWeatherList() {
        return list;
    }

    public void setWeatherList(
            List<ForecastFullWeatherInformation> weatherList) {
        list = weatherList;
    }

    public ForecastCity getCity() {
        return city;
    }

    public void setCity(ForecastCity city) {
        city = city;
    }

    public int getCod() {
        return cod;
    }

    public void setCod(int cod) {
        this.cod = cod;
    }

    public double getMessage() {
        return message;
    }

    public void setMessage(double message) {
        this.message = message;
    }

    public int getCnt() {
        return cnt;
    }

    public void setCnt(int cnt) {
        this.cnt = cnt;
    }
}
