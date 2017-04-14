package com.example.ronjc.tiptracker.utils;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.ronjc.tiptracker.BudgetFragment;

/**
 * Created by ronjc on 4/14/2017.
 */

public class BudgetPageAdapter extends FragmentPagerAdapter{

    private final int NUM_ITEMS = 2;
    private String tabTitles[] = new String[] {"Income", "Expenses"};
    private Context context;

    public BudgetPageAdapter(FragmentManager fragmentManager, Context context) {
        super(fragmentManager);
        this.context = context;
    }

    @Override
    public int getCount() {
        return NUM_ITEMS;
    }

    @Override
    public Fragment getItem(int position) {
        return BudgetFragment.newInstance(position + 1);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }
}
