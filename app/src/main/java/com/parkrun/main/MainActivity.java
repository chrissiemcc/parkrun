package com.parkrun.main;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button signOutButton = findViewById(R.id.btnSignOut);

        final FirebaseAuth authentication = FirebaseAuth.getInstance();

        signOutButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Toast.makeText(getApplicationContext(), authentication.getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
                Toast.makeText(getApplicationContext(), authentication.getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
