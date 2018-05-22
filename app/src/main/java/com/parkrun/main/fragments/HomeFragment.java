package com.parkrun.main.fragments;

import android.graphics.drawable.Drawable;
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
import com.parkrun.main.objects.Forecast;
import com.parkrun.main.objects.Item;
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

        TextView tvHome = view.findViewById(R.id.tvHome);
        tvWeather = view.findViewById(R.id.tvWeather);

        imageWeather = view.findViewById(R.id.imageWeather);

        service = new YahooWeatherService(this);
        service.refreshWeather("Carrickfergus, Northern Ireland");

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

    @Override
    public void serviceSuccess(Channel channel)
    {
        int index = 1;
        Item item = channel.getItem();
        Forecast[] forecast = item.getForecast();
        Forecast saturday = null;

        for(Forecast day : forecast)
        {
            if(day.getDay().equals("Sat") && index != 1)
            {
                saturday = day;
                break;
            }
            if(index == 1)
                index++;//avoid giving forecast for today if today = saturday
        }//get forecast for Saturday

        int resourceId = getResources().getIdentifier("icon_" + saturday.getCode(), "drawable", getActivity().getPackageName());

        Drawable weatherIconDrawable = getResources().getDrawable(resourceId);

        imageWeather.setImageDrawable(weatherIconDrawable);

        String weatherText = "Local parkruns on Saturday are expected to be: " + saturday.getDescription();
        tvWeather.setText(weatherText);
    }// yahoo weather service success

    @Override
    public void serviceFailure(Exception exception)
    {
        Toast.makeText(getActivity(), exception.getMessage(), Toast.LENGTH_LONG).show();
    }
}