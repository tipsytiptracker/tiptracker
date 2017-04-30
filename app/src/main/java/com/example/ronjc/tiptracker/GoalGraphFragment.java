package com.example.ronjc.tiptracker;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.ronjc.tiptracker.utils.DBHelper;
import com.example.ronjc.tiptracker.utils.FontManager;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

import static com.example.ronjc.tiptracker.utils.FontManager.BITTER;
import static com.example.ronjc.tiptracker.utils.FontManager.FONTAWESOME;


/**
 * @author Gene Hernandez, Ronald Mangiliman
 */
public class GoalGraphFragment extends Fragment {

    private static final String PERIOD_ID = "period";

    private Button budgetGraphBtn;
    private Button incomeGraphBtn;
    private Button expenseGraphBtn;
    private TextView graphHeader;

    private int graphIncrementer;


    private ArrayList<String> xValues;
    private ArrayList<String> yValues;
    private ArrayList<String> listOfIDs;

    private String periodID;

    private OnFragmentInteractionListener mListener;

    private DatabaseReference dbRef;
    private FirebaseUser user;
    private FirebaseAuth mAuth;

    public GoalGraphFragment() {
        // Required empty public constructor
    }

    public static GoalGraphFragment newInstance(String periodID) {
        GoalGraphFragment fragment = new GoalGraphFragment();
        Bundle args = new Bundle();
        args.putString(PERIOD_ID, periodID);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            periodID = getArguments().getString(PERIOD_ID);
        }
        xValues = new ArrayList<>();
        yValues = new ArrayList<>();
        listOfIDs = new ArrayList<>();
        dbRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mView = inflater.inflate(R.layout.fragment_goal_graph, container, false);
        Typeface bitter = FontManager.getTypeface(mView.getContext(), BITTER);

        graphHeader = (TextView) mView.findViewById(R.id.viewGraph_tv);
        budgetGraphBtn = (Button) mView.findViewById(R.id.budget_goal_graph);
        incomeGraphBtn = (Button) mView.findViewById(R.id.income_graph);
        expenseGraphBtn = (Button) mView.findViewById(R.id.expense_graph);

        graphHeader.setTypeface(bitter);
        budgetGraphBtn.setTypeface(bitter);
        incomeGraphBtn.setTypeface(bitter);
        expenseGraphBtn.setTypeface(bitter);

        //Button's that bring up a view for its corresponding graph
        budgetGraphBtn.setOnClickListener(new View.OnClickListener() {//Creates view for budget goal progress graph
                                              @Override
                                              public void onClick(View view) {
                                                  graphIncrementer = 1;
                                                  createLineGraph(inflater);
                                              }
                                          }
        );
        incomeGraphBtn.setOnClickListener(new View.OnClickListener() {//Creates view for income progress graph
                                              @Override
                                              public void onClick(View view) {
                                                  graphIncrementer = 2;
                                                  createIncomeGraph(inflater);
                                              }
                                          }
        );
        expenseGraphBtn.setOnClickListener(new View.OnClickListener() {//Creates view for expense progress graph
                                               @Override
                                               public void onClick(View view) {
                                                   graphIncrementer = 3;
                                                   createExpenseGraph(inflater);
                                               }
                                           }
        );

        //set On click listener for repeated button
//        repeatedButton.setOnClickListener(new BudgetGoalActivity.RepeatedButtonListener());


