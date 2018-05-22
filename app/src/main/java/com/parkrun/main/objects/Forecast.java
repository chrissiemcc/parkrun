package com.parkrun.main.objects;

import org.json.JSONObject;

public class Forecast implements JSONObjectPopulator
{
    private String day, description;
    private int code;

    @Override
    public void populate(JSONObject data)
    {
        day = data.optString("day");
        description = data.optString("text");
        code = data.optInt("code");
    }

    public String getDay()
    {
        return this.day;
    }

    public String getDescription()
    {
        return this.description;
    }

    public int getCode()
    {
        return this.code;
    }
}