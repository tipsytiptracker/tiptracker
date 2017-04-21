package com.example.ronjc.tiptracker;

import android.graphics.Typeface;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ronjc.tiptracker.model.Expense;
import com.example.ronjc.tiptracker.model.Income;
import com.example.ronjc.tiptracker.model.Period;
import com.example.ronjc.tiptracker.utils.BudgetPageAdapter;
import com.example.ronjc.tiptracker.utils.DBHelper;
import com.example.ronjc.tiptracker.utils.DateManager;
import com.example.ronjc.tiptracker.utils.FontManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Semaphore;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Controller for the BudgetManagement Activity
 *
 * @author Ronald Mangiliman
 */

public class BudgetManagement extends AppCompatActivity {

    //Bind UI views to references
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

    //Start and end date for current period
    private Date startDate, endDate;

    //Strings of start and end dates (for display)
    private String sStartDate, sEndDate;

    //Used to format dates
    private SimpleDateFormat simpleDateFormat;

    //Firebase references
    private DatabaseReference mDatabaseReference;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser mFirebaseUser;

    private ArrayList<Income> incomeList;
    private ArrayList<Expense> expenseList;
    private ArrayList<String> incomeKeys;
    private ArrayList<String> expenseKeys;
    private double totalIncome;
    private double totalExpense;
    private String currentPeriodID;
    private int pendingIncome = 0;
    private int pendingExpense = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_management);

        //Add logo to action bar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.tiptrackerlogo3);
        getSupportActionBar().setTitle("");

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        ButterKnife.bind(this);
        simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
        //method call to set start and end date
        calculateStartAndEndDate();
        incomeList = new ArrayList<Income>();
        expenseList = new ArrayList<Expense>();

        incomeKeys = new ArrayList<String>();
        expenseKeys = new ArrayList<String>();

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        //Checks to see if user has a period associated with them in the database that corresponds to the current period
        mDatabaseReference.child(DBHelper.USERS).child(mFirebaseUser.getUid()).child(DBHelper.PERIODS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount() > 0 ) {
                    Iterable<DataSnapshot> periods = dataSnapshot.getChildren();
                    for (DataSnapshot period : periods) {
                        if ((long) period.getValue() == DateManager.trimMilliseconds(startDate.getTime())) {
                            currentPeriodID = period.getKey();
                            readBudget();
                        }
                    }
                } else {
                        writeNewPeriod(DateManager.trimMilliseconds(startDate.getTime()),
                                DateManager.trimMilliseconds(endDate.getTime()),
                                new ArrayList<Income>(), new ArrayList<Expense>(),
                                500.00, 600.00, 400.00);
                        readBudget();
                    }
                }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //Font styling
        Typeface bitter = FontManager.getTypeface(getApplicationContext(), FontManager.BITTER);
        FontManager.markAsIconContainer(findViewById(R.id.sliding_tabs), bitter);
        Typeface iconFont = FontManager.getTypeface(getApplicationContext(), FontManager.FONTAWESOME);
        mLeftArrowIcon.setTypeface(iconFont);
        mRightArrowIcon.setTypeface(iconFont);

        //Set on click listeners for right and left date arrow icons
        mRightArrowIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToNextWeek();
            }
        });
        mLeftArrowIcon.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                goToLastWeek();
            }
        });
    }

    /**
     * Calculates the start and end dates for the current period
     */
    private void calculateStartAndEndDate() {
        //Get current calendar instance
        Calendar calendar = Calendar.getInstance();
        //Set the day to Sunday of this week
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        //Set hour, seconds, minutes to 0 so that starting range for week is the Sunday of current
        //week at 12:00:00AM
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        //Return as Date object
        startDate = calendar.getTime();
        //Add six days to that week's Sunday
        calendar.add(Calendar.DATE, 6);
        //Set the time to that Saturday at 11:59:59 PM
        calendar.add(Calendar.HOUR_OF_DAY, 23);
        calendar.add(Calendar.MINUTE, 59);
        calendar.add(Calendar.SECOND, 59);
        endDate = calendar.getTime();

        //Format to display to user
        sStartDate = simpleDateFormat.format(startDate);
        sEndDate = simpleDateFormat.format(endDate);
    }

    /**
     * Changes current dates to next Sunday and Saturday, respectively
     * TODO: Will most likely need to refresh the information on the user's screen on call to this method
     */
    private void goToNextWeek() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.add(Calendar.DATE, 7);
        startDate = calendar.getTime();
        //TODO: I think we can just add 7 days to end date for this but its like 3AM and im too tired.
        calendar.add(Calendar.DATE, 6);
        calendar.add(Calendar.HOUR_OF_DAY, 23);
        calendar.add(Calendar.MINUTE, 59);
        calendar.add(Calendar.SECOND, 59);
        endDate = calendar.getTime();
        //Format
        sStartDate = simpleDateFormat.format(startDate);
        sEndDate = simpleDateFormat.format(endDate);
        //Change date on UI
        mDateTextView.setText("" + sStartDate + " - " + sEndDate);
    }

    /**
     * Basically method above, but does the opposite. Yeah, Im really tired.
     */
    private void goToLastWeek() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.add(Calendar.DATE, -7);
        startDate = calendar.getTime();
        calendar.add(Calendar.DATE, 6);
        calendar.add(Calendar.HOUR_OF_DAY, 23);
        calendar.add(Calendar.MINUTE, 59);
        calendar.add(Calendar.SECOND, 59);
        endDate = calendar.getTime();
        sStartDate = simpleDateFormat.format(startDate);
        sEndDate = simpleDateFormat.format(endDate);
        mDateTextView.setText("" + sStartDate + " - " + sEndDate);
    }

    /**
     * Write a new Period to the database as well as creating a corresponding reference to it in
     * the user's tree
     *
     * @param start start date in milliseconds for the period being created
     * @param end end date in milliseconds for the period being created
     * @param income ArrayList of Income for this period. This will sometimes be empty but will take in repeated incomes
     * @param expense ArrayList of Expenses. Once again, can be empty or may contain repeated expenses
     * @param currentBudget The current budget of the user. This is simply Income - Expenses
     * @param allIncome Total amount of all the user's income for this period
     * @param allExpense Total amount of all the user's expenses for this period
     */
    private void writeNewPeriod(long start, long end, ArrayList<Income> income, ArrayList<Expense> expense, double currentBudget, double allIncome, double allExpense) {
        String key = mDatabaseReference.child(DBHelper.USERS).child(mFirebaseUser.getUid()).child(DBHelper.PERIODS).push().getKey();
        mDatabaseReference.child(DBHelper.USERS).child(mFirebaseUser.getUid()).child(DBHelper.PERIODS).child(key).setValue(DateManager.trimMilliseconds(startDate.getTime()));
        Period mPeriod = new Period(start, end, income, expense, currentBudget, allIncome, allExpense);
        mDatabaseReference.child(DBHelper.PERIODS).child(key).setValue(mPeriod);
        currentPeriodID = key;
    }

    /**
     * Calculates the total income of the user using a simple for each loop
     * To keep addition as accurate as possible and avoid any floating point arithmetic errors,
     * BigDecimal was used.
     *
     * @param incomes List of user's incomes
     * @return total amount of user's income
     */
    private double calculateTotalIncome(List<Income> incomes) {
        BigDecimal total = new BigDecimal(0.00);
        total = total.setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal bigDecimal;
        for (Income income : incomes) {
            bigDecimal = new BigDecimal(income.getAmount());
            bigDecimal = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP);
            total = total.add(bigDecimal);
        }
        return total.doubleValue();
    }

    /**
     * Calculates the total expenses of the user using a simple for each loop
     * To keep addition as accurate as possible and avoid any floating point arithmetic errors,
     * BigDecimal was used.
     *
     * @param expenses List of all of user's expenses
     * @return total amount of user's expenses
     */
    private double calculateTotalExpense(List<Expense> expenses) {
        BigDecimal total = new BigDecimal(0.00);
        total = total.setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal bigDecimal;
        for (Expense expense : expenses) {
            bigDecimal = new BigDecimal(expense.getAmount());
            bigDecimal = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP);
            total = total.add(bigDecimal);
        }
        return total.doubleValue();
    }

    private void readBudget() {
        String userID = mFirebaseUser.getUid();
        mDateTextView.setText("" + sStartDate + " - " + sEndDate);
//        mDatabaseReference.child(DBHelper.USERS).child(userID).child(DBHelper.PERIODS).addListenerForSingleValueEvent(new FindCurrentPeriodListener());
        mDatabaseReference.child(DBHelper.PERIODS).child(currentPeriodID).child(DBHelper.INCOMES).addListenerForSingleValueEvent(new RetrieveIncomeKeys());
        mDatabaseReference.child(DBHelper.PERIODS).child(currentPeriodID).child(DBHelper.EXPENSES).addListenerForSingleValueEvent(new RetrieveExpenseKeys());
//        totalIncome = calculateTotalIncome(incomeList);
//        totalExpense = calculateTotalExpense(expenseList);
    }

    private class RetrieveIncomeKeys implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Iterable<DataSnapshot> incomes = dataSnapshot.getChildren();
            for(DataSnapshot income : incomes) {
                incomeKeys.add(income.getKey());
            }
            iterateKeys(DBHelper.INCOMES);
        }
        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }

    private class RetrieveExpenseKeys implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Iterable<DataSnapshot> expenses = dataSnapshot.getChildren();
            for(DataSnapshot expense : expenses) {
                expenseKeys.add(expense.getKey());
            }
            iterateKeys(DBHelper.EXPENSES);
        }
        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }

    private class RetrieveIncome implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Income income = dataSnapshot.getValue(Income.class);
            incomeList.add(income);
            pendingIncome--;
            if(pendingIncome == 0 && pendingExpense == 0) {
                displayItems();
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }

    private class RetrieveExpense implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Expense expense = dataSnapshot.getValue(Expense.class);
            expenseList.add(expense);
            pendingExpense--;
            if(pendingExpense == 0 && pendingIncome == 0) {
                displayItems();
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }

    private void iterateKeys(String type) {
        if (type.equals(DBHelper.INCOMES)) {
            pendingIncome = incomeKeys.size();
            if(pendingIncome == 0 && pendingExpense == 0) {
                displayItems();
            }
            for(String key : incomeKeys) {
                mDatabaseReference.child(DBHelper.INCOMES).child(key).addListenerForSingleValueEvent(new RetrieveIncome());
            }
        } else {
            pendingExpense = expenseKeys.size();
            if(pendingExpense == 0 && pendingIncome == 0) {
                displayItems();
            }
            for(String key : expenseKeys) {
                mDatabaseReference.child(DBHelper.EXPENSES).child(key).addListenerForSingleValueEvent(new RetrieveExpense());
            }
        }
    }

    private void displayItems() {
        Period period = new Period(startDate.getTime(), endDate.getTime(), incomeList, expenseList, 500.00, totalIncome, totalExpense);

        //Create and set up adapter for ViewPager
        mViewPager.setAdapter(new BudgetPageAdapter(getSupportFragmentManager(), BudgetManagement.this, period, mFirebaseUser.getUid(), currentPeriodID));
        //Sets up tabbed layout with ViewPager
        mTabLayout.setupWithViewPager(mViewPager);
    }
}
