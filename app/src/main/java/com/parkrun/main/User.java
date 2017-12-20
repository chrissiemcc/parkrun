package com.parkrun.main;

import java.util.Random;

public class User
{
    private int athleteId;
    private String firstName, lastName, email, password;

    public User(){}

    public User(String firstName, String lastName, String email, String password)
    {
        this.athleteId = generateId();
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
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

    public String getPassword()
    {
        return this.password;
    }

    private int generateId()
    {
        Random random = new Random();
        athleteId = 100 + random.nextInt(999);
        return athleteId;
    }
}