package com.example.ronjc.tiptracker;

import android.graphics.Typeface;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.ronjc.tiptracker.model.Expense;
import com.example.ronjc.tiptracker.model.Income;
import com.example.ronjc.tiptracker.model.Period;
import com.example.ronjc.tiptracker.utils.BudgetPageAdapter;
import com.example.ronjc.tiptracker.utils.FontManager;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BudgetManagement extends AppCompatActivity {

    @BindView(R.id.left_arrow_icon)
    TextView mLeftArrowIcon;
    @BindView(R.id.right_arrow_icon)
    TextView mRightArrowIcon;
    @BindView(R.id.budget_view_pager)
    ViewPager mViewPager;
    @BindView(R.id.sliding_tabs)
    TabLayout mTabLayout;
    @BindView(R.id.date_tv)
    TextView mDateTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_management);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.tiptrackerlogo3);
        getSupportActionBar().setTitle("");

        ButterKnife.bind(this);

        /*
            Bunch of testing down hur
         */
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");

        Date startDate= new Date(1492228800000L);
        Date endDate = new Date(1492401599000L);

        mDateTextView.setText("" + simpleDateFormat.format(startDate) + " - " + simpleDateFormat.format(endDate));
        Income income1 = new Income("01", "Paycheck", new BigDecimal(2000.00), new Date(1492299999123L), "Salary");
        Income income2 = new Income("02", "Money from Mom", new BigDecimal(50.00), new Date(1492295342323L), "Gifts");
        Income income3 = new Income("03", "Money found on sidewalk", new BigDecimal(20.00), new Date(1492295342123L), "Misc.");
        ArrayList<Income> incomeList = new ArrayList<Income>();
        incomeList.add(income1);
        incomeList.add(income2);
        incomeList.add(income3);
        Expense expense1 = new Expense("01", "Rent", new BigDecimal(850.00), new Date(1492299999123L), "Rent");
        Expense expense2 = new Expense("02", "Bagels", new BigDecimal(5.00), new Date(1492299999123L), "Grocery");
        Expense expense3 = new Expense("03", "Eggs", new BigDecimal(2.99), new Date(1492299999123L), "Grocery");
        Expense expense4 = new Expense("04", "Cheese", new BigDecimal(1.99), new Date(1492299999123L), "Grocery");
        Expense expense5 = new Expense("05", "Coffee", new BigDecimal(1.99), new Date(1492299999123L), "Coffee");
        ArrayList<Expense> expenseList = new ArrayList<Expense>();
        expenseList.add(expense1);
        expenseList.add(expense2);
        expenseList.add(expense3);
        expenseList.add(expense4);
        expenseList.add(expense5);

        Period period = new Period(startDate, endDate, incomeList, expenseList, new BigDecimal(500.00));
        mViewPager.setAdapter(new BudgetPageAdapter(getSupportFragmentManager(), BudgetManagement.this, period));
        mTabLayout.setupWithViewPager(mViewPager);

        Typeface bitter = FontManager.getTypeface(getApplicationContext(), FontManager.BITTER);
        FontManager.markAsIconContainer(findViewById(R.id.sliding_tabs), bitter);

        Typeface iconFont = FontManager.getTypeface(getApplicationContext(), FontManager.FONTAWESOME);
        mLeftArrowIcon.setTypeface(iconFont);
        mRightArrowIcon.setTypeface(iconFont);
    }
}
