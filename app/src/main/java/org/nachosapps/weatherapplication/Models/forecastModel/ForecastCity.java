package org.nachosapps.weatherapplication.Models.forecastModel;

/**
 * Created by Kamil on 2018-02-27.
 */

public class ForecastCity {
    private int id;
    private ForecastCoord mForecastCoord;
    private String name;
    private String country;

    public ForecastCity(int id,
            ForecastCoord forecastCoord, String name, String country) {
        this.id = id;
        mForecastCoord = forecastCoord;
        this.name = name;
        this.country = country;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ForecastCoord getForecastCoord() {
        return mForecastCoord;
    }

    public void setForecastCoord(ForecastCoord forecastCoord) {
        mForecastCoord = forecastCoord;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
