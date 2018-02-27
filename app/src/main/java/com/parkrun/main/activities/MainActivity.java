package com.parkrun.main.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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
import com.parkrun.main.R;
import com.parkrun.main.fragments.HomeFragment;
import com.parkrun.main.fragments.InfoFragment;
import com.parkrun.main.fragments.MyClubFragment;
import com.parkrun.main.fragments.myparkrun.MyParkrunMainFragment;
import com.parkrun.main.fragments.ResultsFragment;
import com.parkrun.main.fragments.VolunteerFragment;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private DrawerLayout drawerLayout;
    private FrameLayout frameLayout;
    private HomeFragment homeFragment;
    private VolunteerFragment volunteerFragment;
    private ResultsFragment resultsFragment;
    private MyParkrunMainFragment parkrunFragment;
    private MyClubFragment clubFragment;
    private InfoFragment infoFragment;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.action_bar);
        setSupportActionBar(toolbar);
        //Initialise and set the action bar

        drawerLayout = findViewById(R.id.drawerLayout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //Add menu button to the action bar

        navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        //Initialise the navigation view and set listener

        frameLayout = findViewById(R.id.main_frame);
        homeFragment = new HomeFragment();
        volunteerFragment = new VolunteerFragment();
        resultsFragment = new ResultsFragment();
        parkrunFragment = new MyParkrunMainFragment();
        clubFragment = new MyClubFragment();
        infoFragment = new InfoFragment();
        //Initialise fragments for each nav page

        setFragment(homeFragment, "Home");
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
        navigationView = findViewById(R.id.navigation_view);
        int id = item.getItemId();

        if(!navigationView.getMenu().findItem(id).isChecked()) // if menu item is already selected, don't refresh
        {
            String name;

            switch (id)
            {
                case R.id.nav_home:
                    name = "Home";
                    setFragment(homeFragment, name);
                    break;

                case R.id.nav_volunteer:
                    name = "Volunteer";
                    setFragment(volunteerFragment, name);
                    break;

                case R.id.nav_results:
                    name = "Results";
                    setFragment(resultsFragment, name);
                    break;

                case R.id.nav_my_parkrun:
                    name = "My parkrun";
                    setFragment(parkrunFragment, name);
                    break;

                case R.id.nav_my_club:
                    name = "My Club";
                    setFragment(clubFragment, name);
                    break;

                case R.id.nav_info:
                    name = "parkrun Info";
                    setFragment(infoFragment, name);
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
        }

        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    private void setFragment(Fragment fragment, String name)
    {
        getSupportActionBar().setTitle(name);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction
                .replace(frameLayout.getId(), fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .addToBackStack(name)
                .commit();
    }

    @Override
    public void onBackPressed()
    {
        drawerLayout = findViewById(R.id.drawerLayout);

        FragmentManager fragmentManager = getSupportFragmentManager();

        if(drawerLayout.isDrawerOpen(GravityCompat.START))
        {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else
        {
            int fragments = fragmentManager.getBackStackEntryCount();

            if (fragments == 1)
            {
                finish();
                // finish activity if no fragments remain in back stack
            }
            else
            {
                if (fragments > 1)
                {
                    fragmentManager.popBackStack();
                    getSupportActionBar().setTitle(fragmentManager.getBackStackEntryAt(fragments-2).getName());
                    // To ensure the action bar title is always correct when back is pressed
                }
                else
                {
                    super.onBackPressed();
                }
            }
        }
    }
}