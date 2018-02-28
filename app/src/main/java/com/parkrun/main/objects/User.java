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

    public User(String firstName, String lastName, String email)
    {
        this.athleteId = generateId();
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

    private int generateId()
    {
        Random random = new Random();
        athleteId = random.nextInt((999 - 100) + 1) + 100;
        return athleteId;
    }

    private boolean getDirector()
    {
        return this.director;
    }
}