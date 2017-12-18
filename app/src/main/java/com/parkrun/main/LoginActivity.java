package com.parkrun.main;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity
{
    DatabaseReference databaseUsers;
    FirebaseAuth authentication;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    int athleteId;
    Intent intent;
    String passString, correctPass;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final Button loginButton = findViewById(R.id.loginButton);
        final Button registerButton = findViewById(R.id.registerLogin);

        final EditText athleteNumber = findViewById(R.id.athleteNumberLoginField);
        final EditText password = findViewById(R.id.passwordLoginField);

        databaseUsers = FirebaseDatabase.getInstance().getReference("users");

        authentication = FirebaseAuth.getInstance();

//        if (authentication.getCurrentUser() == null)
//        {
//
//        }



        loginButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                athleteId = Integer.parseInt(athleteNumber.getText().toString());
                passString = password.getText().toString();

                databaseUsers.addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        Iterable<DataSnapshot> children = dataSnapshot.getChildren();

                        for (DataSnapshot child : children)
                        {
                            User user = child.getValue(User.class);
                            if (user.getAthleteId() == athleteId)
                            {
                                correctPass = user.getPassword();

                                if(passString.equals(correctPass))
                                {
                                    intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    break;
                                }
                                else
                                {
                                    Toast.makeText(getApplicationContext(),"Wrong password", Toast.LENGTH_SHORT).show();
                                    break;
                                }
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

        registerButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
}