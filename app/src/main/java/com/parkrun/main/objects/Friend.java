package com.parkrun.main.objects;

public class Friend
{
    private String name;
    private int athleteId;

    public Friend()
    {
        // Required empty public constructor
    }

    public Friend(String name, int athleteId)
    {
        this.name = name;
        this.athleteId = athleteId;
    }

    public int getAthleteId()
    {
        return athleteId;
    }

    public String getName()
    {
        return name;
    }
}