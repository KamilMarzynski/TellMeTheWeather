package org.nachosapps.weatherapplication.Models.ForecastModel;

/**
 * Created by Kamil on 2018-02-27.
 */

public class ForecastSys {
    private String pod;

    public ForecastSys(String pod) {
        this.pod = pod;
    }

    public String getPod() {
        return pod;
    }

    public void setPod(String pod) {
        this.pod = pod;
    }
}
