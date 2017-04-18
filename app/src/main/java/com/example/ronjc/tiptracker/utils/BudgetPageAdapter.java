package com.example.ronjc.tiptracker.utils;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

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

public class BudgetPageAdapter extends FragmentPagerAdapter{

    private final int NUM_ITEMS = 2;
    private String tabTitles[] = new String[] {"Income", "Expenses"};
    private Date startDate, endDate;
    private ArrayList<Expense> expenses;
    private ArrayList<Income> incomes;
    private Context context;

    //User ID being passed from BudgetManagement
    private String userID;

    public BudgetPageAdapter(FragmentManager fragmentManager, Context context, Period period, String userID) {
        super(fragmentManager);
        this.context = context;
        this.startDate = new Date(period.getStartDate());
        this.endDate = new Date(period.getEndDate());
        this.expenses = period.getExpenses();
        this.incomes = period.getIncomes();
        this.userID = userID;
    }

    @Override
    public int getCount() {
        return NUM_ITEMS;
    }

    @Override
    public Fragment getItem(int position) {
        return position == 0 ? BudgetFragment.newInstance(position + 1, incomes, startDate, userID) : BudgetFragment.newInstance(position + 1, expenses, startDate, userID);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }
}
