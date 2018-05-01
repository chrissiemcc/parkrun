package com.parkrun.main.objects;

import java.util.ArrayList;
import java.util.List;

public class parkrun
{
    private String name, lastCheckInReadDate, directorId;
    private int attendance;
    private List<Announcement> announcements;
    private List<Photo> gallery;

    public parkrun(String name, String lastCheckInReadDate, String directorId, int attendance)
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

    public String getLastCheckInReadDate()
    {
        return lastCheckInReadDate;
    }

    public List<Announcement> getAnnouncements()
    {
        return announcements;
    }

    public List<Photo> getGallery()
    {
        return gallery;
    }

    public void setAnnouncements(List <Announcement> announcements)
    {
        this.announcements = announcements;
    }

    public void setGallery(List <Photo> gallery)
    {
        this.gallery = gallery;
    }
}