        return mView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    /**
     * Bottom 3 methods create graphs that correspond to the budget goal, income, and expense history.
     * The methods use the MPAndroidChart library to create line graphs.
     * X-Axis represents the time period, while the Y-Axis represents the dollar amount.
     **/
    private void createLineGraph(final LayoutInflater inflater) {//Draws and plots the graph for budget goal progress graph

        Handler handler = new Handler();
        Runnable run = new Runnable() {
            @Override
            public void run() {
                getXYValues();
                Handler handler = new Handler();
                Runnable run = new Runnable() {
                    @Override
                    public void run() {
                        final AlertDialog.Builder mBuilder = new AlertDialog.Builder(getContext());
                        View view2 = inflater.inflate(R.layout.linechart, null);
                        view2.setBackgroundColor(Color.parseColor("#bff6bc"));


                        mBuilder.setView(view2).show();


                        LineChart linechart = (LineChart) view2.findViewById(R.id.linegraph);
                        linechart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
                        linechart.getAxisRight().setEnabled(false);

                        Description description = new Description();
                        description.setText("");
                        linechart.setDescription(description);

                        List<Entry> entries = new ArrayList<Entry>();
                        //add for loop to add other entries
                        int lengthX = xValues.size();
                        for (int i = 0; i < lengthX; i++) {
                            final float time = Float.parseFloat(xValues.get(i));
                            entries.add(new Entry(i, Float.parseFloat(yValues.get(i))));
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
                handler.postDelayed(run, 2000);
            }
        };
        handler.postDelayed(run, 1000);


    }

    private void createIncomeGraph(final LayoutInflater inflater) {//Draws and plots the graph for income progress graph
        getXYValues();
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                final AlertDialog.Builder mBuilder = new AlertDialog.Builder(getContext());
                View view2 = inflater.inflate(R.layout.linechart, null);
                view2.setBackgroundColor(Color.parseColor("#bff6bc"));


                mBuilder.setView(view2).show();


                LineChart linechart = (LineChart) view2.findViewById(R.id.linegraph);
                linechart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
                linechart.getAxisRight().setEnabled(false);

                Description description = new Description();
                description.setText("");
                linechart.setDescription(description);

                List<Entry> entries = new ArrayList<Entry>();
                //add for loop to add other entries
                for (int i = 0; i < yValues.size(); i++) {
                    entries.add(new Entry(i, Float.parseFloat(yValues.get(i))));
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
        handler.postDelayed(runnable, 2000);


    }

    private void createExpenseGraph(final LayoutInflater inflater) {//Draws and plots the graph for expense progress graph
        getXYValues();
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                final AlertDialog.Builder mBuilder = new AlertDialog.Builder(getContext());
                View view2 = inflater.inflate(R.layout.linechart, null);
                view2.setBackgroundColor(Color.parseColor("#bff6bc"));

                mBuilder.setView(view2).show();


                LineChart linechart = (LineChart) view2.findViewById(R.id.linegraph);
                linechart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
                linechart.getAxisRight().setEnabled(false);

                Description description = new Description();
                description.setText("");
                linechart.setDescription(description);

                List<Entry> entries = new ArrayList<Entry>();
                //add for loop to add other entries
                for (int i = 0; i < yValues.size(); i++) {
                    entries.add(new Entry(i, Float.parseFloat(yValues.get(i))));
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
        handler.postDelayed(runnable, 2000);
    }

    /**
     * Gets coordianates of the graphs
     */
    private void getXYValues() {
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
                                yValues = budgetValues;
                                xValues = budgetKeys;
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
                        while (i < listOfIDs.size()) {
                            dbRef.child(DBHelper.INCOMES).child(listOfIDs.get(i)).child("amount")
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        ArrayList<String> incomeID = new ArrayList<String>();

                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            yValues.add(dataSnapshot.getValue().toString());
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                            i++;
                        }
                    }

                };
                handle.postDelayed(run, 1000);
                break;
            case 3:
                getExpenseID();
                Handler handle2 = new Handler();
                Runnable run2 = new Runnable() {
                    @Override
                    public void run() {
                        int i = 0;
                        while (i < listOfIDs.size()) {
                            dbRef.child(DBHelper.EXPENSES).child(listOfIDs.get(i)).child("amount")
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            yValues.add(dataSnapshot.getValue().toString());
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                            i++;
                        }
                    }

                };
                handle2.postDelayed(run2, 1000);
                break;
        }
    }

    private void getIncomeID() {
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

    private void getExpenseID() {
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
}
