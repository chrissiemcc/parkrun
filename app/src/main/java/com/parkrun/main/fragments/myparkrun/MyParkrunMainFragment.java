package com.parkrun.main.fragments.myparkrun;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parkrun.main.R;
import com.parkrun.main.adapters.ViewPagerAdapter;

public class MyParkrunMainFragment extends Fragment
{

    public MyParkrunMainFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_my_parkrun, container, false);

        TabLayout tabLayout = layout.findViewById(R.id.my_parkrun_tabs);
        ViewPager viewPager = layout.findViewById(R.id.my_parkrun_viewpager);

        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());

        //Adding fragments
        adapter.setFragment(new MyParkrunNewsFragment(), "New");
        adapter.setFragment(new MyParkrunPhotosFragment(), "Photos");
        adapter.setFragment(new MyParkrunResultsFragment(), "Results");

        //Adapter setup
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        return layout;
    }

}