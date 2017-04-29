//https://github.com/PhilJay/MPAndroidChart/wiki/Getting-Started reference for line graph
package com.example.ronjc.tiptracker;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blackcat.currencyedittext.CurrencyEditText;
import com.example.ronjc.tiptracker.model.Expense;
import com.example.ronjc.tiptracker.model.Income;
import com.example.ronjc.tiptracker.model.Period;
import com.example.ronjc.tiptracker.model.User;
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

    @BindView(R.id.set_budget_et) CurrencyEditText setBudget;
    @BindView(R.id.set_goal_tv) TextView goal;
    String changedGoal;
    @BindView(R.id.change_budget_btn) Button budgetBtn;
    @BindView(R.id.budget_goal_graph) Button budgetGraphbtn;
    @BindView(R.id.income_graph) Button incomeGraphbtn;
    @BindView(R.id.expense_graph) Button expenseGraphbtn;
    DatabaseReference dbRef;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;
    String periodID;

    int graphIncrementer;

    ArrayList<String> XValues;
    ArrayList<String> YValues;
    ArrayList<String> listOfIDs;
    ArrayList<String> temp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_goal);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.tiptrackerlogo3);
        getSupportActionBar().setTitle("");
        ButterKnife.bind(this);

        Typeface iconFont = FontManager.getTypeface(getApplicationContext(), FontManager.BITTER);
        FontManager.markAsIconContainer(findViewById(R.id.budget_goal_activity), iconFont);

        graphIncrementer = 0;

        XValues = new ArrayList<>();
        YValues = new ArrayList<>();
        listOfIDs = new ArrayList<>();
        temp = new ArrayList<String>();

        dbRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        getPeriodId();





        //When "Change Button" is clicked it connects to Firebase and stores values
        budgetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final long currentTime = System.currentTimeMillis();
                final String currentTimestr = Long.toString(currentTime);

                changedGoal = setBudget.getText().toString();
                if(changedGoal.trim().length()==0){
                    setBudget.setError("Please enter in a valid budget");
                }
                else {
                    goal.setText("Current Budget: " + changedGoal);
                    changedGoal = changedGoal.replace(",", "");
                    dbRef.orderByChild("email").equalTo(user.getEmail()).
                            addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    //Updates current budget goal to Firebase
                                    dbRef.child("users").child(user.getUid()).child("current_budget")
                                            .setValue(Double.parseDouble(changedGoal.substring(1)));
                                    //Pushes thee budget goal value and current time the button was pressed
                                    //Adds the two values as a key,value pair to Period tree in Firebase
                                    dbRef.child("periods").child(periodID).child("budgetGoal")
                                            .child(currentTimestr)
                                            .setValue(Double.parseDouble(changedGoal.substring(1)));
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                }

            }
        });

        //Button's that bring up a view for its corresponding graph
        budgetGraphbtn.setOnClickListener(new View.OnClickListener() {//Creates view for budget goal progress graph
            @Override
            public void onClick(View view) {
                graphIncrementer = 1;
                createLineGraph();
            }}
        );
        incomeGraphbtn.setOnClickListener(new View.OnClickListener() {//Creates view for income progress graph
            @Override
            public void onClick(View view) {
                graphIncrementer = 2;
                createIncomeGraph();
            }}
        );
        expenseGraphbtn.setOnClickListener(new View.OnClickListener() {//Creates view for expense progress graph
            @Override
            public void onClick(View view) {
                graphIncrementer = 3;
                createExpenseGraph();}}
        );



    }

    /**
     * Gets coordianates of the graphs
     */
    private void getXYValues(){
        switch (graphIncrementer) {
            case 1:
                dbRef.child("periods").child(periodID).child("budgetGoal")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            ArrayList<String> budgetKeys = new ArrayList<String>();
                            ArrayList<String> budgetValues = new ArrayList<String>();


                            for (DataSnapshot child : dataSnapshot.getChildren()) {
                                budgetKeys.add(child.getKey());
                                budgetValues.add(child.getValue().toString());

                            }
                            YValues = budgetValues;
                            XValues = budgetKeys;
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                break;
            case 2:
                getIncomeID();
                Handler handle = new Handler();
                Runnable run = new Runnable() {
                    @Override
                    public void run() {
                        int i = 0;
                        while(i<listOfIDs.size()) {
                            dbRef.child(DBHelper.INCOMES).child(listOfIDs.get(i)).child("amount")
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        ArrayList<String> incomeID = new ArrayList<String>();
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            YValues.add(dataSnapshot.getValue().toString());
                                        }
                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                            i++;
                         }
                    }

                };
                handle.postDelayed(run,1000);
                break;
            case 3:
                getExpenseID();
                Handler handle2 = new Handler();
                Runnable run2 = new Runnable() {
                    @Override
                    public void run() {
                        int i = 0;
                        while(i<listOfIDs.size()) {
                            dbRef.child(DBHelper.EXPENSES).child(listOfIDs.get(i)).child("amount")
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            YValues.add(dataSnapshot.getValue().toString());
                                        }
                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                            i++;
                        }
                    }

                };
                handle2.postDelayed(run2,1000);
                break;



        }
    }

    /**
     * Methods to find IDs from the database to retrieve for the graphs' respective values
     */
    private void getPeriodId(){//Gets the key for the user's period session
        dbRef.child(DBHelper.USERS).child(user.getUid()).child(DBHelper.PERIODS)
                .addListenerForSingleValueEvent(new ValueEventListener() {
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
                final long time = DateManager.trimMilliseconds(startDate.getTime());

                for (DataSnapshot period : periods) {
                    if ((long) period.getValue() == time) {
                        periodID = period.getKey();
                        break;
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void getIncomeID(){
        dbRef.child(DBHelper.PERIODS).child(periodID).child(DBHelper.INCOMES)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final ArrayList<String> incomeKey = new ArrayList<String>();
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            incomeKey.add(child.getKey());
                        }
                        listOfIDs = incomeKey;

                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }
    private void getExpenseID(){
        dbRef.child(DBHelper.PERIODS).child(periodID).child(DBHelper.EXPENSES)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final ArrayList<String> incomeKey = new ArrayList<String>();
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            incomeKey.add(child.getKey());
                        }
                        listOfIDs = incomeKey;

                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }
    /**
     * Bottom 3 methods create graphs that correspond to the budget goal, income, and expense history.
     * The methods use the MPAndroidChart library to create line graphs.
     * X-Axis represents the time period, while the Y-Axis represents the dollar amount.
    **/
    private void createLineGraph(){//Draws and plots the graph for budget goal progress graph

        Handler handler = new Handler();
        Runnable run = new Runnable() {
            @Override
            public void run() {
                getXYValues();
                Handler handler = new Handler();
                Runnable run = new Runnable() {
                    @Override
                    public void run() {
                        final AlertDialog.Builder mBuilder = new AlertDialog.Builder(BudgetGoalActivity.this);
                        View view2 = getLayoutInflater().inflate(R.layout.linechart, null);
                        view2.setBackgroundColor(Color.parseColor("#bff6bc"));


                        mBuilder.setView(view2).show();


                        LineChart linechart = (LineChart)view2.findViewById(R.id.linegraph);
                        linechart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
                        linechart.getAxisRight().setEnabled(false);

                        Description description = new Description();
                        description.setText("");
                        linechart.setDescription(description);

                        List<Entry> entries = new ArrayList<Entry>();
                        //add for loop to add other entries
                        int lengthX = XValues.size();
                        for(int i = 0; i<lengthX;i++){
                            final float time = Float.parseFloat(XValues.get(i));
                            entries.add(new Entry(i,Float.parseFloat(YValues.get(i))));
                        }

                        LineDataSet dataSet = new LineDataSet(entries, "Budget Goal Progress"); // add entries to dataset
                        dataSet.setColor(Color.BLACK);
                        dataSet.setDrawValues(false);
                        List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
                        dataSet.setCircleColor(Color.BLACK);
                        dataSets.add(dataSet);

                        XAxis xAxis = linechart.getXAxis();
                        xAxis.setDrawGridLines(false);


                        LineData data = new LineData(dataSets);
                        linechart.setBackgroundColor(Color.parseColor("#bff6bc"));
                        linechart.setData(data);

                    }
                };
                handler.postDelayed(run,2000);
            }
        };
        handler.postDelayed(run,1000);




    }
    private void createIncomeGraph(){//Draws and plots the graph for income progress graph
        getXYValues();
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                final AlertDialog.Builder mBuilder = new AlertDialog.Builder(BudgetGoalActivity.this);
                View view2 = getLayoutInflater().inflate(R.layout.linechart, null);
                view2.setBackgroundColor(Color.parseColor("#bff6bc"));


                mBuilder.setView(view2).show();


                LineChart linechart = (LineChart)view2.findViewById(R.id.linegraph);
                linechart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
                linechart.getAxisRight().setEnabled(false);

                Description description = new Description();
                description.setText("");
                linechart.setDescription(description);

                List<Entry> entries = new ArrayList<Entry>();
                //add for loop to add other entries
                for(int i = 0; i<YValues.size();i++){
                    entries.add(new Entry(i,Float.parseFloat(YValues.get(i))));
                }

                LineDataSet dataSet = new LineDataSet(entries, "Income Progress"); // add entries to dataset
                dataSet.setColor(Color.BLUE);
                dataSet.setDrawValues(false);
                List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
                dataSet.setCircleColor(Color.BLUE);
                dataSets.add(dataSet);

                XAxis xAxis = linechart.getXAxis();
                xAxis.setDrawGridLines(false);


                LineData data = new LineData(dataSets);
                linechart.setBackgroundColor(Color.parseColor("#bff6bc"));
                linechart.setData(data);
            }

        };
        handler.postDelayed(runnable,2000);



    }
    private void createExpenseGraph(){//Draws and plots the graph for expense progress graph
        getXYValues();
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                final AlertDialog.Builder mBuilder = new AlertDialog.Builder(BudgetGoalActivity.this);
                View view2 = getLayoutInflater().inflate(R.layout.linechart, null);
                view2.setBackgroundColor(Color.parseColor("#bff6bc"));


                mBuilder.setView(view2).show();


                LineChart linechart = (LineChart)view2.findViewById(R.id.linegraph);
                linechart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
                linechart.getAxisRight().setEnabled(false);

                Description description = new Description();
                description.setText("");
                linechart.setDescription(description);

                List<Entry> entries = new ArrayList<Entry>();
                //add for loop to add other entries
                for(int i = 0; i<YValues.size();i++){
                    entries.add(new Entry(i,Float.parseFloat(YValues.get(i))));
                }

                LineDataSet dataSet = new LineDataSet(entries, "Expense Progress"); // add entries to dataset
                dataSet.setColor(Color.RED);
                dataSet.setDrawValues(false);
                List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
                dataSet.setCircleColor(Color.RED);
                dataSets.add(dataSet);

                XAxis xAxis = linechart.getXAxis();
                xAxis.setDrawGridLines(false);


                LineData data = new LineData(dataSets);
                linechart.setBackgroundColor(Color.parseColor("#bff6bc"));
                linechart.setData(data);
            }

        };
        handler.postDelayed(runnable,2000);


    }
}
