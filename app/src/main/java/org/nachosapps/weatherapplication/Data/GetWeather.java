package org.nachosapps.weatherapplication.Data;

import static org.nachosapps.weatherapplication.HTTPClient.HttpClient.getHTTPData;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

import org.nachosapps.weatherapplication.Interfaces.WeatherResponse;

/**
 * Created by Kamil on 2018-03-06.
 */

public class GetWeather extends AsyncTask<String, Integer, String[]> {

    private WeatherResponse delegate = null;
    @SuppressLint("StaticFieldLeak")

    public GetWeather(WeatherResponse delegate) {
        this.delegate = delegate;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String[] doInBackground(String... strings) {
        String[] HTTPCalls = new String[]{getHTTPData(strings[0]
        ), getHTTPData(strings[1])};
        if (HTTPCalls[0] != null || HTTPCalls[1] != null) {
            return HTTPCalls;
        } else {
            cancel(true);
            return null;
        }
    }


    @Override
    protected void onPostExecute(String json[]) {
        super.onPostExecute(json);
        delegate.processFinish(json);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        delegate.processFinish(null);
    }

}

