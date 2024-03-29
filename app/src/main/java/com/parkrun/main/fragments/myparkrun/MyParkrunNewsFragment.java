package com.parkrun.main.fragments.myparkrun;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.parkrun.main.R;
import com.parkrun.main.objects.Announcement;
import com.parkrun.main.objects.Parkrun;
import com.parkrun.main.objects.User;
import com.parkrun.main.util.UtilAlertDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MyParkrunNewsFragment extends MyParkrunMainFragment
{
    private boolean parkrunSetupComplete = false, userSearchComplete = false, checkIn = false, checkInSetupComplete = false;

    private RelativeLayout announcementList;
    private TableLayout announcementTable;

    private View layout;
    private Button addButton, checkInButton, refreshButton;
    private TextView tvCheckInDetails, tvNoAnnouncements;
    private EditText txtAnnouncement;

    private FirebaseUser firebaseUser;
    private DatabaseReference userReference, parkrunReference;

    private User currentUser;
    private Parkrun currentParkrun;

    private Calendar currentTime;

    private UtilAlertDialog utilAlertDialog;

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            if(currentUser.getCheckedIn()) setCheckInDetails(false);
            else setCheckInDetails(true);

            announcementList.removeView(announcementTable);

            if(announcementTable!=null)
            {
                if(announcementTable.getParent()==null) announcementList.addView(announcementTable);
            }

            formVisibility(true);
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

        currentTime = Calendar.getInstance();

        utilAlertDialog = new UtilAlertDialog();

        addButton = layout.findViewById(R.id.btnAddNews);
        checkInButton = layout.findViewById(R.id.btnCheckInNews);
        refreshButton = layout.findViewById(R.id.btnRefreshNews);

        txtAnnouncement = layout.findViewById(R.id.txtAnnouncement);

        announcementList = layout.findViewById(R.id.announcementListRelative);

        tvNoAnnouncements = layout.findViewById(R.id.tvNoAnnouncements);
        tvCheckInDetails = layout.findViewById(R.id.tvCheckInDetails);

        FirebaseAuth authentication = FirebaseAuth.getInstance();
        firebaseUser = authentication.getCurrentUser();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        parkrunReference = database.getReference("parkruns");
        userReference = database.getReference("users");

        detailSetup();

        checkInButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                boolean checkInCheck = checkInCheck();
                if(checkInCheck)
                    checkInReset();
                else
                {
                    if(currentUser.getCheckedIn())
                    {
                        currentParkrun.setAttendance(currentParkrun.getAttendance()-1);
                        currentUser.setCheckedIn(false);
                        userReference.child(firebaseUser.getUid()).setValue(currentUser);
                        setCheckInDetails(true);
                    }
                    else
                    {
                        currentParkrun.setAttendance(currentParkrun.getAttendance()+1);
                        currentUser.setCheckedIn(true);
                        userReference.child(firebaseUser.getUid()).setValue(currentUser);
                        setCheckInDetails(false);
                    }
                    parkrunReference.child(currentParkrun.getName()).setValue(currentParkrun);
                }
            }
        });

        refreshButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                announcementList.removeView(announcementTable);
                if(announcementTable!=null) announcementTable.removeAllViews();
                tvCheckInDetails.setText("");

                formVisibility(false);

                detailSetup();
            }
        });

        addButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(!txtAnnouncement.getText().toString().equals(""))
                {
                    Calendar calendar = Calendar.getInstance();

                    Announcement announcement = new Announcement(calendar.getTime(), txtAnnouncement.getText().toString());
                    //create new announcement with current time/date and inputted text

                    ArrayList<Announcement> announcements;

                    if(currentParkrun.getAnnouncements() != null)
                    {
                        announcements = (ArrayList<Announcement>) currentParkrun.getAnnouncements();
                    }
                    else
                    {
                        announcements = new ArrayList<>();
                    }

                    announcements.add(announcement);
                    currentParkrun.setAnnouncements(announcements);
                    parkrunReference.child(currentParkrun.getName()).setValue(currentParkrun);

                    Toast.makeText(getActivity(), "Announcement uploaded! Refresh to view.", Toast.LENGTH_LONG).show();
                }
                else
                {
                    utilAlertDialog.getAlertDialog("No text", "The text field has no text", getActivity());
                }

                txtAnnouncement.setText("");
            }
        });

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
                                userSearchComplete = true;

                                break;
                            }
                        }

                        Log.d("Testing", "User setup complete");
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

                        boolean parkrunFound = false;
                        String parkrunName = currentUser.getParkrunName();

                        for(DataSnapshot child : children)
                        {
                            Parkrun parkrun = child.getValue(Parkrun.class);

                            if(parkrun != null && parkrunName.equals(parkrun.getName()))
                            {
                                currentParkrun = parkrun;

                                //ANNOUNCEMENTS
                                if(currentParkrun.getAnnouncements() != null) announcementTable = setupAnnouncements();
                                else tvNoAnnouncements.setVisibility(View.VISIBLE);

                                checkIn = true;
                                parkrunFound = true;

                                break;
                            }
                        }

                        if(!parkrunFound)
                        {
                            // The user's parkrun does not exist in the database.
                            // Creating a node...
                            Calendar calendar= Calendar.getInstance();
                            Date lastCheckInDate = calendar.getTime();
                            calendar.setTime(lastCheckInDate);

                            Parkrun parkrun = new Parkrun(parkrunName, lastCheckInDate, 0);

                            parkrunReference.child(parkrunName).setValue(parkrun);//add parkrun to database

                            checkIn = false;
                        }

                        parkrunSetupComplete = true;

                        Log.d("Testing", "Parkrun setup complete");
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {

                    }
                });

                while(true)
                {
                    if(parkrunSetupComplete) break;
                    //wait for parkrun search to complete
                }

                parkrunSetupComplete = false;

                if(checkIn)
                {
                    if(checkInCheck())
                        checkInReset();
                    else
                        checkInSetupComplete = true;
                    // SET ATTENDANCE TO 0 and CHECK OUT ALL USERS FOR THIS PARKRUN
                }
                else
                {
                    checkInSetupComplete = true;
                }

                currentTime = Calendar.getInstance();
                currentParkrun.setLastCheckInReadDate(currentTime.getTime());
                parkrunReference.child(currentParkrun.getName()).setValue(currentParkrun);
                //reset read time

                while(true)
                {
                    if(checkInSetupComplete) break;
                    //wait for check in setup to complete
                }

                checkInSetupComplete = false;

                handler.sendEmptyMessage(0);
            }
        };

        Thread setupThread = new Thread(setupRunnable);
        setupThread.start();
    }

    private boolean checkInCheck()
    {
        Calendar parkrunStart = currentTime;

        if(parkrunStart.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY && parkrunStart.get(Calendar.HOUR_OF_DAY) <= 9)
        {
            if(parkrunStart.get(Calendar.MINUTE) < 30 && parkrunStart.get(Calendar.HOUR_OF_DAY) == 9 ||
                    parkrunStart.get(Calendar.HOUR_OF_DAY) < 9) parkrunStart.add(Calendar.DAY_OF_WEEK, -1);
        }//If today is Saturday - check if Saturday BEFORE parkrun starts

        while (parkrunStart.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY) parkrunStart.add(Calendar.DAY_OF_WEEK, -1);
        parkrunStart.set(Calendar.HOUR_OF_DAY, 9);
        parkrunStart.set(Calendar.MINUTE, 30);
        parkrunStart.set(Calendar.SECOND, 0);

        Log.d("Testing", "Last parkrun started: "+parkrunStart.getTime());

        Log.d("Testing", "RESET DATE:"+currentParkrun.getLastCheckInReadDate().before(parkrunStart.getTime()));

        return currentParkrun.getLastCheckInReadDate().before(parkrunStart.getTime());
    }

    private void checkInReset()
    {
        currentUser.setCheckedIn(false);
        currentParkrun.setAttendance(0);

        parkrunReference.child(currentParkrun.getName()).setValue(currentParkrun);

        userReference.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();

                for(DataSnapshot child : children)
                {
                    User user = child.getValue(User.class);

                    if(user != null && user.getParkrunName().equals(currentParkrun.getName()))
                    {
                        user.setCheckedIn(false);
                        userReference.child(child.getKey()).setValue(user);

                        break;
                    }
                }

                checkInSetupComplete = true;

                Log.d("Testing", "All users checked-out");
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    private void formVisibility(boolean visibility)
    {
        if(visibility)
        {
            if(currentUser.getDirector())
            {
                addButton.setVisibility(View.VISIBLE);
                txtAnnouncement.setVisibility(View.VISIBLE);
            }

            ProgressBar progressBar = layout.findViewById(R.id.progressBarNews);
            progressBar.setVisibility(View.INVISIBLE);

            RelativeLayout directorPanelRelative = layout.findViewById(R.id.directorPanelRelative);
            directorPanelRelative.setVisibility(View.VISIBLE);

            RelativeLayout announcementListRelative = layout.findViewById(R.id.announcementListRelative);
            announcementListRelative.setVisibility(View.VISIBLE);

        }
        else
        {
            addButton.setVisibility(View.INVISIBLE);
            txtAnnouncement.setVisibility(View.INVISIBLE);

            ProgressBar progressBar = layout.findViewById(R.id.progressBarNews);
            progressBar.setVisibility(View.VISIBLE);

            RelativeLayout directorPanelRelative = layout.findViewById(R.id.directorPanelRelative);
            directorPanelRelative.setVisibility(View.INVISIBLE);

            RelativeLayout announcementListRelative = layout.findViewById(R.id.announcementListRelative);
            announcementListRelative.setVisibility(View.INVISIBLE);
        }
    }

    private void setCheckInDetails(boolean checkInBtnTxt)
    {
        if(checkInBtnTxt) checkInButton.setText(R.string.checkIn);
        else checkInButton.setText(R.string.checkOut);

        String checkInDetails = "So far, there are " + currentParkrun.getAttendance() +
                " parkrunners attending your home parkrun next parkrun!";
        tvCheckInDetails.setText(checkInDetails);
    }

    private TableLayout setupAnnouncements()
    {
        TableLayout tableLayout = new TableLayout(getActivity().getApplicationContext());

        formVisibility(false);
        tvNoAnnouncements.setVisibility(View.INVISIBLE);

        List<Announcement> announcements = currentParkrun.getAnnouncements();

        Collections.reverse(announcements);

        int top = 0;

        for(Announcement announcement : announcements)
        {
            TableRow tableRow = new TableRow(getActivity().getApplicationContext());
            TextView textView = new TextView(getActivity().getApplicationContext());
            String text = announcement.getText()+"\n\n"+announcement.getDate();
            textView.setText(text);
            tableRow.addView(textView);

            if(top != 0)
                tableRow.setPadding(0,50,0,0);

            top++;

            tableRow.setOnClickListener(new View.OnClickListener()
            {

                @Override
                public void onClick(View view)
                {
                    if(currentUser.getDirector())
                        getResponse(view);
                }
            });

            tableLayout.addView(tableRow);
        }

        return tableLayout;
    }

    public void getResponse(final View view)
    {
        String title = "Delete Announcement";
        String message = "Are you sure you want to delete this announcement?";
        Activity currentActivity = getActivity();
        AlertDialog dialog;
        AlertDialog.Builder loginCorrection = new AlertDialog.Builder(currentActivity);

        loginCorrection.setMessage(message).setCancelable(false).setPositiveButton("Ok", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                TableRow tr = (TableRow) view;
                TextView tv = (TextView) tr.getChildAt(0);
                List<Announcement> ans = currentParkrun.getAnnouncements();

                for(Announcement a : ans)
                {
                    if(tv.getText().toString().equals(a.getText()+"\n\n"+a.getDate()))
                    {
                        ans.remove(a);
                        currentParkrun.setAnnouncements(ans);
                        parkrunReference.child(currentParkrun.getName()).setValue(currentParkrun);
                        break;
                    }
                }

                announcementList.removeView(announcementTable);
                if(announcementTable!=null) announcementTable.removeAllViews();
                tvCheckInDetails.setText("");

                formVisibility(false);

                detailSetup();

                dialogInterface.cancel();
            }
        }).setNegativeButton("Cancel",
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        dialog.dismiss();
                    }
                }
        ).setTitle(title);

        dialog = loginCorrection.create();
        dialog.show();
    }//to wait for response
}