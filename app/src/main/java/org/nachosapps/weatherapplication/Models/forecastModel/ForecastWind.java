package org.nachosapps.weatherapplication.Models.forecastModel;

/**
 * Created by Kamil on 2018-02-27.
 */

public class ForecastWind {
    private double speed;
    private  double deg;

    public ForecastWind(double speed, double deg) {
        this.speed = speed;
        this.deg = deg;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getDeg() {
        return deg;
    }

    public void setDeg(double deg) {
        this.deg = deg;
    }
}
