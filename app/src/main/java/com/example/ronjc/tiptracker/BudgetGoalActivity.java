//https://github.com/PhilJay/MPAndroidChart/wiki/Getting-Started reference for line graph
package com.example.ronjc.tiptracker;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.icu.util.Calendar;
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

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.ronjc.tiptracker.utils.FontManager.BITTER;


public class BudgetGoalActivity extends AppCompatActivity {

    @BindView(R.id.set_budget_et) CurrencyEditText setBudget;
    @BindView(R.id.set_goal_tv) TextView goal;
    String changedGoal;
    @BindView(R.id.change_budget_btn) Button budgetBtn;
    @BindView(R.id.budget_goal_graph) Button budgetGraphbtn;
    @BindView(R.id.income_graph) Button lineGraphbtn;
    @BindView(R.id.expense_graph) Button expenseGraphbtn;
    DatabaseReference dbRef;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;
    LineChart lineChart;
    String periodId;

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



        String customFont = "fonts/bitter.ttf";
        Typeface typeface = Typeface.createFromAsset(getAssets(), customFont);
        budgetBtn.setTypeface(iconFont);
        budgetGraphbtn.setTypeface(iconFont);
        lineGraphbtn.setTypeface(iconFont);
        expenseGraphbtn.setTypeface(iconFont);
        FontManager.markAsIconContainer(budgetBtn,iconFont);
        FontManager.markAsIconContainer(budgetGraphbtn,iconFont);
        FontManager.markAsIconContainer(lineGraphbtn,iconFont);
        FontManager.markAsIconContainer(expenseGraphbtn,iconFont);


        dbRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();


        budgetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                changedGoal = setBudget.getText().toString();
                goal.setText("Current Budget: " + changedGoal);
                changedGoal = changedGoal.replace(",","");
                dbRef.orderByChild("email").equalTo(user.getEmail()).
                        addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                //Updates current budget goal to Firebase
                                dbRef.child("users").child(user.getUid()).child("current_budget").
                                        setValue(Double.parseDouble(changedGoal.substring(1)));


                                dbRef.child("periods").child(getPeriodId()).child("budgetGoal").push()
                                        .setValue(Double.parseDouble(changedGoal.substring(1)));

                                dbRef.child("periods").child(getPeriodId()).child("endDate").push()
                                        .setValue(System.currentTimeMillis());


                                GetXYValues();
                                //period key ends in OzG
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

            }
        });

        Button b = (Button)findViewById(R.id.budget_goal_graph);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder mBuilder = new AlertDialog.Builder(BudgetGoalActivity.this);
                View view2 = getLayoutInflater().inflate(R.layout.linechart, null);

                LineChart lineChart2 = (LineChart)view2.findViewById(R.id.linegraph);


                mBuilder.setView(view2).show();
                final AlertDialog dialog = mBuilder.create();

                List<Entry> entries = new ArrayList<Entry>();
                //add for loop to add other entries

                entries.add(new Entry(3,4));
                entries.add(new Entry(5,6));
                entries.add(new Entry(6,8));
                LineDataSet dataSet = new LineDataSet(entries, "Budget Goal"); // add entries to dataset
                dataSet.setColor(Color.parseColor("#2ecc44"));
                List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
                dataSet.setCircleColor(Color.parseColor("#2ecc44"));
                dataSets.add(dataSet);

                LineData data = new LineData(dataSets);
                lineChart2.setBackgroundColor(Color.WHITE);
                lineChart2.setData(data);
            }
        });



    }
    private void GetXYValues(){
        dbRef.child("periods").child(getPeriodId()).child("budgetGoal")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        ArrayList<String> list1 = new ArrayList<String>();
                        ArrayList<String> list3 = new ArrayList<String>();

                        for (DataSnapshot child: dataSnapshot.getChildren()) {
                            Period period = child.getValue(Period.class);

                            String get_amount = ""+period.getBudgetGoal();

                            //long get_date = users.getStartDate();

                            //DateFormat df = new SimpleDateFormat("MM/dd/yy");
                            //String gd = df.format(get_date) + "\n";

                            list1.add(get_amount);
                            //list3.add(gd);

                        }
                        String amounts = TextUtils.join("", list1);
                        Toast.makeText(BudgetGoalActivity.this,"HERE",Toast.LENGTH_LONG).show();;
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }


    private String getPeriodId(){
        dbRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
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
                final long time = DateManager.trimMilliseconds(startDate.getTime());

                for (DataSnapshot period : periods) {
                    if ((long) period.getValue() == time) {
                        periodId = period.getKey();
                        break;
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return periodId;
    }

}
