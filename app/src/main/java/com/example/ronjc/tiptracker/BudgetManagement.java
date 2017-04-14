package com.example.ronjc.tiptracker;

import android.graphics.Typeface;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.ronjc.tiptracker.utils.BudgetPageAdapter;
import com.example.ronjc.tiptracker.utils.FontManager;

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_management);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.tiptrackerlogo3);
        getSupportActionBar().setTitle("");

        ButterKnife.bind(this);

        mViewPager.setAdapter(new BudgetPageAdapter(getSupportFragmentManager(), BudgetManagement.this));
        mTabLayout.setupWithViewPager(mViewPager);

        Typeface bitter = FontManager.getTypeface(getApplicationContext(), FontManager.BITTER);
        FontManager.markAsIconContainer(findViewById(R.id.sliding_tabs), bitter);

        Typeface iconFont = FontManager.getTypeface(getApplicationContext(), FontManager.FONTAWESOME);
        mLeftArrowIcon.setTypeface(iconFont);
        mRightArrowIcon.setTypeface(iconFont);
    }
}
