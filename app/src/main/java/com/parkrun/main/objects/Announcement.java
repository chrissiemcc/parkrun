package com.parkrun.main.objects;

public class Announcement
{
    private String date, text;

    public Announcement()
    {
        // Required empty public constructor
    }

    public Announcement(String date, String text)
    {
        this.date = date;
        this.text = text;
    }

    public String getDate()
    {
        return this.date;
    }

    public String getText()
    {
        return this.text;
    }
}
