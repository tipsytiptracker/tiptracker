package com.example.ronjc.tiptracker;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ronjc.tiptracker.model.Expense;
import com.example.ronjc.tiptracker.model.Income;
import com.example.ronjc.tiptracker.model.Period;
import com.example.ronjc.tiptracker.utils.BudgetPageAdapter;
import com.example.ronjc.tiptracker.utils.Camera;
import com.example.ronjc.tiptracker.utils.DBHelper;
import com.example.ronjc.tiptracker.utils.DateManager;
import com.example.ronjc.tiptracker.utils.FontManager;
import com.example.ronjc.tiptracker.utils.OCR;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

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

    //ArrayList of incomes and expenses objects
    private ArrayList<Income> incomeList;
    private ArrayList<Expense> expenseList;

    //List of income and expense keys belonging to user
    private ArrayList<String> incomeKeys;
    private ArrayList<String> expenseKeys;

    //Total amount that income and expenses add up to
    private double totalIncome;
    private double totalExpense;

    //ID of the current period
    private String currentPeriodID;

    //Counters for pending incomes and expense. Workaround for handling async nature of Firebase
    private int pendingIncome = 0;
    private int pendingExpense = 0;

    private BudgetPageAdapter mBudgetPageAdapter;
    private ProgressDialog mProgressDialog;
    private Period period;

    private Camera mCamera;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_management);

        //Add logo to action bar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.tiptrackerlogo3);
        getSupportActionBar().setTitle("");

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(getString(R.string.retrieving));
        mProgressDialog.setMessage(getString(R.string.please_wait));
        mProgressDialog.setCancelable(false);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        ButterKnife.bind(this);
        simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
        //method call to set start and end date
        calculateStartAndEndDate();

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        retrieveCurrentPeriod();

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

        mCamera = new Camera(this);
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
        retrieveCurrentPeriod();
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
        retrieveCurrentPeriod();
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

    /**
     * Read the budge for current periods incomes and expenses
     * Also displays correct date of current period
     */
    private void readBudget() {
        mDateTextView.setText("" + sStartDate + " - " + sEndDate);
        mDatabaseReference.child(DBHelper.PERIODS).child(currentPeriodID).child(DBHelper.INCOMES).addListenerForSingleValueEvent(new RetrieveIncomeKeys());
        mDatabaseReference.child(DBHelper.PERIODS).child(currentPeriodID).child(DBHelper.EXPENSES).addListenerForSingleValueEvent(new RetrieveExpenseKeys());
    }

    /**
     * Sub class that iterates through all income associated with a current period and adds their key
     * to array list
     */
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

    /**
     * Sub class that iterates through all expenses associated with a current period and adds their key
     * to array list
     */
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

    /**
     * Sub class that gets income from database and adds it into local array list as an instance of
     * Income class.
     */
    private class RetrieveIncome implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Income income = dataSnapshot.getValue(Income.class);
            incomeList.add(income);

            //decrement number of pending incomce
            pendingIncome--;

            //due to asynchronous nature of Firebase, we must check to see if there are no remaining
            //pending income and expense. If not, display the items.
            if(pendingIncome == 0 && pendingExpense == 0) {
                displayItems();
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }

    /**
     * Sub class that gets expenses from database and adds it into local array list as instance of
     * expense
     */
    private class RetrieveExpense implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Expense expense = dataSnapshot.getValue(Expense.class);
            expenseList.add(expense);

            //decrement pending exenses
            pendingExpense--;

            //if no more pending expenses and income, display items
            if(pendingExpense == 0 && pendingIncome == 0) {
                displayItems();
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }

    /**
     * Iterates through arraylist of income or expenses keys and retrieves each matching income/expense
     * from the database
     * @param type
     */
    private void iterateKeys(String type) {

        //If iterating through incomes
        if (type.equals(DBHelper.INCOMES)) {
            pendingIncome = incomeKeys.size();

            //if there are no incomes and expenses to iterate through, display UI
            if(pendingIncome == 0 && pendingExpense == 0) {
                displayItems();
            }

            //Otherwise, retrieve income
            for(String key : incomeKeys) {
                mDatabaseReference.child(DBHelper.INCOMES).child(key).addListenerForSingleValueEvent(new RetrieveIncome());
            }

        //Following code functions the same as above, except with expenses
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

    /**
     * Displays the UI of incomes and expenses
     */
    private void displayItems() {

        //Calculate the total amount of money for income and expenses
        totalIncome = calculateTotalIncome(incomeList);
        totalExpense = calculateTotalExpense(expenseList);

        //Create new Period object
        period = new Period(startDate.getTime(), endDate.getTime(), incomeList, expenseList, 500.00, totalIncome, totalExpense);

        //Remove any adapter for ViewPager, if there is one. This is to update UI when switching dates
        mViewPager.setAdapter(null);

        //Create new Adapter for ViewPager
        mBudgetPageAdapter = new BudgetPageAdapter(getSupportFragmentManager(), BudgetManagement.this, period, mFirebaseUser.getUid(), currentPeriodID, mCamera);

        //Set adapter for ViewPager
        mViewPager.setAdapter(mBudgetPageAdapter);
        mTabLayout.setupWithViewPager(mViewPager);

        //Hide Progress Dialog
        showProgress(false);
    }

    /**
     * Retrieves the current Period that user is on
     */
    private void retrieveCurrentPeriod() {

        //Displays loading dialog
        showProgress(true);

        //Create new arraylists for keys and incomes and expenses
        incomeKeys = new ArrayList<String>();
        expenseKeys = new ArrayList<String>();
        incomeList = new ArrayList<Income>();
        expenseList = new ArrayList<Expense>();


        //Checks to see if user has a period associated with them in the database that corresponds to the current period
        mDatabaseReference.child(DBHelper.USERS).child(mFirebaseUser.getUid()).child(DBHelper.PERIODS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //Check if there are periods associated with the user
                if(dataSnapshot.getChildrenCount() > 0 ) {
                    boolean foundPeriod = false;

                    //Get all periods fo this user
                    Iterable<DataSnapshot> periods = dataSnapshot.getChildren();

                    //Iterate through users
                    for (DataSnapshot period : periods) {

                        //If a period has a matching value in time, set that to current PeriodID
                        if ((long) period.getValue() == DateManager.trimMilliseconds(startDate.getTime())) {
                            currentPeriodID = period.getKey();
                            foundPeriod = true;
                            break;
                        }
                    }

                    //If no period matches, create a new one to the database
                    if(!foundPeriod) {
                        //TODO: change to actual values
                        writeNewPeriod(DateManager.trimMilliseconds(startDate.getTime()),
                                DateManager.trimMilliseconds(endDate.getTime()),
                                new ArrayList<Income>(), new ArrayList<Expense>(),
                                500.00, 600.00, 400.00);
                    }
                    readBudget();

                //Case where if user has no periods written in database
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
    }

    private void showProgress(boolean show) {
        if(show) {
            mProgressDialog.show();
        } else {
            mProgressDialog.dismiss();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Camera.REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            Bitmap mBitmap = mCamera.getBitmap();
            OCR mOCR = new OCR(this, mBitmap);
            String ocrString = mOCR.getTotal();
            LayoutInflater mLayoutInflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            Typeface bitter = FontManager.getTypeface(getApplicationContext(), FontManager.BITTER);

            displayAddDialog(mLayoutInflator, bitter, mCamera.getHeader(), ocrString, mCamera.getType());

//            Toast.makeText(this, ocrString, Toast.LENGTH_SHORT).show();
        }
    }


    //Horrible workaround needed because bad design lol
    private void displayAddDialog(LayoutInflater mLayoutInflator, Typeface bitter, final String headerTitle, String cameraAmount, final String type) {

        //Create dialog
        AlertDialog.Builder pencilBuilder = new AlertDialog.Builder(this);
        final View pencilView = mLayoutInflator.inflate(R.layout.add_budget_manually, null);
        ((TextView) pencilView.findViewById(R.id.budget_manually_header)).setTypeface(bitter);
        TextView pencilHeader = (TextView) pencilView.findViewById(R.id.budget_manually_header);
        pencilHeader.setTypeface(bitter);
        pencilHeader.setText(getString(R.string.add) + " " + headerTitle);
        final TextInputLayout itemName = (TextInputLayout) pencilView.findViewById(R.id.add_item_name_text_input);
        final EditText itemNameEditText = (EditText) pencilView.findViewById(R.id.add_item_name);
        itemNameEditText.setTypeface(bitter);
        TextInputLayout itemAmount = (TextInputLayout) pencilView.findViewById(R.id.add_item_amount_text_input);
        final EditText itemAmountEditText = (EditText) pencilView.findViewById(R.id.item_amount);
        itemAmountEditText.setText(cameraAmount);
        itemAmountEditText.setTypeface(bitter);
        Button itemButton = (Button) pencilView.findViewById(R.id.add_item_manually_button);

        //Set font styling
        itemName.setTypeface(bitter);
        itemAmount.setTypeface(bitter);
        itemButton.setTypeface(bitter);

        //Show dialog
        pencilBuilder.setView(pencilView);
        final AlertDialog pencilDialog = pencilBuilder.create();

        //Write new income or expense
        itemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (type.equals(DBHelper.INCOMES)) {
                    writeNewIncome(itemNameEditText.getText().toString(), itemAmountEditText.getText().toString(), headerTitle);
                } else {
                    writeNewExpense(itemNameEditText.getText().toString(), itemAmountEditText.getText().toString(), headerTitle);
                }
                pencilDialog.dismiss();
            }
        });
        pencilDialog.show();
    }

    private void writeNewIncome(final String name, final String amount, final String category) {
        final double doubleAmount = Double.parseDouble(amount.substring(1).replace(",", ""));

        BigDecimal bigDecimal1 = new BigDecimal(totalIncome);
        bigDecimal1 = bigDecimal1.setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal bigDecimal2 = new BigDecimal(doubleAmount);
        bigDecimal2 = bigDecimal2.setScale(2, BigDecimal.ROUND_HALF_UP);

        totalIncome = bigDecimal1.add(bigDecimal2).doubleValue();

        String incomeKey = mDatabaseReference.child(DBHelper.PERIODS).child(currentPeriodID).child(DBHelper.INCOMES).push().getKey();
        mDatabaseReference.child(DBHelper.PERIODS).child(currentPeriodID).child(DBHelper.INCOMES).child(incomeKey).setValue(true);

        //TODO: Find solution for this. Currently, if they add to this from a past or future period, then its date will be out of the bounds of the actual period
        Income income = new Income(incomeKey, name, doubleAmount, System.currentTimeMillis(), category, mFirebaseUser.getUid());
        mDatabaseReference.child(DBHelper.INCOMES).child(incomeKey).setValue(income);

        //alert user of success
        Toast.makeText(this, getString(R.string.income_added), Toast.LENGTH_SHORT).show();
        incomeList.add(income);
        displayItems();
    }

    private void writeNewExpense(String name, String amount, String category) {
        double doubleAmount = Double.parseDouble(amount.substring(1));

        BigDecimal bigDecimal1 = new BigDecimal(totalExpense);
        bigDecimal1 = bigDecimal1.setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal bigDecimal2 = new BigDecimal(doubleAmount);
        bigDecimal2 = bigDecimal2.setScale(2, BigDecimal.ROUND_HALF_UP);

        totalExpense = bigDecimal1.add(bigDecimal2).doubleValue();

        String expenseKey = mDatabaseReference.child(DBHelper.PERIODS).child(currentPeriodID).child(DBHelper.EXPENSES).push().getKey();
        final Expense expense = new Expense(expenseKey, name, doubleAmount, System.currentTimeMillis(), category, mFirebaseUser.getUid());
        mDatabaseReference.child(DBHelper.PERIODS).child(currentPeriodID).child(DBHelper.EXPENSES).child(expenseKey).setValue(true);
        mDatabaseReference.child(DBHelper.EXPENSES).child(expenseKey).setValue(expense);
        Toast.makeText(this, getString(R.string.expense_added), Toast.LENGTH_SHORT).show();
        expenseList.add(expense);
        displayItems();
    }
}
