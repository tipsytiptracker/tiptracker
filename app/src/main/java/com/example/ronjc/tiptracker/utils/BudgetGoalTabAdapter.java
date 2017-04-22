package com.example.ronjc.tiptracker.utils;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.ronjc.tiptracker.model.Period;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by geneh on 4/20/2017.
 */

public class BudgetGoalTabAdapter extends FragmentPagerAdapter{
    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();

    public void addFragment(Fragment fragment, String title) {
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
    }
    public BudgetGoalTabAdapter(FragmentManager fragmentManager) {super(fragmentManager);}

    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentTitleList.get(position);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);

    }

    @Override
    public int getCount() {
        return mFragmentList.size();

    }
}
