package com.parkrun.main;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private DrawerLayout drawerLayout;

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

        Toolbar toolbar = findViewById(R.id.action_bar);
        toolbar.setTitle("Home");
        setSupportActionBar(toolbar);
        //Initialises and sets the action bar

        drawerLayout = findViewById(R.id.drawerLayout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //Adds menu button to the action bar

        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        //Initialises the navigation view and sets listener

        signOutButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                signOut();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        return actionBarDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        int id = item.getItemId();

        Utilities utilities = new Utilities();

        if(id == R.id.nav_account)
        {
            getSupportActionBar().setTitle("Account");
            utilities.getAlertDialog("Account clicked!", "Account!", MainActivity.this);
        }
        else if(id == R.id.nav_settings)
        {
            getSupportActionBar().setTitle("Settings");
            utilities.getAlertDialog("Settings clicked!", "Settings!", MainActivity.this);
        }
        else if(id == R.id.nav_logout)
        {
            signOut();
        }

        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    public void onBackPressed()
    {
        drawerLayout = findViewById(R.id.drawerLayout);

        if(drawerLayout.isDrawerOpen(GravityCompat.START))
        {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else
        {
            super.onBackPressed();
        }
    }
}