package com.parkrun.main.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.parkrun.main.R;
import com.parkrun.main.objects.User;

public class SettingsActivity extends AppCompatActivity
{
    private User currentUser;
    private CheckBox checkBox;

    private DatabaseReference userReference;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        checkBox = findViewById(R.id.cbxWeatherNotify);

        FirebaseAuth authentication = FirebaseAuth.getInstance();
        firebaseUser = authentication.getCurrentUser();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        userReference = database.getReference("users");

        userReference.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();

                for(DataSnapshot child : children)
                {
                    User user = child.getValue(User.class);

                    if(user != null && firebaseUser.getUid().equals(child.getKey()))
                    {
                        currentUser = user;

                        if(currentUser.getWeatherNotify())
                        {
                            checkBox.setChecked(true);
                            checkBox.setVisibility(View.VISIBLE);
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

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked)
            {
                if(isChecked)
                {
                    currentUser.setWeatherNotify(true);
                    userReference.child(firebaseUser.getUid()).setValue(currentUser);
                }
                else
                {
                    currentUser.setWeatherNotify(false);
                    userReference.child(firebaseUser.getUid()).setValue(currentUser);
                }
            }
        });
    }
}