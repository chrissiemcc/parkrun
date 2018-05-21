package com.parkrun.main.fragments.myparkrun;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.parkrun.main.R;
import com.parkrun.main.objects.User;
import com.parkrun.main.objects.parkrun;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MyParkrunNewsFragment extends MyParkrunMainFragment
{
    private boolean setupComplete = false, userSearchComplete = false, checkInCheckComplete = false;

    private View layout;
    private Button addButton;

    private FirebaseUser firebaseUser;
    private DatabaseReference userReference, parkrunReference;

    private User currentUser;
    private parkrun currentParkrun;

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            ProgressBar progressBar = layout.findViewById(R.id.progressBarNews);
            progressBar.setVisibility(View.INVISIBLE);

            RelativeLayout directorPanelRelative = layout.findViewById(R.id.directorPanelRelative);
            directorPanelRelative.setVisibility(View.VISIBLE);

            RelativeLayout announcementListRelative = layout.findViewById(R.id.announcementListRelative);
            announcementListRelative.setVisibility(View.VISIBLE);
        }
    };

    public MyParkrunNewsFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        layout = inflater.inflate(R.layout.fragment_my_parkrun_news, container, false);

        FirebaseAuth authentication = FirebaseAuth.getInstance();
        firebaseUser = authentication.getCurrentUser();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        parkrunReference = database.getReference("parkruns");
        userReference = database.getReference("users");

        detailSetup();

        //Log.d("Testing", currentParkrun.getDirectorId());

        //currentParkrun.setLastCheckInReadDate(Calendar.getInstance().getTime());
        //parkrunReference.child(currentParkrun.getName()).setValue(currentParkrun);

        return layout;
    }

    private void detailSetup()
    {
        Runnable setupRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                userReference.addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        Iterable<DataSnapshot> children = dataSnapshot.getChildren();

                        for(DataSnapshot child : children)
                        {
                            User user = child.getValue(User.class);

                            if(user != null && user.getEmail().equals(firebaseUser.getEmail()))
                            {
                                currentUser = user;

                                if(currentUser.getDirector())
                                {
                                    addButton = layout.findViewById(R.id.btnAddNews);
                                    addButton.setVisibility(View.VISIBLE); //if user is director, give ability to add announcements
                                }
                                userSearchComplete = true;

                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {

                    }
                });

                while(true)
                {
                    if(userSearchComplete) break;
                    //wait for user search to complete
                }

                userSearchComplete = false;

                parkrunReference.addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        Iterable<DataSnapshot> children = dataSnapshot.getChildren();

                        for(DataSnapshot child : children)
                        {
                            parkrun parkrun = child.getValue(parkrun.class);

                            if(parkrun != null && currentUser.getParkrunName().equals(parkrun.getName()))
                            {
                                currentParkrun = parkrun;
                                //PAGE DETAILS GO HERE
                                setupComplete = true;
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {

                    }
                });

                while(true)
                {
                    if(setupComplete) break;
                    //wait for parkrun search to complete
                }

                setupComplete = false;

                checkInCheck();

                handler.sendEmptyMessage(0);
            }
        };

        Thread setupThread = new Thread(setupRunnable);
        setupThread.start();
    }

    private void checkInCheck()
    {


        /*
        int index = 0;
        boolean resetCheck = false;
        Calendar parkrunStart = Calendar.getInstance();
        while (parkrunStart.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY)
        {
            parkrunStart.add(Calendar.DAY_OF_WEEK, -1);
            index++;
        }

        String[] lastCheckInRead = currentParkrun.getLastCheckInReadDate().split(" ");
        String[] lastCheckInReadTime = currentParkrun.getLastCheckInReadDate().split(":");

        @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat("EEE HH:mm dd/MM/yyyy");
        String checkIn = df.format(Calendar.getInstance().getTime());
        String[] current = checkIn.split(" ");
        String[] currentTime = current[1].split(":");
        String[] currentDate = current[2].split("/");

        if(index == 0)
        {
            if(Integer.parseInt(currentTime[0]) >= 9 && Integer.parseInt(lastCheckInReadTime[0]) <= 9)
            {
                if(Integer.parseInt(currentTime[1]) >= 30 && Integer.parseInt(lastCheckInReadTime[1]) <= 30)
                {
                    resetCheck = true;
                }
            }
        }//Today is Saturday
        else
        {
            if(lastCheckInRead[0].equals("Sat"))
            {
                if(Integer.parseInt(lastCheckInReadTime[0]) <= 9)
                {
                    if(Integer.parseInt(lastCheckInReadTime[1]) <= 30)
                    {
                        resetCheck = true;
                    }
                }
            }//Last check was Saturday
            else
            {
                if(lastCheckInRead[0].equals(current[0]) && Integer.parseInt(currentTime[0]) >= Integer.parseInt(lastCheckInReadTime[0]))
                {
                    if(Integer.parseInt(currentTime[1]) >= Integer.parseInt(lastCheckInReadTime[1]))
                    {

                    }
                    else
                    {

                    }
                }//Today and last check both same day
                switch(current[0])
                {
                    case "Mon":
                        if(!lastCheckInRead[0].equals("Sun"))
                            resetCheck = true;
                        break;
                    case "Tue":
                        if(!lastCheckInRead[0].equals("Sun") || !lastCheckInRead[0].equals("Mon"))
                            resetCheck = true;
                        break;
                    case "Wed":
                        if(!lastCheckInRead[0].equals("Sun") || !lastCheckInRead[0].equals("Mon")
                                || !lastCheckInRead[0].equals("Tue"))
                            resetCheck = true;
                        break;
                    case "Thu":
                        if(!lastCheckInRead[0].equals("Sun") || !lastCheckInRead[0].equals("Mon")
                                || !lastCheckInRead[0].equals("Tue") || !lastCheckInRead[0].equals("Wed"))
                            resetCheck = true;
                        break;
                    case "Fri":
                        if(!lastCheckInRead[0].equals("Sun") || !lastCheckInRead[0].equals("Mon")
                                || !lastCheckInRead[0].equals("Tue") || !lastCheckInRead[0].equals("Wed")
                                || !lastCheckInRead[0].equals("Thu"))
                            resetCheck = true;
                        break;
                    case "Sun":
                        if(!lastCheckInRead[0].equals("Sun"))
                            resetCheck = true;
                        break;
                    default:
                        break;
                }
            }//Last check was not Saturday
        }//Today is not Saturday







        if(resetCheck)
        {
            currentParkrun.setAttendance(0);
            parkrunReference.child(currentParkrun.getName()).setValue(currentParkrun);
        }// SET ATTENDANCE TO 0

        while(true)
        {
            if(checkInCheckComplete) break;
            //wait for check in search to complete
        }
        */
    }
}