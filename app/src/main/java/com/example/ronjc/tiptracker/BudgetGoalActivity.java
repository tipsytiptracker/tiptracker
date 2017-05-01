//https://github.com/PhilJay/MPAndroidChart/wiki/Getting-Started reference for line graph
package com.example.ronjc.tiptracker;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.blackcat.currencyedittext.CurrencyEditText;
import com.example.ronjc.tiptracker.model.Expense;
import com.example.ronjc.tiptracker.model.Income;
import com.example.ronjc.tiptracker.model.Period;
import com.example.ronjc.tiptracker.model.User;
import com.example.ronjc.tiptracker.utils.BudgetGoalTabAdapter;
import com.example.ronjc.tiptracker.utils.DBHelper;
import com.example.ronjc.tiptracker.utils.DateManager;
import com.example.ronjc.tiptracker.utils.FontManager;
import com.example.ronjc.tiptracker.utils.Utils;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Calendar;


import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.ronjc.tiptracker.utils.FontManager.BITTER;

//Activity that allows Users to set a budget goal and
//view progress through graphs (previous budget goals, income and expenses)

public class BudgetGoalActivity extends AppCompatActivity {

    @BindView(R.id.budget_goal_pager) ViewPager budgetGoalPager;
    @BindView(R.id.goal_sliding_tabs) TabLayout mTabLayout;

    private BudgetGoalTabAdapter budgetGoalTabAdapter;

    private DatabaseReference dbRef;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;
    private String periodID;

    private ArrayList<String> temp;

    private Typeface bitter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_goal);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.tiptrackerlogo3);
        getSupportActionBar().setTitle("");
        ButterKnife.bind(this);

        bitter = FontManager.getTypeface(getApplicationContext(), FontManager.BITTER);
        FontManager.markAsIconContainer(findViewById(R.id.budget_goal_activity), bitter);


        temp = new ArrayList<String>();

        dbRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        getPeriodId();
    }

    /**
     * Methods to find IDs from the database to retrieve for the graphs' respective values
     */
    private void getPeriodId(){//Gets the key for the user's period session
        dbRef.child(DBHelper.USERS).child(user.getUid()).child(DBHelper.PERIODS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Get key for period to write
                Iterable<DataSnapshot> periods = dataSnapshot.getChildren();
                //Get last Sunday in milliseconds
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                Date startDate = calendar.getTime();
                boolean exist = true;

                final long time = DateManager.trimMilliseconds(startDate.getTime());

                for (DataSnapshot period : periods) {
                    if ((long) period.getValue() == time) {
                        periodID = period.getKey();
                        exist = false;
                        break;
                    }
                }
                setBudgetGoalAdapter();

                if(exist){ //create a period if it doesn't exist
                    String k = dbRef.child(DBHelper.USERS).child(user.getUid()).child(DBHelper.PERIODS).push().getKey();
                    dbRef.child(DBHelper.USERS).child(user.getUid()).child(DBHelper.PERIODS).child(k).setValue(time);
                    periodID = k;

                    calendar.add(Calendar.DATE, 6);
                    calendar.add(Calendar.HOUR_OF_DAY, 23);
                    calendar.add(Calendar.MINUTE, 59);
                    calendar.add(Calendar.SECOND, 59);
                    Date endDate = calendar.getTime();
                    long end = DateManager.trimMilliseconds(endDate.getTime());

                    Period mPeriod = new Period(time, end, null, null, 0.00, 0.00, 0.00);
                    dbRef.child("periods").child(periodID).setValue(mPeriod);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Used for handling Async nature of Firebase
     */
    private void setBudgetGoalAdapter() {
        budgetGoalTabAdapter = new BudgetGoalTabAdapter(getSupportFragmentManager(), periodID);
        budgetGoalPager.setAdapter(budgetGoalTabAdapter);
        mTabLayout.setupWithViewPager(budgetGoalPager);
        changeTabFont();
    }

    /**
     * Changes tab font
     *
     * http://stackoverflow.com/questions/31067265/change-the-font-of-tab-text-in-android-design-support-tablayout
     */
    private void changeTabFont() {
        Typeface bitter = FontManager.getTypeface(getApplicationContext(), FontManager.BITTER);

        ViewGroup vg = (ViewGroup) mTabLayout.getChildAt(0);
        int tabsCount = vg.getChildCount();
        for (int j = 0; j < tabsCount; j++) {
            ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
            int tabChildsCount = vgTab.getChildCount();
            for (int i = 0; i < tabChildsCount; i++) {
                View tabViewChild = vgTab.getChildAt(i);
                if (tabViewChild instanceof TextView) {
                    ((TextView) tabViewChild).setTypeface(bitter);
                }
            }
        }
    }
}
