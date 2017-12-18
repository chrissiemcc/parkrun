package com.parkrun.main;

import java.util.Random;

public class User
{
    private int athleteId;
    private String  firstName, lastName, email, password;

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
        return athleteId;
    }

    public String getFirstName()
    {
        return firstName;
    }

    public String getLastName()
    {
        return lastName;
    }

    public String getEmail()
    {
        return email;
    }

    public String getPassword()
    {
        return password;
    }

    private int generateId()
    {
        Random random = new Random();
        athleteId = 100 + random.nextInt(999);
        return athleteId;
    }
}