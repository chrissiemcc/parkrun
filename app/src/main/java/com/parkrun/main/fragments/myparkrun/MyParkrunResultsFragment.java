package com.parkrun.main.fragments.myparkrun;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
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

public class MyParkrunResultsFragment extends MyParkrunMainFragment
{
    private View layout;
    private TableLayout tableLayout;
    private TextView[] results = new TextView[7];

    private User user;

    private String parkrunName = "";
    private boolean outcome = true;

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            ProgressBar parkrunResultsProgress;
            if(outcome)
            {
                parkrunResultsProgress = layout.findViewById(R.id.progressBarParkrunResults);
                parkrunResultsProgress.setVisibility(View.INVISIBLE);

                FrameLayout myResultsFrame = layout.findViewById(R.id.parkrunResultsFrame);

                myResultsFrame.addView(tableLayout);
                //view results of parkrunner
            }
            else
            {
                TextView noResults = layout.findViewById(R.id.tvNoResultsParkrun);
                parkrunResultsProgress = layout.findViewById(R.id.progressBarParkrunResults);
                parkrunResultsProgress.setVisibility(View.INVISIBLE);
                noResults.setVisibility(View.VISIBLE);
                //no results found
            }
        }
    };

    public MyParkrunResultsFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        layout = inflater.inflate(R.layout.fragment_my_parkrun_results, container, false);

        final FirebaseAuth authentication = FirebaseAuth.getInstance();
        final FirebaseUser databaseUser = authentication.getCurrentUser();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();

                for (DataSnapshot child : children)
                {
                    user = child.getValue(User.class);

                    if(child.getKey().equals(databaseUser.getUid()))
                    {
                        parkrunName = user.getParkrunName(); //gets the user parkrun name from the database to display it's results

                        Log.d("Testing", "The parkrun: "+parkrunName+" was successfully retrieved");

                        break;
                    }
                }
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
                try
                {
                    while(true)
                    {
                        if (!parkrunName.equals(""))
                        {
                            break; //Sometimes Firebase async tasks need time to catch up...
                        }
                    }

                    tableLayout = new TableLayout(getActivity().getApplicationContext());

                    TableRow tableRow;

                    boolean doHeader = true;

                    TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
                    layoutParams.setMargins(0,8,0,0);

                    Document jsoupDocument = Jsoup.connect("http://www.parkrun.org.uk/"+ parkrunName +"/results/latestresults/").get();
                    // Retrieve parkrun results html page

                    Element resultsTable = jsoupDocument.selectFirst("#results");
                    // Select the main results table

                    Elements rows = resultsTable.select("tr");

                    if(rows.last() != rows.first())
                    {
                        for (Element row : rows)
                        {
                            tableRow = new TableRow(getActivity().getApplicationContext());

                            for (int i=0;i<results.length;i++)
                                results[i] = new TextView(getActivity().getApplicationContext());

                            Elements cells = row.select("td");

                            if(doHeader)
                            {
                                String[] headings = {"Pos","Name","Time","Age%","Club","PB?","Total Runs"};
                                for(int i=0; i<results.length;i++)
                                {
                                    if(i != 3 && i != 5 && i != 6 && i != 10)
                                        results[i].setText(headings[i]);
                                }
                            }
                            else
                            {
                                int i = 0, arrayIndex = 0;

                                for (Element cell : cells)
                                {
                                    if(i != 3 && i != 5 && i != 6 && i != 10)
                                    {
                                        results[arrayIndex].setText(cell.text());
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
                        //No results found
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
}