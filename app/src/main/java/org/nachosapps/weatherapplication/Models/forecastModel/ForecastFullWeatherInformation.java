package org.nachosapps.weatherapplication.Models.forecastModel;

import java.util.List;

/**
 * Created by Kamil on 2018-02-27.
 */

public class ForecastFullWeatherInformation {
    private List<ForecastWeather> weather;
    private double dt;
    private String dt_txt;
    private ForecastMain main;
    private ForecastSys sys;
    private ForecastRain rain;
    private ForecastClouds clouds;
    private ForecastWind wind;

    public ForecastFullWeatherInformation(
            List<ForecastWeather> forecastWeathers, double dt, String dt_txt,
            ForecastMain main, ForecastSys sys,
            ForecastRain rain, ForecastClouds clouds,
            ForecastWind wind) {
        weather= forecastWeathers;
        this.dt = dt;
        this.dt_txt = dt_txt;
        this.main = main;
        this.sys = sys;
        this.rain = rain;
        this.clouds = clouds;
        this.wind = wind;
    }

    public List<ForecastWeather> getForecastWeathers() {
        return weather;
    }

    public void setForecastWeathers(
            List<ForecastWeather> forecastWeathers) {
        weather = forecastWeathers;
    }

    public double getDt() {
        return dt;
    }

    public void setDt(double dt) {
        this.dt = dt;
    }

    public String getDt_txt() {
        return dt_txt;
    }

    public void setDt_txt(String dt_txt) {
        this.dt_txt = dt_txt;
    }

    public ForecastMain getMain() {
        return main;
    }

    public void setMain(ForecastMain main) {
        this.main = main;
    }

    public ForecastSys getSys() {
        return sys;
    }

    public void setSys(ForecastSys sys) {
        this.sys = sys;
    }

    public ForecastRain getRain() {
        return rain;
    }

    public void setRain(ForecastRain rain) {
        this.rain = rain;
    }

    public ForecastClouds getClouds() {
        return clouds;
    }

    public void setClouds(ForecastClouds clouds) {
        this.clouds = clouds;
    }

    public ForecastWind getWind() {
        return wind;
    }

    public void setWind(ForecastWind wind) {
        this.wind = wind;
    }
}
