package com.parkrun.main.fragments;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.parkrun.main.R;
import com.parkrun.main.objects.Parkrun;
import com.parkrun.main.objects.User;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class VolunteerFragment extends Fragment
{
    private View layout;
    private TableLayout tableLayout;

    private boolean volunteerSearchComplete = false, userSearchComplete = false, isRoster = false;

    private Map<String,String> volunteerRoster;
    private Map<String,String> parkrunRoster = null;
    private TextView[] results = new TextView[2];

    private FirebaseUser firebaseUser;
    private DatabaseReference userReference, parkrunReference;

    private User currentUser;
    private Parkrun currentParkrun;

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            ProgressBar progressBarVolunteer = layout.findViewById(R.id.progressBarVolunteer);
            progressBarVolunteer.setVisibility(View.INVISIBLE);

            FrameLayout volunteerFrame = layout.findViewById(R.id.volunteerFrame);
            volunteerFrame.addView(tableLayout);
        }
    };

    public VolunteerFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        layout = inflater.inflate(R.layout.fragment_volunteer, container, false);

        volunteerRoster = new HashMap<>();

        FirebaseAuth authentication = FirebaseAuth.getInstance();
        firebaseUser = authentication.getCurrentUser();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        parkrunReference = database.getReference("parkruns");
        userReference = database.getReference("users");

        runJsoupThread();

        return layout;
    }

    private void runJsoupThread()
    {
        Runnable jsoupRun = new Runnable()
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
                                parkrunFound = true;

                                if(parkrun.getVolunteerRoster() != null)
                                {
                                    isRoster = true;
                                    parkrunRoster = parkrun.getVolunteerRoster();
                                }

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
                        }

                        volunteerSearchComplete = true;

                        Log.d("Testing", "Parkrun setup complete");
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {

                    }
                });

                try
                {
                    while(true)
                    {
                        if (volunteerSearchComplete) break;
                        //wait for the volunteer search to complete if necessary
                    }

                    tableLayout = new TableLayout(getActivity().getApplicationContext());

                    TableRow tableRow;

                    TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
                    rowParams.setMargins(0,8,0,0);

                    Document jsoupDocument = Jsoup.connect("http://www.parkrun.org.uk/"+ currentParkrun.getName() +"/futureroster/").get();
                    // Retrieve volunteer html page

                    Element resultsTable = jsoupDocument.selectFirst("#rosterTable");
                    // Select the roster table

                    Elements rows = resultsTable.select("tr");

                    int index = 1;

                    for (Element row : rows)
                    {
                            tableRow = new TableRow(getActivity().getApplicationContext());

                            for (int i=0;i<results.length;i++)
                                results[i] = new TextView(getActivity().getApplicationContext());

                            Element role = row.selectFirst("a");
                            Element volunteer = row.selectFirst("td");

                            if(role != null)
                            {
                                if(role!=null)
                                {
                                    Log.d("Testing", role.text());
                                    results[0].setText(role.text());
                                    results[0].setBackgroundColor(Color.CYAN);
                                }
                                else
                                {
                                    Log.d("Testing", " ");
                                }

                                if(volunteer!=null)
                                {
                                    results[1].setText(volunteer.text());
                                    results[1].setBackgroundColor(getResources().getColor(R.color.colorPrimary));

                                    if(!volunteer.hasText())
                                    {
                                        Log.d("Testing", "ROLE - NO");
                                        results[1].setOnClickListener(new View.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(View view)
                                            {
                                                for (Map.Entry<String, String> entry : volunteerRoster.entrySet())
                                                {
                                                    if (entry.getValue().equals("freeRole"))
                                                    {
                                                        TableRow tableRowRole = (TableRow) view.getParent();
                                                        TextView textView = (TextView) tableRowRole.getChildAt(0);
                                                        if(entry.getKey().contains(textView.getText().toString()))
                                                            Log.d("Testing", textView.getText().toString());
                                                    }
                                                    Log.d("THE MAP LIST", entry.getKey() + " " + entry.getValue());
                                                }
                                            }
                                        });
                                    }
                                    else
                                    {
                                        Log.d("Testing", "ROLE - YES");
                                        Log.d("Testing", volunteer.text());
                                    }
                                }
                                else
                                {
                                    Log.d("Testing", "NULL");
                                }
                            }

                            if(volunteer!=null)
                                if(!volunteer.hasText())
                                {
                                    if(volunteerRoster.containsKey(results[0].getText().toString()+index))
                                        index++;

                                    volunteerRoster.put(results[0].getText().toString()+index, "freeRole");
                                }
                                else
                                {
                                    if(volunteerRoster.containsKey(results[0].getText().toString()+index))
                                        index++;

                                    volunteerRoster.put(results[0].getText().toString()+index, results[1].getText().toString());
                                }

                            if(index == 2)
                                index--;

                            for (TextView result : results)
                            {
                                result.setGravity(Gravity.CENTER);
                                result.setPadding(8, 0, 8, 0);
                                result.setLayoutParams(rowParams); //set margins between rows
                                result.setTextSize(5, 3f);
                                tableRow.addView(result);
                            }

                            tableLayout.addView(tableRow);
                            //Add row to table after it has finished populating

                            tableLayout.setStretchAllColumns(true);
                            //Makes table fills the screen
                    }

                    if(parkrunRoster != null)
                    {
                        parkrunRoster = volunteerRoster;
                        currentParkrun.setVolunteerRoster(volunteerRoster);
                        parkrunReference.child(currentParkrun.getName()).setValue(currentParkrun);
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                handler.sendEmptyMessage(0);
            }
        };

        Thread jsoupThread = new Thread(jsoupRun);
        jsoupThread.start();
    }
    //A separate method to run the thread for jsoup, which cannot access network on main thread

    @Override
    public void onResume()
    {
        super.onResume();

        NavigationView navigationView = getActivity().findViewById(R.id.navigation_view);
        MenuItem menuItem = navigationView.getMenu().findItem(R.id.nav_volunteer);

        if(!menuItem.isChecked())
        {
            menuItem.setChecked(true);
        }
    }
    //To make sure the item checked in the navigation menu is always correct, even on back press
}