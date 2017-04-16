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
 * Created by ronjc on 4/14/2017.
 */

public class BudgetPageAdapter extends FragmentPagerAdapter{

    private final int NUM_ITEMS = 2;
    private String tabTitles[] = new String[] {"Income", "Expenses"};
    private Date startDate, endDate;
    private ArrayList<Expense> expenses;
    private ArrayList<Income> incomes;
    private Context context;

    public BudgetPageAdapter(FragmentManager fragmentManager, Context context, Period period) {
        super(fragmentManager);
        this.context = context;
        this.startDate = period.getStartDate();
        this.endDate = period.getEndDate();
        this.expenses = period.getExpenses();
        this.incomes = period.getIncomes();
    }

    @Override
    public int getCount() {
        return NUM_ITEMS;
    }

    @Override
    public Fragment getItem(int position) {

        return position == 0 ? BudgetFragment.newInstance(position + 1, incomes) : BudgetFragment.newInstance(position + 1, expenses);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }
}
