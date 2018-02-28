package com.parkrun.main.fragments.results;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.parkrun.main.R;
import com.parkrun.main.adapters.ViewPagerAdapter;

public class ResultsMainFragment extends Fragment
{

    public ResultsMainFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_results, container, false);

        TabLayout tabLayout = layout.findViewById(R.id.results_tabs);
        ViewPager viewPager = layout.findViewById(R.id.results_viewpager);

        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());

        //Adding fragments
        adapter.setFragment(new ResultsYouFragment(), "You");
        adapter.setFragment(new ResultsFriendsFragment(), "Friends");
        adapter.setFragment(new ResultsOtherFragment(), "Other");

        //Adapter setup
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        return layout;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        NavigationView navigationView = getActivity().findViewById(R.id.navigation_view);
        MenuItem menuItem = navigationView.getMenu().findItem(R.id.nav_results);

        if(!menuItem.isChecked())
        {
            menuItem.setChecked(true);
        }
    }
    //To make sure the item checked in the navigation menu is always correct, even on back press
}