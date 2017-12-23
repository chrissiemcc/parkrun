package com.parkrun.main;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button signOutButton = findViewById(R.id.btnSignOut);

        FirebaseUser databaseUser = FirebaseAuth.getInstance().getCurrentUser();

        TextView textView = findViewById(R.id.textView);

        String display = "Welcome " + databaseUser.getDisplayName() + "!";
        textView.setText(display);

        signOutButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                signOut();
            }
        });
    }

    private void signOut()
    {
        FirebaseAuth authentication = FirebaseAuth.getInstance();

        if (authentication != null)
        {
            authentication.signOut();
        }

        Intent intent = new Intent(MainActivity.this, LaunchingActivity.class);
        startActivity(intent);

        finish();
    }
}