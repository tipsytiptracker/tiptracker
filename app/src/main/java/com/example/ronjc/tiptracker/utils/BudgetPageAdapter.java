package com.example.ronjc.tiptracker.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.ronjc.tiptracker.BudgetFragment;
import com.example.ronjc.tiptracker.model.Expense;
import com.example.ronjc.tiptracker.model.Income;
import com.example.ronjc.tiptracker.model.Period;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Custom adapter for BudgetManagement Fragment. Creates a tabbed layout that user can slide left and
 * right to see the incomes/expenses.
 *
 * Much of the code was based off/taken from:
 * https://github.com/codepath/android_guides/wiki/Google-Play-Style-Tabs-using-TabLayout
 *
 * @author Ronald Mangiliman
 * Created on 4/14/2017.
 */

public class BudgetPageAdapter extends FragmentStatePagerAdapter{

    private final int NUM_ITEMS = 2;
    private String tabTitles[] = new String[] {"Income", "Expenses"};
    private Date startDate, endDate;
    private ArrayList<Expense> expenses;
    private ArrayList<Income> incomes;
    private double totalIncomes = 0.00;
    private double totalExpenses = 0.00;
    private Context context;
    private String currentPeriodID;
    //User ID being passed from BudgetManagement
    private String userID;
    private Camera camera;
    private double longitude;
    private double latitude;

    public BudgetPageAdapter(FragmentManager fragmentManager, Context context, Period period,
                             String userID, String currentPeriodID, Camera camera, double longitude,
                             double latitude) {
        super(fragmentManager);
        this.context = context;
        this.startDate = new Date(period.getStartDate());
        this.endDate = new Date(period.getEndDate());
        this.expenses = period.getExpenses();
        this.incomes = period.getIncomes();
        this.totalExpenses = period.getTotalExpenses();
        this.totalIncomes = period.getTotalIncome();
        this.userID = userID;
        this.currentPeriodID = currentPeriodID;
        this.camera = camera;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    @Override
    public int getCount() {
        return NUM_ITEMS;
    }

    @Override
    public Fragment getItem(int position) {
        if(position == 0) {
            return BudgetFragment.newInstance(position + 1, incomes, startDate, userID, currentPeriodID, totalIncomes, camera, longitude, latitude);
        } else {
            return BudgetFragment.newInstance(position + 1, expenses, startDate, userID, currentPeriodID, totalExpenses, camera, longitude, latitude);
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
}
