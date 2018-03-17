package com.parkrun.main.objects;

import java.util.Random;

public class User
{
    private int athleteId;
    private String firstName, lastName, email;
    private boolean director;

    public User()
    {
        // Required empty public constructor
    }

    public User(int athleteId, String firstName, String lastName, String email)
    {
        this.athleteId = athleteId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.director = false;
    }

    public int getAthleteId()
    {
        return this.athleteId;
    }

    public String getFirstName()
    {
        return this.firstName;
    }

    public String getLastName()
    {
        return this.lastName;
    }

    public String getEmail()
    {
        return this.email;
    }

    private boolean getDirector()
    {
        return this.director;
    }
}