package com.parkrun.main.objects;

import java.util.ArrayList;
import java.util.List;

public class User
{
    private int athleteId, dobday, dobyear, runningClubId;
    private String firstName, lastName, gender, email, dobmonth, runningClubName, parkrunName, postcode, icename, icecontact, medicalInfo;
    private boolean director, checkedIn, weatherNotify, volunteering;
    private List<Friend> friends;

    public User()
    {
        // Required empty public constructor
    }

    public User(int athleteId, String firstName, String lastName, String email, String gender, int dobday, String dobmonth,
                int dobyear, int runningClubId, String runningClubName, String parkrunName, String postcode, String icename, String icecontact,
                String medicalInfo, boolean director, boolean checkedIn, boolean weatherNotify, boolean volunteering)
    {
        this.athleteId = athleteId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.gender = gender;
        this.dobday = dobday;
        this.dobmonth = dobmonth;
        this.dobyear = dobyear;
        this.runningClubId = runningClubId;
        this.runningClubName = runningClubName;
        this.parkrunName = parkrunName;
        this.postcode = postcode;
        this.icename = icename;
        this.icecontact = icecontact;
        this.medicalInfo = medicalInfo;
        this.friends = new ArrayList<>();
        this.director = director;
        this.checkedIn = checkedIn;
        this.weatherNotify = weatherNotify;
        this.volunteering = volunteering;
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

    public boolean getDirector()
    {
        return this.director;
    }

    public String getGender()
    {
        return this.gender;
    }

    public int getDobday()
    {
        return this.dobday;
    }

    public String getDobmonth()
    {
        return this.dobmonth;
    }

    public int getDobyear()
    {
        return this.dobyear;
    }

    public String getRunningClubName()
    {
        return this.runningClubName;
    }

    public int getRunningClubId()
    {
        return this.runningClubId;
    }

    public String getParkrunName()
    {
        return this.parkrunName;
    }

    public String getPostcode()
    {
        return this.postcode;
    }

    public String getIcecontact()
    {
        return this.icecontact;
    }

    public String getIcename()
    {
        return this.icename;
    }

    public String getMedicalInfo()
    {
        return this.medicalInfo;
    }

    public List<Friend> getFriends()
    {
        return this.friends;
    }

    public boolean getCheckedIn()
    {
        return this.checkedIn;
    }

    public boolean getVolunteering()
    {
        return this.volunteering;
    }

    public void setFriends(List<Friend> friends)
    {
        this.friends = friends;
    }

    public void setCheckedIn(boolean checkedIn)
    {
        this.checkedIn = checkedIn;
    }

    public boolean getWeatherNotify()
    {
        return this.weatherNotify;
    }

    public void setWeatherNotify(boolean weatherNotify)
    {
        this.weatherNotify = weatherNotify;
    }

    public void setVolunteering(boolean volunteering)
    {
        this.volunteering = volunteering;
    }

    public void setDirector(boolean director)
    {
        this.director = director;
    }
}