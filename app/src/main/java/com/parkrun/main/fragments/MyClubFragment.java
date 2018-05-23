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
import com.parkrun.main.objects.User;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class MyClubFragment extends Fragment
{
    private View layout;
    private TableLayout tableLayout;
    private TextView[] results = new TextView[5];

    private User currentUser;

    private boolean outcome = true, userSearchComplete = false;

    private DatabaseReference userReference;
    private FirebaseUser firebaseUser;

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            ProgressBar clubResultsProgress;
            if(outcome)
            {
                clubResultsProgress = layout.findViewById(R.id.progressBarClubResults);
                clubResultsProgress.setVisibility(View.INVISIBLE);

                FrameLayout myResultsFrame = layout.findViewById(R.id.clubResultsFrame);

                myResultsFrame.addView(tableLayout);
                //view results of parkrunner
            }
            else
            {
                TextView noResults = layout.findViewById(R.id.tvNoClub);
                clubResultsProgress = layout.findViewById(R.id.progressBarClubResults);
                clubResultsProgress.setVisibility(View.INVISIBLE);
                noResults.setVisibility(View.VISIBLE);
                //no club found
            }
        }
    };

    public MyClubFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        layout = inflater.inflate(R.layout.fragment_my_club, container, false);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        userReference = database.getReference("users");

        FirebaseAuth authentication = FirebaseAuth.getInstance();
        firebaseUser = authentication.getCurrentUser();

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
                while(true)
                {
                    if(userSearchComplete) break;
                    //wait for user search to complete
                }

                userSearchComplete = false;

                try
                {
                    tableLayout = new TableLayout(getActivity().getApplicationContext());

                    TableRow tableRow;

                    boolean doHeader = true;

                    TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
                    layoutParams.setMargins(0,8,0,0);

                    Document jsoupDocument = Jsoup.connect("http://www.parkrun.org.uk/carrickfergus/results/clubhistory/?clubNum=" + currentUser.getRunningClubId()).get();
                    // Retrieve club results html page

                    Element resultsTable = jsoupDocument.selectFirst("#results");
                    // Select the main results table

                    Elements rows = resultsTable.select("tr");

                    if(!currentUser.getRunningClubName().equals("Unattached"))
                    {
                        for (Element row : rows)
                        {
                            tableRow = new TableRow(getActivity().getApplicationContext());

                            for (int i=0;i<results.length;i++)
                                results[i] = new TextView(getActivity().getApplicationContext());

                            Elements cells = row.select("td:not(.bspacer)");

                            if(doHeader)
                            {
                                String[] headings = {"Name","Fastest","Average","Slowest","Total"};
                                for(int i=0; i<results.length;i++)
                                {
                                    try
                                    {
                                        results[i].setText(headings[i]);
                                    }
                                    catch (ArrayIndexOutOfBoundsException e)
                                    {
                                        break;
                                    }
                                }
                            }
                            else
                            {
                                int i = 0, arrayIndex = 0;

                                for (Element cell : cells)
                                {
                                    if(i != 4 && i != 5 && i != 6 && i != 7 && i != 9 && i != 10)
                                    {
                                        String cellText = cell.text();
                                        //if(i == 7 || i == 1 || i == 8)
                                        //{
                                        //    cellText = cellText.replace(" ", "\n");
                                        //}
                                        results[arrayIndex].setText(cellText);
                                        arrayIndex++;
                                    }
                                    i++;
                                }
                            }

                            for (TextView result : results)
                            {
                                result.setGravity(Gravity.CENTER);
                                result.setPadding(8, 0, 8, 0);
                                result.setLayoutParams(layoutParams); //set margins between rows
                                if(doHeader) result.setBackgroundColor(Color.CYAN); //set heading background colour
                                result.setTextSize(5, 2f);
                                tableRow.addView(result);
                            }

                            if(doHeader) doHeader = false;

                            tableLayout.addView(tableRow);
                            //Add row to table after it has finished populating

                            tableLayout.setStretchAllColumns(true);
                            //Makes table fills the screen
                        }
                    }
                    else
                    {
                        //No club found
                        outcome = false;
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
        MenuItem menuItem = navigationView.getMenu().findItem(R.id.nav_my_club);

        if(!menuItem.isChecked())
        {
            menuItem.setChecked(true);
        }
    }
    //To make sure the item checked in the navigation menu is always correct, even on back press
}