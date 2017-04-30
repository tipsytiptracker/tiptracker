package com.example.ronjc.tiptracker.utils;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.ronjc.tiptracker.GoalGraphFragment;
import com.example.ronjc.tiptracker.RepeatedFragment;
import com.example.ronjc.tiptracker.SetBudgetGoalFragment;
import com.example.ronjc.tiptracker.model.Period;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Gene Hernandez, Ronald Mangiliman
 * Created by Gene Hernandez on 4/20/2017.
 */

public class BudgetGoalTabAdapter extends FragmentPagerAdapter{

    private static int NUM_PAGES = 3;
    private String periodID;
    private String[] headers = {"Budget Goal", "Repeated Items", "Graphs"};

    public BudgetGoalTabAdapter(FragmentManager fragmentManager, String periodID) {
        super(fragmentManager);
        this.periodID = periodID;
    }

    @Override
    public int getCount() {
        return NUM_PAGES;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return headers[position];
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0: {
                return SetBudgetGoalFragment.newInstance(periodID);
            }
            case 1: {
                return RepeatedFragment.newInstance(periodID);
            }
            case 2: {
                return GoalGraphFragment.newInstance(periodID);
            }
            default: {
                return null;
            }
        }
    }

}
