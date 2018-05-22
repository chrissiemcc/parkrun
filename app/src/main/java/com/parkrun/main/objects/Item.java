package com.parkrun.main.objects;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Item implements JSONObjectPopulator
{
    private Forecast[] forecast;

    @Override
    public void populate(JSONObject data)
    {
        JSONArray forecastData = data.optJSONArray("forecast");

        forecast = new Forecast[forecastData.length()];

        for (int i = 0; i < forecastData.length(); i++)
        {
            forecast[i] = new Forecast();
            try
            {
                forecast[i].populate(forecastData.getJSONObject(i));
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
    }

    public Forecast[] getForecast()
    {
        return this.forecast;
    }
}
