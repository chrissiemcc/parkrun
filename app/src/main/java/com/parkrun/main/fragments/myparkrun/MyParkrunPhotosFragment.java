package com.parkrun.main.fragments.myparkrun;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
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
import android.widget.TableLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.parkrun.main.R;
import com.parkrun.main.objects.Parkrun;
import com.parkrun.main.objects.Photo;
import com.parkrun.main.objects.User;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MyParkrunPhotosFragment extends MyParkrunMainFragment
{
    private boolean parkrunSetupComplete = false, userSearchComplete = false;
    private static final int GALLERY_INTENT = 2;

    private View layout;

    private Button addButton, refreshButton;

    private RelativeLayout galleryList;
    private TableLayout galleryTable;

    private FirebaseUser firebaseUser;
    private DatabaseReference userReference, parkrunReference;
    private StorageReference storageReference;

    private User currentUser;
    private Parkrun currentParkrun;

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            formVisibility(true);
        }
    };

    public MyParkrunPhotosFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        layout = inflater.inflate(R.layout.fragment_my_parkrun_photos, container, false);

        addButton = layout.findViewById(R.id.btnAddPhoto);
        refreshButton = layout.findViewById(R.id.btnRefreshGallery);

        galleryList = layout.findViewById(R.id.galleryListRelative);

        FirebaseAuth authentication = FirebaseAuth.getInstance();
        firebaseUser = authentication.getCurrentUser();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        parkrunReference = database.getReference("parkruns");
        userReference = database.getReference("users");

        detailSetup();

        addButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(Intent.ACTION_PICK);

                intent.setType("image/*");

                startActivityForResult(intent, GALLERY_INTENT);
            }
        });

        refreshButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

            }
        });

        return layout;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_INTENT && resultCode == Activity.RESULT_OK)
        {
            int gallerySize;
            String parkrunName = currentParkrun.getName();
            Photo photo;
            List<Photo> gallery;
            Uri uri = data.getData();

            if(currentParkrun.getGallery() != null)
            {
                gallerySize = currentParkrun.getGallery().size();
                photo = new Photo(parkrunName+(gallerySize+1));
                gallery = currentParkrun.getGallery();
            }//if gallery does exist
            else
            {
                gallery = new ArrayList<>();
                photo = new Photo(parkrunName+"1");
            }//if gallery does not exist

            gallery.add(photo);
            currentParkrun.setGallery(gallery);
            storageReference = FirebaseStorage.getInstance().getReference().child(parkrunName).child(photo.getName());
            parkrunReference.child(parkrunName).setValue(currentParkrun);

            storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
            {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                {
                    Log.d("Testing", "Photo uploaded!");

                    Toast.makeText(getActivity(), "Photo uploaded! Refresh to view.", Toast.LENGTH_LONG).show();
                }
            });
        }
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

                                //PHOTOS
                                if(currentParkrun.getGallery() != null) galleryTable = setupGallery();

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

                handler.sendEmptyMessage(0);
            }
        };

        Thread setupThread = new Thread(setupRunnable);
        setupThread.start();
    }

    private void formVisibility(boolean visibility)
    {
        if(visibility)
        {
            if(currentUser.getDirector())
            {
                addButton.setVisibility(View.VISIBLE);
            }

            ProgressBar progressBar = layout.findViewById(R.id.progressBarGallery);
            progressBar.setVisibility(View.INVISIBLE);

            RelativeLayout directorPanelRelative = layout.findViewById(R.id.directorGalleryRelative);
            directorPanelRelative.setVisibility(View.VISIBLE);

            RelativeLayout announcementListRelative = layout.findViewById(R.id.galleryListRelative);
            announcementListRelative.setVisibility(View.VISIBLE);
        }
        else
        {
            addButton.setVisibility(View.INVISIBLE);

            ProgressBar progressBar = layout.findViewById(R.id.progressBarGallery);
            progressBar.setVisibility(View.VISIBLE);

            RelativeLayout directorPanelRelative = layout.findViewById(R.id.directorGalleryRelative);
            directorPanelRelative.setVisibility(View.INVISIBLE);

            RelativeLayout announcementListRelative = layout.findViewById(R.id.galleryListRelative);
            announcementListRelative.setVisibility(View.INVISIBLE);
        }
    }

    private TableLayout setupGallery()
    {
        TableLayout tableLayout = new TableLayout(getActivity().getApplicationContext());

        formVisibility(false);

        return tableLayout;
    }
}