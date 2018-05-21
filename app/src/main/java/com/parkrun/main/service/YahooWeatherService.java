package com.parkrun.main.service;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

public class YahooWeatherService
{
    private WeatherServiceCallback callback;
    private String location;

    public YahooWeatherService(WeatherServiceCallback callback)
    {
        this.callback = callback;
    }

    public void setLocation(String location)
    {
        this.location = location;
    }

    @SuppressLint("StaticFieldLeak")
    public void refreshWeather(String location)
    {
        new AsyncTask<String, Void, String>()
        {
            @Override
            protected String doInBackground(String... strings)
            {
                return null;
            }

            @Override
            protected void onPostExecute(String s)
            {
                super.onPostExecute(s);
            }
        }.execute(location);
    }
}