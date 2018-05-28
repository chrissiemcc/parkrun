package com.parkrun.main.fragments.myparkrun;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
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

    private RelativeLayout galleryListRelative, galleryList;

    private TextView tvNoImages;

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
            galleryListRelative.removeView(galleryList);
            if(galleryList!= null) galleryListRelative.addView(galleryList);

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

        galleryListRelative = layout.findViewById(R.id.galleryListRelative);

        tvNoImages = layout.findViewById(R.id.tvNoImages);

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
                if(galleryList!=null) galleryList.removeAllViews();

                formVisibility(false);

                detailSetup();
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
            Toast.makeText(getActivity(), "Uploading...", Toast.LENGTH_LONG).show();

            int gallerySize;
            String parkrunName = currentParkrun.getName();
            Photo photo;
            List<Photo> gallery;
            Uri uri = data.getData();

            if(currentParkrun.getGallery() != null)
            {
                gallerySize = currentParkrun.getGallery().size();
                photo = new Photo(parkrunName+(gallerySize+290));
                gallery = currentParkrun.getGallery();
            }//if gallery does exist
            else
            {
                gallery = new ArrayList<>();
                photo = new Photo(parkrunName+"290");
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
                                if(currentParkrun.getGallery() != null) galleryList = setupGallery();
                                else tvNoImages.setVisibility(View.VISIBLE);

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

            galleryListRelative.setVisibility(View.VISIBLE);
        }
        else
        {
            addButton.setVisibility(View.INVISIBLE);

            ProgressBar progressBar = layout.findViewById(R.id.progressBarGallery);
            progressBar.setVisibility(View.VISIBLE);

            RelativeLayout directorPanelRelative = layout.findViewById(R.id.directorGalleryRelative);
            directorPanelRelative.setVisibility(View.INVISIBLE);

            galleryListRelative.setVisibility(View.INVISIBLE);
        }
    }

    private RelativeLayout setupGallery()
    {
        RelativeLayout relativeLayout = new RelativeLayout(getActivity().getApplicationContext());
        TableLayout tableLayout = new TableLayout(getActivity().getApplicationContext());

        formVisibility(false);
        tvNoImages.setVisibility(View.INVISIBLE);

        List<Photo> gallery = currentParkrun.getGallery();

        for(Photo photo : gallery)
        {
            TableRow tableRow = new TableRow(getActivity().getApplicationContext());
            ImageView imageView = new ImageView(getActivity().getApplicationContext());
            RelativeLayout imageRelative = new RelativeLayout(getActivity().getApplicationContext());
            TextView tvImageName = new TextView(getActivity().getApplicationContext());
            tvImageName.setVisibility(View.INVISIBLE);

            storageReference = FirebaseStorage.getInstance().getReference().child(currentParkrun.getName()).child(photo.getName());

            Log.d("Testing", photo.getName()+" loading");

            Glide.with(getActivity().getApplicationContext())
                    .using(new FirebaseImageLoader())
                    .load(storageReference)
                    .into(imageView);

            imageRelative.addView(imageView);
            imageRelative.setPadding(5, 40, 5, 0);
            tvImageName.setText(photo.getName());
            imageRelative.addView(tvImageName);

            imageRelative.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if(currentUser.getDirector())
                        getResponse(view);
                }
            });

            tableRow.addView(imageRelative);
            tableLayout.addView(tableRow);
        }
        relativeLayout.addView(tableLayout);

        return relativeLayout;
    }

    public void getResponse(final View view)
    {
        String title = "Delete Photo";
        String message = "Are you sure you want to delete this photo?";
        Activity currentActivity = getActivity();
        AlertDialog dialog;
        AlertDialog.Builder loginCorrection = new AlertDialog.Builder(currentActivity);

        loginCorrection.setMessage(message).setCancelable(false).setPositiveButton("Ok", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                RelativeLayout imageRelative = (RelativeLayout) view;
                TextView textView = (TextView) imageRelative.getChildAt(1);

                List<Photo> gallery = currentParkrun.getGallery();

                for(Photo image : gallery)
                {
                    if(image.getName().equals(textView.getText().toString()))
                    {
                        gallery.remove(image);
                        currentParkrun.setGallery(gallery);
                        parkrunReference.child(currentParkrun.getName()).setValue(currentParkrun);
                        break;
                    }
                }

                StorageReference deleteReference = FirebaseStorage.getInstance().getReference().child(currentParkrun.getName()).child(textView.getText().toString());
                deleteReference.delete().addOnSuccessListener(new OnSuccessListener<Void>()
                {
                    @Override
                    public void onSuccess(Void aVoid)
                    {
                        Log.d("Testing", "Photo deleted");
                    }
                });

                if(galleryList!=null) galleryList.removeAllViews();

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