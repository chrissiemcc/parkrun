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

public class ResultsFriendsFragment extends Fragment
{
    private View layout;
    private TableLayout tableLayout, friendTableLayout;
    private RelativeLayout friendDisplayRelative, friendResultsRelative;

    private int friendAthleteId;
    private String friendAthleteName;

    private TextView[] results = new TextView[7];

    private Button btnRemoveFriend;

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

        btnRemoveFriend = layout.findViewById(R.id.btnRemoveFriend);

        utilAlertDialog = new UtilAlertDialog(getActivity().getApplicationContext());

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
                        friendTableLayout = new TableLayout(getActivity().getApplicationContext());
                        TableRow tableRow;

                        ArrayList<Friend> friends = (ArrayList<Friend>) user.getFriends();
                        for(final Friend friend : friends)
                        {
                            friendAthleteId = friend.getAthleteId();
                            friendAthleteName = friend.getName();
                            String friendDetails = friendAthleteName;
                            tableRow = new TableRow(getActivity().getApplicationContext());
                            TextView tvFriend = new TextView(getActivity().getApplicationContext());
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
                            friendTableLayout.addView(tableRow);
                        }
                        setFrameView(3);
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

                                        for (DataSnapshot child : children)
                                        {
                                            Friend friend = child.getValue(Friend.class);

                                            if(friendAthleteId == friend.getAthleteId())
                                            {
                                                btnRemoveFriend.setText(R.string.add);

                                                friendsReference.child(child.getKey()).removeValue();

                                                utilAlertDialog.getAlertDialog("Friend removed", friendAthleteName+" has been removed from your friend list", getActivity());

                                                btnRemoveFriend.setVisibility(View.INVISIBLE);
                                            }
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

                    Element athleteCheck = jsoupDocument.selectFirst("h2");

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
        switch(choice)
        {
            case 0: //Display NO FRIENDS MESSAGE
                TextView tvNoFriends = layout.findViewById(R.id.tvNoFriends);
                tvNoFriends.setVisibility(View.VISIBLE);
                break;
            case 1: //Display FRIEND RESULTS TABLE
                progressBarFriend.setVisibility(View.INVISIBLE);

                TextView tvFriendNameDisplay = layout.findViewById(R.id.tvFriendNameDisplay);
                tvFriendNameDisplay.append(friendAthleteName);
                tvFriendNameDisplay.setVisibility(View.VISIBLE);
                Button btnRemoveFriend = layout.findViewById(R.id.btnRemoveFriend);
                btnRemoveFriend.setVisibility(View.VISIBLE);
                break;
            case 2: //Display PROGRESS BAR
                friendTableLayout.setVisibility(View.INVISIBLE);

                progressBarFriend.setVisibility(View.VISIBLE);
                break;
            case 3: //Display FRIEND LIST
                friendDisplayRelative = layout.findViewById(R.id.friendDisplayRelative);
                friendDisplayRelative.addView(friendTableLayout);
                friendDisplayRelative.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }
    //This method switches the view of the friend tab
}