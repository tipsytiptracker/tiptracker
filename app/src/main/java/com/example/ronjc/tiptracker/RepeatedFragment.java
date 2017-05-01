package com.example.ronjc.tiptracker;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ronjc.tiptracker.utils.BudgetGoalTabAdapter;
import com.example.ronjc.tiptracker.utils.ExpandableListAdapter;
import com.example.ronjc.tiptracker.utils.DBHelper;
import com.example.ronjc.tiptracker.utils.FontManager;
import com.example.ronjc.tiptracker.utils.RepeatedListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static com.example.ronjc.tiptracker.utils.FontManager.BITTER;


/**
 * @author Ronald Mangiliman, Gene Hernandez
 */
public class RepeatedFragment extends Fragment {

    private final static String PERIOD_KEY = "period";

    private Typeface bitter;
    private String[] typeArray, frequencyArray;
    private String periodID,selectedType;
    private ExpandableListView mExpandableListView;
    private RepeatedListAdapter mRepeatedListAdapter;
    private List<String> headerList;
    private HashMap<String, List<String>> idList;
    private HashMap<String, List<String>> childList;

    private DatabaseReference dbRef;
    private FirebaseUser user;
    private FirebaseAuth mAuth;

    private DecimalFormat decimalFormat;

    private String key="";

    private OnFragmentInteractionListener mListener;

    public RepeatedFragment() {
        // Required empty public constructor
    }

    public static RepeatedFragment newInstance(String periodID) {
        RepeatedFragment fragment = new RepeatedFragment();
        Bundle args = new Bundle();
        args.putString(PERIOD_KEY, periodID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.periodID = getArguments().getString(PERIOD_KEY);
        }
        typeArray = new String[]{"Income", "Expense"};
        frequencyArray = new String[] {"Weekly", "Monthly", "Annually"};
        headerList = new ArrayList<String>();
        headerList.add("Repeated Incomes");
        headerList.add("Repeated Expenses");
        dbRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        decimalFormat = new DecimalFormat("0.00");
        retrieveRepeated();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mView = inflater.inflate(R.layout.fragment_repeated, container, false);
        bitter = FontManager.getTypeface(mView.getContext(), BITTER);

        TextView addRepeatedHeader = (TextView) mView.findViewById(R.id.add_repeated_tv);
//        Button addRepeatedButton = (Button) mView.findViewById(R.id.add_repeated_button);
        mExpandableListView = (ExpandableListView) mView.findViewById(R.id.repeated_list);
        mRepeatedListAdapter = new RepeatedListAdapter(getContext(), headerList, childList, idList, typeArray, frequencyArray);
        mExpandableListView.setAdapter(mRepeatedListAdapter);

        addRepeatedHeader.setTypeface(bitter);

//        addRepeatedButton.setTypeface(bitter);
//        addRepeatedButton.setOnClickListener(new RepeatedButtonListener());

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



    private void retrieveRepeated() {

        //HashMap of Categories with LIst String representing incomes
        childList = new HashMap<String, List<String>>();

        //HashMap of Categories and List of Income IDs belinging to that income
        idList = new HashMap<String, List<String>>();

        childList.put(headerList.get(0), new ArrayList<String>());
        childList.put(headerList.get(1), new ArrayList<String>());
        idList.put(headerList.get(0), new ArrayList<String>());
        idList.put(headerList.get(1), new ArrayList<String>());
        dbRef.child(DBHelper.USERS).child(user.getUid()).child(DBHelper.REPEATED_INCOME).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    Iterable<DataSnapshot> incomes = dataSnapshot.getChildren();
                    for (DataSnapshot income : incomes) {
                        String incomeString = income.child("name").getValue().toString() + ": $" + decimalFormat.format(income.child("amount").getValue());
                        childList.get(headerList.get(0)).add(incomeString);
                        idList.get(headerList.get(0)).add(income.getKey());
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        dbRef.child(DBHelper.USERS).child(user.getUid()).child(DBHelper.REPEATED_EXPENSE).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    Iterable<DataSnapshot> expenses = dataSnapshot.getChildren();
                    for (DataSnapshot expense : expenses) {
                        String expenseString = expense.child("name").getValue().toString() + ": $" + decimalFormat.format(expense.child("amount").getValue());
                        childList.get(headerList.get(1)).add(expenseString);
                        idList.get(headerList.get(1)).add(expense.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
