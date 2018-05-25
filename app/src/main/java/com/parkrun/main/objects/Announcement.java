package com.parkrun.main.objects;

import java.util.Date;

public class Announcement
{
    private String text;
    private Date date;

    public Announcement()
    {
        // Required empty public constructor
    }

    public Announcement(Date date, String text)
    {
        this.date = date;
        this.text = text;
    }

    public Date getDate()
    {
        return this.date;
    }

    public String getText()
    {
        return this.text;
    }
}
