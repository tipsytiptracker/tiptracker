package com.example.ronjc.tiptracker;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ronjc.tiptracker.model.Expense;
import com.example.ronjc.tiptracker.model.Income;
import com.example.ronjc.tiptracker.utils.DBHelper;
import com.example.ronjc.tiptracker.utils.DateManager;
import com.example.ronjc.tiptracker.utils.ExpandableListAdapter;
import com.example.ronjc.tiptracker.utils.FontManager;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


import static com.example.ronjc.tiptracker.utils.FontManager.BITTER;


/**
 * Budget Fragment that lists out Income/Expenses.
 *
 * @author Ronald Mangiliman
 */
public class BudgetFragment extends Fragment {

    //Keys for storing values in Bundle
    private static final String PAGE_KEY = "page";
    private static final String LIST_KEY = "list";
    private static final String USER_ID_KEY = "user";
    private static final String PERIOD_KEY = "period";
    private String type = "";

    //User ID being passed from ExpandableListAdapter
    private String userID = "";
    private int page;
    private String currentPeriodID = "";
    private ExpandableListAdapter listAdapter;
    private ExpandableListView expandableListView;

    //List of either Incomes or Expenses to display
    private ArrayList<?> list;

    //List of category "headers"
    private ArrayList<String> headerList;

    //HashMap of children
    private HashMap<String, List<String>> childList;
    private OnFragmentInteractionListener mListener;

    private DatabaseReference mDatabaseReference;

    public BudgetFragment() {
        // Required empty public constructor
    }

