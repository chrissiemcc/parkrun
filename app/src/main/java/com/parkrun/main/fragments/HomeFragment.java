package com.parkrun.main.fragments;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
import com.parkrun.main.Manifest;
import com.parkrun.main.R;
import com.parkrun.main.objects.Channel;
import com.parkrun.main.objects.Forecast;
import com.parkrun.main.objects.Item;
import com.parkrun.main.service.WeatherServiceCallback;
import com.parkrun.main.service.YahooWeatherService;

import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment implements WeatherServiceCallback {
    private static final int MY_PERMISSION_REQUEST_LOCATION = 1;
    private String locationName = "";

    private ImageView imageWeather;
    private TextView tvWeather;

    private FirebaseUser firebaseUser;

    private LocationManager locationManager;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvHome = view.findViewById(R.id.tvHome);
        tvWeather = view.findViewById(R.id.tvWeather);

        imageWeather = view.findViewById(R.id.imageWeather);

        //check permissions of device
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            if(ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION))
            {
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSION_REQUEST_LOCATION);
            }
            else
            {
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSION_REQUEST_LOCATION);
            }
        }
        else
        {
            locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            try
            {
                locationName = getLocation(location.getLatitude(), location.getLongitude());

                YahooWeatherService service = new YahooWeatherService(this);
                service.refreshWeather(locationName);

                Log.d("Testing", "Permission already granted");
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        FirebaseAuth authentication = FirebaseAuth.getInstance();
        firebaseUser = authentication.getCurrentUser();

        Log.d("Debug", "Home fragment arrived!");

        String welcome;

        if(firebaseUser.getDisplayName()!=null)
        {
            welcome = "Welcome " + firebaseUser.getDisplayName() + "!";
        }
        else
        {
            welcome = "Welcome parkrunner!";
        }

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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        switch (requestCode)
        {
            case MY_PERMISSION_REQUEST_LOCATION:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {

                    if(ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    {
                        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                        try
                        {
                            locationName = getLocation(location.getLatitude(), location.getLongitude());

                            YahooWeatherService service = new YahooWeatherService(this);
                            service.refreshWeather(locationName);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                        Log.d("Testing", "Permission granted");
                    }
                    else
                    {
                        Log.d("Testing", "No permission");
                    }
                }
                break;
            default:
                break;
        }
    }

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
        Log.d("Testing", weatherText);
        tvWeather.setText(weatherText);
    }// yahoo weather service success

    @Override
    public void serviceFailure(Exception exception)
    {
        Toast.makeText(getActivity(), exception.getMessage(), Toast.LENGTH_LONG).show();
    }

    public String getLocation(double lat, double lon)
    {
        String currentCity = "";

        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
        List<Address> addressList;

        try
        {
            addressList = geocoder.getFromLocation(lat, lon, 1);
            if(addressList.size() > 0)
            {
                currentCity = addressList.get(0).getLocality();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return currentCity;
    }//get nearest city name
}