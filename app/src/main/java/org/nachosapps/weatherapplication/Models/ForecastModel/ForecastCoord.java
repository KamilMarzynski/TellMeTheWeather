package org.nachosapps.weatherapplication.Models.ForecastModel;

/**
 * Created by Kamil on 2018-02-27.
 */

public class ForecastCoord {
    private double lat;
    private double lon;

    public ForecastCoord(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }
}
