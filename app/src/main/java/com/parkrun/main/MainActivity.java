package com.parkrun.main;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.support.v7.widget.Toolbar;
import android.widget.FrameLayout;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private DrawerLayout drawerLayout;
    private FrameLayout frameLayout;
    private HomeFragment homeFragment;
    private VolunteerFragment volunteerFragment;
    private ResultsFragment resultsFragment;
    private MyParkrunFragment parkrunFragment;
    private MyClubFragment clubFragment;
    private InfoFragment infoFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.action_bar);
        toolbar.setTitle("Home");
        setSupportActionBar(toolbar);
        //Initialise and set the action bar

        drawerLayout = findViewById(R.id.drawerLayout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //Add menu button to the action bar

        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        //Initialise the navigation view and set listener

        frameLayout = findViewById(R.id.main_frame);
        homeFragment = new HomeFragment();
        volunteerFragment = new VolunteerFragment();
        resultsFragment = new ResultsFragment();
        parkrunFragment = new MyParkrunFragment();
        clubFragment = new MyClubFragment();
        infoFragment = new InfoFragment();
        //Initialise fragments for each nav page

        setFragment(homeFragment);
        navigationView.setCheckedItem(R.id.nav_home);
        //Set default fragment and nav menu item
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

        switch (id)
        {
            case R.id.nav_home:
                getSupportActionBar().setTitle("Home");
                setFragment(homeFragment);
                break;

            case R.id.nav_volunteer:
                getSupportActionBar().setTitle("Volunteer");
                setFragment(volunteerFragment);
                break;

            case R.id.nav_results:
                getSupportActionBar().setTitle("Results");
                setFragment(resultsFragment);
                break;

            case R.id.nav_my_parkrun:
                getSupportActionBar().setTitle("My parkrun");
                setFragment(parkrunFragment);
                break;

            case R.id.nav_my_club:
                getSupportActionBar().setTitle("My Club");
                setFragment(clubFragment);
                break;

            case R.id.nav_info:
                getSupportActionBar().setTitle("parkrun Info");
                setFragment(infoFragment);
                break;

            case R.id.nav_profile:

                //new intent

                break;

            case R.id.nav_settings:

                //new intent

                break;

            case R.id.nav_logout:
                signOut();
                break;

            default:
                return false;
        }

        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    private void setFragment(Fragment fragment)
    {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(frameLayout.getId(), fragment).commit();
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