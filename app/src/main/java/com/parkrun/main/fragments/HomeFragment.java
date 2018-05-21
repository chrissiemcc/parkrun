package com.parkrun.main.fragments;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.parkrun.main.R;
import com.parkrun.main.objects.Channel;
import com.parkrun.main.service.WeatherServiceCallback;
import com.parkrun.main.service.YahooWeatherService;

public class HomeFragment extends Fragment implements WeatherServiceCallback
{
    private ImageView imageWeather;
    private TextView tvWeather;

    private YahooWeatherService service;

    public HomeFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        weatherThread();

        TextView tvHome = view.findViewById(R.id.tvHome);
        tvWeather = view.findViewById(R.id.tvWeather);

        imageWeather = view.findViewById(R.id.imageWeather);

        service = new YahooWeatherService(this);
        service.refreshWeather("Carrickfergus, United Kingdom");

        FirebaseAuth authentication = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authentication.getCurrentUser();

        Log.d("Debug", "Home fragment arrived!");

        String welcome = "Welcome " + firebaseUser.getDisplayName() + "!";
        tvHome.setText(welcome);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        NavigationView navigationView = getActivity().findViewById(R.id.navigation_view);
        MenuItem menuItem = navigationView.getMenu().findItem(R.id.nav_home);

        if(!menuItem.isChecked())
        {
            menuItem.setChecked(true);
        }
    }
    //To make sure the item checked in the navigation menu is always correct, even on back press

    private void weatherThread()
    {
        Runnable weatherRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                //weather code
            }
        };

        Thread weatherThread = new Thread(weatherRunnable);
        weatherThread.start();
    }

    @Override
    public void serviceSuccess(Channel channel)
    {

    }

    @Override
    public void serviceFailure(Exception exception)
    {
        Toast.makeText(getActivity(), exception.getMessage(), Toast.LENGTH_LONG).show();
    }
}