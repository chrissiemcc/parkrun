package com.parkrun.main.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerAdapter extends FragmentPagerAdapter
{
    private final List<Fragment> fragmentList = new ArrayList<>();
    private final List<String> fragmentListNames = new ArrayList<>();

    public ViewPagerAdapter(FragmentManager fm)
    {
        super(fm);
    }

    @Override
    public Fragment getItem(int position)
    {
        return fragmentList.get(position);
    }

    @Override
    public int getCount()
    {
        return fragmentListNames.size();
    }

    @Override
    public CharSequence getPageTitle(int position)
    {
        return fragmentListNames.get(position);
    }

    public void setFragment(Fragment fragment, String name)
    {
        fragmentList.add(fragment);
        fragmentListNames.add(name);
    }
}
