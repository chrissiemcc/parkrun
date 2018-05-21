package com.parkrun.main.objects;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class parkrun
{
    private String name, directorId;
    private Date lastCheckInReadDate;
    private int attendance;
    private List<Announcement> announcements;
    private List<Photo> gallery;

    public parkrun()
    {
        // Required empty public constructor
    }

    public parkrun(String name, Date lastCheckInReadDate, String directorId, int attendance)
    {
        this.name = name;
        this.directorId = directorId;
        this.attendance = attendance;
        this.lastCheckInReadDate = lastCheckInReadDate;
        this.announcements = new ArrayList<>();
        this.gallery = new ArrayList<>();
    }

    public int getAttendance()
    {
        return this.attendance;
    }

    public String getName()
    {
        return this.name;
    }

    public String getDirectorId()
    {
        return this.directorId;
    }

    public Date getLastCheckInReadDate()
    {
        return this.lastCheckInReadDate;
    }

    public List<Announcement> getAnnouncements()
    {
        return this.announcements;
    }

    public List<Photo> getGallery()
    {
        return this.gallery;
    }

    public void setAnnouncements(List <Announcement> announcements)
    {
        this.announcements = announcements;
    }

    public void setGallery(List <Photo> gallery)
    {
        this.gallery = gallery;
    }

    public void setAttendance(int attendance)
    {
        this.attendance = attendance;
    }

    public void setLastCheckInReadDate(Date lastCheckInReadDate)
    {
        this.lastCheckInReadDate = lastCheckInReadDate;
    }
}