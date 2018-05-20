package com.parkrun.main.fragments.results;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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
import com.parkrun.main.objects.Friend;
import com.parkrun.main.objects.User;
import com.parkrun.main.util.UtilAlertDialog;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class ResultsOtherFragment extends Fragment
{
    private int outcome = 0;
    // 0 = parkrunner found with results
    // 1 = parkrunner found with no results
    // 2 = no parkrunner found

    private int friendAthleteId;
    private boolean friendSearchComplete = false;
    private String friendAthleteName = "";

    private View layout;
    private TableLayout tableLayout;
    private RelativeLayout nameDisplayRelative;
    private FrameLayout otherResultsFrame;

    private TextView tvNoResults;
    private TextView[] results = new TextView[7];
    private EditText txtSearchAthlete;
    private Button btnSearchAthlete;
    private Button btnAddFriend;
    private ProgressBar progressBarSearchOther;

    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference, friendsReference;

    private UtilAlertDialog utilAlertDialog;

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            if(outcome == 0 || outcome == 1)
            {
                progressBarSearchOther = layout.findViewById(R.id.progressBarSearchOther);
                progressBarSearchOther.setVisibility(View.INVISIBLE);

                nameDisplayRelative.setVisibility(View.VISIBLE);

                TextView tvNameDisplay = layout.findViewById(R.id.tvNameDisplay);

                String nameDisplay = "Results for:\n\n" + friendAthleteName;
                tvNameDisplay.setText(nameDisplay);

                if(outcome == 0)
                {
                    otherResultsFrame.addView(tableLayout);
                    //view results of parkrunner
                }
                else if(outcome == 1)
                {
                    tvNoResults.setVisibility(View.VISIBLE);
                }
            }
            else if(outcome == 2)
            {
                progressBarSearchOther = layout.findViewById(R.id.progressBarSearchOther);
                searchFormVisibility(true);

                //no parkrunner found
                utilAlertDialog.getAlertDialog("No athlete found", "The athlete ID provided did not match a parkrunner.", getActivity());
            }
            friendSearchComplete = false;
        }
    };

    public ResultsOtherFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        layout = inflater.inflate(R.layout.fragment_results_other, container, false);

        btnSearchAthlete = layout.findViewById(R.id.btnSearchAthlete);
        txtSearchAthlete = layout.findViewById(R.id.txtSearchAthlete);
        tvNoResults = layout.findViewById(R.id.tvNoResultsOther);
        progressBarSearchOther = layout.findViewById(R.id.progressBarSearchOther);
        btnAddFriend = layout.findViewById(R.id.btnAddFriend);
        Button btnBackOther = layout.findViewById(R.id.btnBackOther);

        nameDisplayRelative = layout.findViewById(R.id.nameDisplayRelative);
        otherResultsFrame = layout.findViewById(R.id.otherResultsFrame);

        FirebaseAuth authentication = FirebaseAuth.getInstance();
        firebaseUser = authentication.getCurrentUser();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("users");
        friendsReference = databaseReference.child(firebaseUser.getUid()).child("friends");

        utilAlertDialog = new UtilAlertDialog(getActivity().getApplicationContext());

        btnSearchAthlete.setEnabled(false);

        txtSearchAthlete.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int before, int count)
            {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count)
            {
                if(txtSearchAthlete.getText().toString().equals(""))
                {
                    btnSearchAthlete.setEnabled(false);
                }
                else
                {
                    btnSearchAthlete.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable)
            {

            }
        });

        btnSearchAthlete.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                friendAthleteId = Integer.parseInt(txtSearchAthlete.getText().toString());
                friendSearchComplete = false;

                outcome = 0; //reset the outcome

                closeKeyboard();
                searchFormVisibility(false);

                txtSearchAthlete.setText("");

                friendsReference.addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        Iterable<DataSnapshot> children = dataSnapshot.getChildren();

                        boolean friendExists = false;

                        for (DataSnapshot child : children)
                        {
                            Friend friend = child.getValue(Friend.class);

                            if(friendAthleteId == friend.getAthleteId())
                            {
                                btnAddFriend.setText(R.string.remove);
                                friendExists = true;
                                break;
                            }
                        }
                        if(!friendExists)
                        {
                            btnAddFriend.setText(R.string.add);
                        }

                        friendSearchComplete = true;
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {

                    }
                });

                runJsoupThread();
            }
        });

        btnAddFriend.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        Iterable<DataSnapshot> children = dataSnapshot.getChildren();

                        for (DataSnapshot child : children)
                        {
                            User user = child.getValue(User.class);
                            if(user != null && user.getEmail().equals(firebaseUser.getEmail()))
                            {
                                if(user.getAthleteId() != friendAthleteId)
                                {
                                    if(btnAddFriend.getText().equals("Remove"))
                                    {
                                        friendsReference.addListenerForSingleValueEvent(new ValueEventListener()
                                        {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot)
                                            {
                                                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                                                boolean found = false;

                                                for (DataSnapshot child : children)
                                                {
                                                    Friend friend = child.getValue(Friend.class);

                                                    if(friendAthleteId == friend.getAthleteId())
                                                    {
                                                        btnAddFriend.setText(R.string.add);

                                                        friendsReference.child(child.getKey()).removeValue();

                                                        utilAlertDialog.getAlertDialog("Friend removed", friendAthleteName+" has been removed from your friend list", getActivity());

                                                        found = true;
                                                        break;
                                                    }
                                                }

                                                if(!found)
                                                {
                                                    btnAddFriend.setText(R.string.add);

                                                    utilAlertDialog.getAlertDialog("Friend already removed", friendAthleteName+" has already been removed from your friend list", getActivity());
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError)
                                            {

                                            }
                                        });
                                    }
                                    else if(btnAddFriend.getText().equals("Add"))
                                    {
                                        Friend friend = new Friend(friendAthleteName, friendAthleteId);
                                        ArrayList<Friend> friends = new ArrayList<>();

                                        if (user.getFriends() != null)
                                        {
                                            friends = (ArrayList<Friend>) user.getFriends();
                                        }
                                        //if not null, friend list exists (get old list and add to it)
                                        //if null, friend list does not exist

                                        friends.add(friend);
                                        friendsReference.setValue(friends);

                                        btnAddFriend.setText(R.string.remove);

                                        utilAlertDialog.getAlertDialog("Friend added", friendAthleteName+" has been added to your friend list", getActivity());
                                    }
                                }
                                else
                                {
                                    utilAlertDialog.getAlertDialog("This is you!", "You cannot add yourself!", getActivity());
                                }
                                break;
                            }
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {

                    }
                });
            }
        });

        btnBackOther.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                nameDisplayRelative.setVisibility(View.INVISIBLE);
                tvNoResults.setVisibility(View.INVISIBLE);
                if(otherResultsFrame != null)
                otherResultsFrame.removeView(tableLayout);

                searchFormVisibility(true);
            }
        });

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
                        if (friendSearchComplete) break;
                        //wait for the database friend search to complete if necessary
                    }

                    tableLayout = new TableLayout(getActivity().getApplicationContext());

                    TableRow tableRow;

                    boolean doHeader = true;

                    TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
                    rowParams.setMargins(0,8,0,0);

                    Log.d("Testing", friendAthleteId +" is the id");

                    Document jsoupDocument = Jsoup.connect("http://www.parkrun.org.uk/results/athleteeventresultshistory/?athleteNumber="+friendAthleteId+"&eventNumber=0").get();
                    // Retrieve parkrun results html page

                    Element athleteCheck = jsoupDocument.selectFirst("h2");

                    Log.d("Testing", ""+athleteCheck.text().charAt(0));

                    if(athleteCheck.text().charAt(0) == '-')
                    {
                        outcome = 2;
                        //no athlete
                    }
                    else
                    {
                        int dash = athleteCheck.text().indexOf('-');
                        friendAthleteName = athleteCheck.text().substring(0, dash).trim();
                        //athlete found
                    }

                    Element resultsTable = jsoupDocument.selectFirst("caption:contains(All Results)").parent();
                    // Select the main results table

                    Elements rows = resultsTable.select("tr");

                    if((rows.last() != rows.first())&&(outcome != 2))
                    {
                        for (Element row : rows)
                        {
                            tableRow = new TableRow(getActivity().getApplicationContext());

                            for (int i=0;i<results.length;i++)
                                results[i] = new TextView(getActivity().getApplicationContext());

                            Elements cells = row.select("td");

                            if(doHeader)
                            {
                                String[] headings = {"parkrun","Date","Event #","Pos","Time","Age %","PB?"};
                                for(int i=0; i<results.length;i++)
                                    results[i].setText(headings[i]);
                            }
                            else
                                for (Element cell : cells)
                                    results[cell.elementSiblingIndex()].setText(cell.text());

                            for (TextView result : results)
                            {
                                result.setGravity(Gravity.CENTER);
                                result.setPadding(8, 0, 8, 0);
                                result.setLayoutParams(rowParams); //set margins between rows
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
                    else if (outcome == 0)
                    {
                        //No results found
                        outcome = 1;
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

    private void searchFormVisibility (boolean visible)
    {
        if (!visible)
        {
            progressBarSearchOther.setVisibility(View.VISIBLE);
            txtSearchAthlete.setVisibility(View.INVISIBLE);
            btnSearchAthlete.setVisibility(View.INVISIBLE);
        }
        else if (visible)
        {
            progressBarSearchOther.setVisibility(View.INVISIBLE);
            txtSearchAthlete.setVisibility(View.VISIBLE);
            btnSearchAthlete.setVisibility(View.VISIBLE);
        }
    }

    private void closeKeyboard()
    {
        InputMethodManager inputManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }
}