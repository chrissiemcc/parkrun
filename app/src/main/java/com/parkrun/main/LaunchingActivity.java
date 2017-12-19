package com.parkrun.main;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;

public class LaunchingActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launching);

        FirebaseAuth authentication = FirebaseAuth.getInstance();

        authentication.signOut();

        if (authentication.getCurrentUser() != null)
        {
            Intent intent = new Intent(LaunchingActivity.this, MainActivity.class);
            startActivity(intent);
        }
        else
        {
            Intent intent = new Intent(LaunchingActivity.this, LoginActivity.class);
            startActivity(intent);
        }
    }
}
