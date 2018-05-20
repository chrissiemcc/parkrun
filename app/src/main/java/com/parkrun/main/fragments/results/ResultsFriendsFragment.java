package com.parkrun.main.fragments.results;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

public class ResultsFriendsFragment extends Fragment
{
    private View layout;
    private TableLayout tableLayout, friendListLayout;
    private RelativeLayout friendResultsRelative;

    private int friendAthleteId;
    private boolean outcome = false;
    private String friendAthleteName;

    private TextView[] results = new TextView[7];

    private TextView tvNoResultsFriend;
    private Button btnRemoveFriend, btnBackFriend, btnRefreshFriend;

    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference, friendsReference;

    private UtilAlertDialog utilAlertDialog;

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            friendResultsRelative = layout.findViewById(R.id.friendResultsRelative);
            friendResultsRelative.addView(tableLayout);

            setFrameView(1);

            if(outcome)
            {
                tvNoResultsFriend.setVisibility(View.VISIBLE);
                outcome = false; //reset
            }
            else
            {
                tvNoResultsFriend.setVisibility(View.INVISIBLE);
            }
        }
    };

    public ResultsFriendsFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        layout = inflater.inflate(R.layout.fragment_results_friends, container, false);

        FirebaseAuth authentication = FirebaseAuth.getInstance();
        firebaseUser = authentication.getCurrentUser();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("users");
        friendsReference = databaseReference.child(firebaseUser.getUid()).child("friends");

        tvNoResultsFriend = layout.findViewById(R.id.tvNoResultsFriend);
        btnRemoveFriend = layout.findViewById(R.id.btnRemoveFriend);
        btnBackFriend = layout.findViewById(R.id.btnBackFriend);
        btnRefreshFriend = layout.findViewById(R.id.btnRefreshFriend);

        utilAlertDialog = new UtilAlertDialog(getActivity().getApplicationContext());

        generateFriendList(0);

        btnBackFriend.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                generateFriendList(1);
            }
        });

        btnRefreshFriend.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                generateFriendList(1);
            }
        });

        btnRemoveFriend.setOnClickListener(new View.OnClickListener()
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
                                                friendsReference.child(child.getKey()).removeValue();

                                                utilAlertDialog.getAlertDialog("Friend removed", friendAthleteName+" has been removed from your friend list", getActivity());

                                                btnRemoveFriend.setVisibility(View.INVISIBLE);

                                                found = true;
                                                break;
                                            }
                                        }

                                        if(!found)
                                        {
                                            utilAlertDialog.getAlertDialog("Friend already removed", friendAthleteName+" has already been removed from your friend list", getActivity());

                                            btnRemoveFriend.setVisibility(View.INVISIBLE);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError)
                                    {

                                    }
                                });
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
                    tableLayout = new TableLayout(getActivity().getApplicationContext());

                    TableRow tableRow;

                    boolean doHeader = true;

                    TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
                    rowParams.setMargins(0,8,0,0);

                    Log.d("Testing", friendAthleteId +" is the id");

                    Document jsoupDocument = Jsoup.connect("http://www.parkrun.org.uk/results/athleteeventresultshistory/?athleteNumber="+friendAthleteId+"&eventNumber=0").get();
                    // Retrieve parkrun results html page

                    Element resultsTable = jsoupDocument.selectFirst("caption:contains(All Results)").parent();
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
                    else
                    {
                        outcome = true;
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

    private void setFrameView(int choice)
    {
        ProgressBar progressBarFriend = layout.findViewById(R.id.progressBarFriend);
        TextView tvFriendNameDisplay = layout.findViewById(R.id.tvFriendNameDisplay);
        TextView tvNoFriends = layout.findViewById(R.id.tvNoFriends);
        RelativeLayout friendDisplayRelative = layout.findViewById(R.id.friendDisplayRelative);

        switch(choice)
        {
            case 0: //Display NO FRIENDS MESSAGE
                progressBarFriend.setVisibility(View.INVISIBLE);

                tvNoFriends.setVisibility(View.VISIBLE);
                btnRefreshFriend.setVisibility(View.VISIBLE);
                break;
            case 1: //Display FRIEND RESULTS TABLE
                progressBarFriend.setVisibility(View.INVISIBLE);

                friendResultsRelative.setVisibility(View.VISIBLE);

                String nameDisplay = "Results for:\n\n" + friendAthleteName;
                tvFriendNameDisplay.setText(nameDisplay);
                tvFriendNameDisplay.setVisibility(View.VISIBLE);
                btnRemoveFriend.setVisibility(View.VISIBLE);
                btnBackFriend.setVisibility(View.VISIBLE);
                break;
            case 2: //Display PROGRESS BAR
                friendListLayout.setVisibility(View.INVISIBLE);
                btnRefreshFriend.setVisibility(View.INVISIBLE);

                progressBarFriend.setVisibility(View.VISIBLE);
                break;
            case 3: //Display FRIEND LIST
                friendDisplayRelative.addView(friendListLayout);
                btnRefreshFriend.setVisibility(View.VISIBLE);
                break;
            case 4: //FRIEND RESULTS TABLE > FRIEND LIST - Stage 1
                progressBarFriend.setVisibility(View.VISIBLE);

                if(friendResultsRelative != null)
                friendResultsRelative.setVisibility(View.INVISIBLE);

                tvNoFriends.setVisibility(View.INVISIBLE);
                tvFriendNameDisplay.setText(""); //empty text
                tvFriendNameDisplay.setVisibility(View.INVISIBLE);
                btnRemoveFriend.setVisibility(View.INVISIBLE);
                btnBackFriend.setVisibility(View.INVISIBLE);
                break;
            case 5: //FRIEND RESULTS TABLE > FRIEND LIST - Stage 2 & REFRESH
                progressBarFriend.setVisibility(View.INVISIBLE);

                tvNoFriends.setVisibility(View.INVISIBLE);
                friendListLayout.setVisibility(View.VISIBLE);
                btnRefreshFriend.setVisibility(View.VISIBLE);
                friendDisplayRelative.addView(friendListLayout);
                break;
            default:
                break;
        }
    }
    //This method switches the view of the friend tab

    private void generateFriendList(final int path)
    {
        //path = 0 - friend list>friend results
        //path = 1 - friend results>friend list

        if(path == 1)
        {
            setFrameView(4);

            if(friendListLayout != null)
            friendListLayout.removeAllViews();

            if(friendResultsRelative != null)
            friendResultsRelative.removeView(tableLayout);
            //clear the tab
        }
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();

                for(DataSnapshot child : children)
                {
                    User user = child.getValue(User.class);
                    if(user.getFriends() != null)
                    {
                        friendListLayout = new TableLayout(getActivity().getApplicationContext());
                        TableRow tableRow;
                        int index = 1;

                        ArrayList<Friend> friends = (ArrayList<Friend>) user.getFriends();
                        for(final Friend friend : friends)
                        {
                            if(friend != null)
                            {
                                friendAthleteId = friend.getAthleteId();
                                friendAthleteName = friend.getName();
                                String friendDetails = friendAthleteName;
                                tableRow = new TableRow(getActivity().getApplicationContext());
                                TextView tvFriend = new TextView(getActivity().getApplicationContext());

                                //Appearance details
                                TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
                                if(friends.size() != index)
                                {
                                    rowParams.setMargins(4,4,4,0);
                                }
                                else
                                {
                                    rowParams.setMargins(4,4,4,4);
                                } //So as the last element makes a bottom border instead of just top
                                tvFriend.setLayoutParams(rowParams);
                                tvFriend.setBackgroundColor(Color.WHITE);

                                tvFriend.setPadding(10, 0, 10, 0);
                                //Give space to the left and right edges of the text

                                tvFriend.setText(friendDetails);
                                tvFriend.setId(friendAthleteId);
                                tvFriend.setOnClickListener(new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View view)
                                    {
                                        TextView tvFriend = (TextView) view;
                                        friendAthleteName = tvFriend.getText().toString();
                                        friendAthleteId = view.getId();
                                        setFrameView(2);

                                        runJsoupThread();
                                    }
                                });
                                tableRow.addView(tvFriend);
                                friendListLayout.addView(tableRow);
                            }
                            index++;
                        }

                        friendListLayout.setBackgroundColor(Color.BLACK);
                        if(path == 0)
                        {
                            setFrameView(3);
                        }
                        else
                        {
                            setFrameView(5);
                        }
                    }
                    else
                    {
                        setFrameView(0);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }// This method generates the friend list
}