    public static BudgetFragment newInstance(int page, ArrayList<? extends Serializable> list, Date startDate, String userID, String currentPeriodID) {
        BudgetFragment mBudgetFragment = new BudgetFragment();
        Bundle args = new Bundle();
        args.putInt(PAGE_KEY, page);
        args.putSerializable(LIST_KEY, list);
        args.putString(USER_ID_KEY, userID);
        args.putString(PERIOD_KEY, currentPeriodID);
        mBudgetFragment.setArguments(args);
        return mBudgetFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        headerList = new ArrayList<String>();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        if (getArguments() != null) {
            page = getArguments().getInt(PAGE_KEY, 0);
            list = (ArrayList<?>) getArguments().getSerializable(LIST_KEY);
            userID = getArguments().getString(USER_ID_KEY);
            currentPeriodID = getArguments().getString(PERIOD_KEY);
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_budget, container, false);
        type = page == 1 ? DBHelper.INCOMES : DBHelper.EXPENSES;
        expandableListView = (ExpandableListView) view.findViewById(R.id.budget_list);
        mDatabaseReference.child(DBHelper.PERIODS).child(currentPeriodID).child(DBHelper.CATEGORIES).child(type).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> categories = dataSnapshot.getChildren();
                for (DataSnapshot category : categories) {
                    if(!headerList.contains(category.getValue().toString())) {
                        headerList.add(category.getValue().toString());
                    }
                }
                prepareListData();
                listAdapter = new ExpandableListAdapter(view.getContext(), headerList, childList, userID, type, currentPeriodID);
                expandableListView.setAdapter(listAdapter);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        Button addCategoryButton = (Button)view.findViewById(R.id.add_category_button);
        final Typeface bitter = FontManager.getTypeface(view.getContext(), BITTER);
        addCategoryButton.setTypeface(bitter);
        addCategoryButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //Clean up!!
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(getContext());
                final View mView = inflater.inflate(R.layout.add_category_dialog, null);
                ((TextView) mView.findViewById(R.id.add_category_header)).setTypeface(bitter);
                final TextInputLayout categoryName = (TextInputLayout) mView.findViewById(R.id.add_category_text_input);
                final EditText mEditText = (EditText)mView.findViewById(R.id.add_category_edit_text);
                mEditText.setTypeface(bitter);
                categoryName.setTypeface(bitter);
                mBuilder.setView(mView);
                final AlertDialog dialog = mBuilder.create();
                Button mButton = (Button)mView.findViewById(R.id.add_category_dialog_button);
                mButton.setTypeface(bitter);
                mButton.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        String newCategory = mEditText.getText().toString();
                        writeNewCategory(newCategory);
                        dialog.dismiss();
                        Snackbar.make(mView, getString(R.string.category_added), Snackbar.LENGTH_SHORT).show();
                    }
                });
                dialog.show();
            }
        });
        return view;

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

    //TODO: Unfortunately, this will most likely have to be rewritten.
    private void prepareListData() {
        ArrayList<Income> incomes;
        ArrayList<Expense> expenses;
        childList = new HashMap<String, List<String>>();
        ArrayList<ArrayList<String>> listOfLists = new ArrayList<ArrayList<String>>();
        DecimalFormat decimalFormat = new DecimalFormat("0.00");

        //Check if incomes or expenses. 1 is incomes, 2 is expenses
        if(type.equals(DBHelper.INCOMES)) {
            if(list.size() == 0) {
                for (int i = 0; i < headerList.size(); i++) {
                    childList.put(headerList.get(i), new ArrayList<String>());
                }
            } else {
                for(int i = 0; i < headerList.size(); i++) {
                    listOfLists.add(new ArrayList<String>());
                }
                String stringToAdd;
                incomes = (ArrayList<Income>)list;
                //For some reason, arraylist contains null elements on creation. remove them using this line
                incomes.removeAll(Collections.singleton(null));
                for(Income income : incomes) {
                    int index = headerList.indexOf(income.getCategory());
                    stringToAdd = income.getName() + ": $" + decimalFormat.format(income.getAmount());
                    listOfLists.get(index).add(stringToAdd);
                }
                for(int i = 0; i < headerList.size(); i++) {
                    childList.put(headerList.get(i), listOfLists.get(i));
                }
            }
        } else {
            if(list.size() == 0) {
                for (int i = 0; i < headerList.size(); i++) {
                    childList.put(headerList.get(i), new ArrayList<String>());
                }
            } else {
                for(int i = 0; i < headerList.size(); i++) {
                    listOfLists.add(new ArrayList<String>());
                }
                String stringToAdd;
                expenses = (ArrayList<Expense>)list;
                expenses.removeAll(Collections.singleton(null));
                for(Expense expense : expenses) {
                    int index = headerList.indexOf(expense.getCategory());
                    stringToAdd = expense.getName() + ": $" + decimalFormat.format(expense.getAmount());
                    listOfLists.get(index).add(stringToAdd);
                }
                for(int i = 0; i < headerList.size(); i++) {
                    childList.put(headerList.get(i), listOfLists.get(i));
                }
            }
        }
//        ArrayList<String> emptyLIst = new ArrayList<>();
//        emptyLIst.add("Sring");
//        if(page == 1) {
//            type = DBHelper.INCOMES;
//            if (list.size() != 0) {
//                incomes = (ArrayList<Income>) list;
//                for(Income income : incomes) {
//                    String stringToAdd;
//                    if(!headerList.contains(income.getCategory())) {
//                        headerList.add(income.getCategory());
//                        ArrayList<String> incomeList = new ArrayList<String>();
//                        stringToAdd = income.getName() + ": $" + decimalFormat.format(income.getAmount());
//                        incomeList.add(stringToAdd);
//                        listOfLists.add(incomeList);
//                    } else {
//                        int index = headerList.indexOf(income.getCategory());
//                        stringToAdd = income.getName() + ": $" + decimalFormat.format(income.getAmount());
//                        listOfLists.get(index).add(stringToAdd);
//                    }
//                    for(int i = 0; i < headerList.size(); i++) {
//                        childList.put(headerList.get(i), em);
//                    }
//                }
//            } else {
//                for(int i = 0; i < headerList.size(); i++) {
//                    childList.put(headerList.get(i), );
//                }
//            }
//        } else {
//            type = DBHelper.EXPENSES;
//            if(list.size() != 0) {
//                expenses = (ArrayList<Expense>) list;
//                for(Expense expense : expenses) {
//                    String stringToAdd;
//                    if(!headerList.contains(expense.getCategory())) {
//                        headerList.add(expense.getCategory());
//                        ArrayList<String> expenseList = new ArrayList<String>();
//                        stringToAdd = expense.getName() + ": $" + decimalFormat.format(expense.getAmount());
//                        expenseList.add(stringToAdd);
//                        listOfLists.add(expenseList);
//                    } else {
//                        int index = headerList.indexOf(expense.getCategory());
//                        stringToAdd = expense.getName() + ": $" + decimalFormat.format(expense.getAmount());
//                        listOfLists.get(index).add(stringToAdd);
//                    }
//                    for(int i = 0; i < headerList.size(); i++) {
//                        childList.put(headerList.get(i), childList.get(i));
//                    }
//                }
//            } else {
//                for(int i = 0; i < headerList.size(); i++) {
//                    childList.put(headerList.get(i), new ArrayList<String>());
//                }
//            }
//        }
//        headerList.add("Rent");
//        headerList.add("Grocery");
//        headerList.add("Misc.");
//
//        List<String> rent = new ArrayList<String>();
//        rent.add("Weekly: $300.00");
//
//        List<String> groceries = new ArrayList<String>();
//        groceries.add("Avocados: $5.99");
//        groceries.add("Tofu: $1.29");
//        groceries.add("Broccoli: $1.29");
//        groceries.add("Coffee: $8.99");
//
//
//        List<String> misc = new ArrayList<String>();
//        misc.add("420: $5.00");
//
//        childList.put(headerList.get(0), rent); // Header, Child data
//        childList.put(headerList.get(1), groceries);
//        childList.put(headerList.get(2), misc);
    }

    private void writeNewCategory(final String category) {
        final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        String categoryKey = mDatabase.child(DBHelper.PERIODS).child(currentPeriodID).child(DBHelper.CATEGORIES).child(type).push().getKey();
        mDatabase.child(DBHelper.PERIODS).child(currentPeriodID).child(DBHelper.CATEGORIES).child(type).child(categoryKey).setValue(category);
        Snackbar.make(getActivity().findViewById(R.id.activity_budget_management), getString(R.string.category_added), Snackbar.LENGTH_SHORT).show();
        headerList.add(category);
        childList.put(category, new ArrayList<String>());
        listAdapter.notifyDataSetChanged();
    }
}
