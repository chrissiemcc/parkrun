package com.parkrun.main.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parkrun.main.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

public class HomeFragment extends Fragment
{
    View layout;
    String tableData;
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            TextView textView = layout.findViewById(R.id.jsoup_test);

            textView.setText(tableData);
        }
    };

    public HomeFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        layout = inflater.inflate(R.layout.fragment_home, container, false);

        runJsoupThread();

        return layout;
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

    private void runJsoupThread()
    {
        Runnable jsoupRun = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Document document = Jsoup.connect("http://www.parkrun.org.uk/carrickfergus/results/athletehistory/?athleteNumber=763139").get();

                    Element resultsTable = document.selectFirst("caption:contains(All Results)").parent();

                    tableData = resultsTable.text();

                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
               // handler.sendEmptyMessage(0);
            }
        };

        Thread jsoupThread = new Thread(jsoupRun);
        jsoupThread.start();
    }
}