package com.parkrun.main.objects;

import java.util.ArrayList;
import java.util.List;

public class User
{
    private int athleteId, DOBDay, DOBYear, runningClubId;
    private String firstName, lastName, gender, email, DOBMonth, runningClubName, parkrunName, postcode, ICEName, ICEContact, medicalInfo;
    private boolean director;
    private List<Friend> friends;

    public User()
    {
        // Required empty public constructor
    }

    public User(int athleteId, String firstName, String lastName, String email, String gender, int DOBDay, String DOBMonth,
                int DOBYear, int runningClubId, String runningClubName, String parkrunName, String postcode, String ICEName, String ICEContact, String medicalInfo, boolean director)
    {
        this.athleteId = athleteId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.gender = gender;
        this.DOBDay = DOBDay;
        this.DOBMonth = DOBMonth;
        this.DOBYear = DOBYear;
        this.runningClubId = runningClubId;
        this.runningClubName = runningClubName;
        this.parkrunName = parkrunName;
        this.postcode = postcode;
        this.ICEName = ICEName;
        this.ICEContact = ICEContact;
        this.medicalInfo = medicalInfo;
        this.friends = new ArrayList<>();
        this.director = director;
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

    public int getDOBDay()
    {
        return this.DOBDay;
    }

    public String getDOBMonth()
    {
        return this.DOBMonth;
    }

    public int getDOBYear()
    {
        return this.DOBYear;
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

    public String getICEContact()
    {
        return this.ICEContact;
    }

    public String getICEName()
    {
        return this.ICEName;
    }

    public String getMedicalInfo()
    {
        return this.medicalInfo;
    }

    public List<Friend> getFriends()
    {
        return friends;
    }

    public void setFriends(List<Friend> friends)
    {
        this.friends = friends;
    }
}