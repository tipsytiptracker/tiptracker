package com.example.ronjc.tiptracker;

import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.blackcat.currencyedittext.CurrencyEditText;
import com.example.ronjc.tiptracker.utils.FontManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

import static com.example.ronjc.tiptracker.utils.FontManager.BITTER;


/**
 * @author Gene Hernandez, Ronald Mangiliman
 */
public class SetBudgetGoalFragment extends Fragment {

    private static final String PERIOD_ID = "periodID";

    private EditText setBudget;
    private TextView goal;
    private Button budgetBtn;
    private DatabaseReference dbRef;
    private FirebaseUser user;
    private FirebaseAuth mAuth;
    private String changedGoal;
    Spinner goal_period;
    String current_period;


    private String periodID;
    private OnFragmentInteractionListener mListener;

    public SetBudgetGoalFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static SetBudgetGoalFragment newInstance(String periodID) {
        SetBudgetGoalFragment fragment = new SetBudgetGoalFragment();
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
        dbRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mView = inflater.inflate(R.layout.fragment_set_budget_goal, container, false);
        final Typeface bitter = FontManager.getTypeface(mView.getContext(), BITTER);

        setBudget = (EditText) mView.findViewById(R.id.set_budget_et);
        goal = (TextView) mView.findViewById(R.id.set_goal_tv);
        budgetBtn = (Button) mView.findViewById(R.id.change_budget_btn);

        setBudget.setTypeface(bitter);
        goal.setTypeface(bitter);
        budgetBtn.setTypeface(bitter);

        //When "Change Button" is clicked it connects to Firebase and stores values
        budgetBtn.setOnClickListener(new ChangeBudgetListener());

        goal_period = (Spinner)mView.findViewById(R.id.budget_goal_period);

        List<String> list = new ArrayList<String>();
        list.add("Weekly");
        list.add("Monthly");
        list.add("Annually");


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mView.getContext(),
                R.layout.custom_spinner_item, list){
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View mView = super.getView(position, convertView, parent);
                ((TextView)mView).setTypeface(bitter);
                return mView;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View mView  = super.getDropDownView(position, convertView, parent);
                ((TextView)mView).setTypeface(bitter);
                return mView;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        goal_period.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                current_period = adapterView.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        goal_period.setAdapter(adapter);

        //current_period = goal_period.getSelectedItem().toString();

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

    private class ChangeBudgetListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            final long currentTime = System.currentTimeMillis();
            final String currentTimestr = Long.toString(currentTime);

            switch (current_period) {
                case  "Weekly":
                changedGoal = setBudget.getText().toString();
                if (changedGoal.trim().length() == 0) {
                    setBudget.setError("Please enter in a valid budget");
                } else {
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
                                    if(periodID != null) {
                                        dbRef.child("periods").child(periodID).child("budgetGoal")
                                                .child(currentTimestr)
                                                .setValue(Double.parseDouble(changedGoal.substring(1)));
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                }
                break;

                case "Monthly":
                    changedGoal = setBudget.getText().toString();
                    double g = Double.parseDouble(changedGoal.substring(1).replace(",", ""));
                    BigDecimal d = new BigDecimal(g);
                    d = d.divide(BigDecimal.valueOf(4), BigDecimal.ROUND_HALF_UP);
                    final double i = d.doubleValue();

                    if (changedGoal.trim().length() == 0) {
                        setBudget.setError("Please enter in a valid budget");
                    } else {
                        goal.setText("Current Budget: " + i);
                        changedGoal = changedGoal.replace(",", "");
                        dbRef.orderByChild("email").equalTo(user.getEmail()).
                                addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        //Updates current budget goal to Firebase
                                        dbRef.child("users").child(user.getUid()).child("current_budget")
                                                .setValue(i);
                                        //Pushes thee budget goal value and current time the button was pressed
                                        //Adds the two values as a key,value pair to Period tree in Firebase
                                        if(periodID != null) {
                                            dbRef.child("periods").child(periodID).child("budgetGoal")
                                                    .child(currentTimestr)
                                                    .setValue(i);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                    }
                    break;

                case "Annually":
                    changedGoal = setBudget.getText().toString();
                    double dg = Double.parseDouble(changedGoal.substring(1).replace(",", ""));
                    BigDecimal bd = new BigDecimal(dg);
                    bd = bd.divide(BigDecimal.valueOf(52), BigDecimal.ROUND_HALF_UP);
                    final double cg = bd.doubleValue();

                    if (changedGoal.trim().length() == 0) {
                        setBudget.setError("Please enter in a valid budget");
                    } else {
                        goal.setText("Current Budget: " + cg);
                        changedGoal = changedGoal.replace(",", "");
                        dbRef.orderByChild("email").equalTo(user.getEmail()).
                                addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        //Updates current budget goal to Firebase
                                        dbRef.child("users").child(user.getUid()).child("current_budget")
                                                .setValue(cg);
                                        //Pushes thee budget goal value and current time the button was pressed
                                        //Adds the two values as a key,value pair to Period tree in Firebase
                                        if(periodID != null) {
                                            dbRef.child("periods").child(periodID).child("budgetGoal")
                                                    .child(currentTimestr)
                                                    .setValue(cg);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                    }
                    break;

            }

        }
    }
}
