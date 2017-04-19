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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ronjc.tiptracker.model.Expense;
import com.example.ronjc.tiptracker.model.Income;
import com.example.ronjc.tiptracker.utils.DBHelper;
import com.example.ronjc.tiptracker.utils.DateManager;
import com.example.ronjc.tiptracker.utils.ExpandableListAdapter;
import com.example.ronjc.tiptracker.utils.FontManager;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import butterknife.BindView;

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
    private String type = "";

    //User ID being passed from ExpandableListAdapter
    private String userID = "";
    private int page;
    private ExpandableListAdapter listAdapter;
    private ExpandableListView expandableListView;

    //List of either Incomes or Expenses to display
    private ArrayList<?> list;

    //List of category "headers"
    private ArrayList<String> headerList;

    //HashMap of children
    private HashMap<String, List<String>> childList;
    private OnFragmentInteractionListener mListener;

    public BudgetFragment() {
        // Required empty public constructor
    }

    public static BudgetFragment newInstance(int page, ArrayList<? extends Serializable> list, Date startDate, String userID) {
        BudgetFragment mBudgetFragment = new BudgetFragment();
        Bundle args = new Bundle();
        args.putInt(PAGE_KEY, page);
        args.putSerializable(LIST_KEY, list);
        args.putString(USER_ID_KEY, userID);
        mBudgetFragment.setArguments(args);
        return mBudgetFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            page = getArguments().getInt(PAGE_KEY, 0);
            list = (ArrayList<?>) getArguments().getSerializable(LIST_KEY);
            userID = getArguments().getString(USER_ID_KEY);
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_budget, container, false);
        expandableListView = (ExpandableListView) view.findViewById(R.id.budget_list);
        prepareListData();
        listAdapter = new ExpandableListAdapter(view.getContext(), headerList, childList, userID, type);
        expandableListView.setAdapter(listAdapter);
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
        headerList = new ArrayList<String>();
        childList = new HashMap<String, List<String>>();
        ArrayList<ArrayList<String>> listOfLists = new ArrayList<ArrayList<String>>();
        DecimalFormat decimalFormat = new DecimalFormat("0.00");

        if (list.size() != 0) {
            if(list.get(0) instanceof Income) {
                type = "income";
                incomes = (ArrayList<Income>) list;
                for(Income income : incomes) {
                    String stringToAdd;
                    if(!headerList.contains(income.getCategory())) {
                        headerList.add(income.getCategory());
                        ArrayList<String> incomeList = new ArrayList<String>();
                        stringToAdd = income.getName() + ": $" + decimalFormat.format(income.getAmount());
                        incomeList.add(stringToAdd);
                        listOfLists.add(incomeList);
                    } else {
                        int index = headerList.indexOf(income.getCategory());
                        stringToAdd = income.getName() + ": $" + decimalFormat.format(income.getAmount());
                        listOfLists.get(index).add(stringToAdd);
                    }
                }
            } else {
                type = "expense";
                expenses = (ArrayList<Expense>) list;
                for(Expense expense : expenses) {
                    String stringToAdd;
                    if(!headerList.contains(expense.getCategory())) {
                        headerList.add(expense.getCategory());
                        ArrayList<String> expenseList = new ArrayList<String>();
                        stringToAdd = expense.getName() + ": $" + decimalFormat.format(expense.getAmount());
                        expenseList.add(stringToAdd);
                        listOfLists.add(expenseList);
                    } else {
                        int index = headerList.indexOf(expense.getCategory());
                        stringToAdd = expense.getName() + ": $" + decimalFormat.format(expense.getAmount());
                        listOfLists.get(index).add(stringToAdd);
                    }
                }
            }
            for(int i = 0; i < headerList.size(); i++) {
                childList.put(headerList.get(i), listOfLists.get(i));
            }
        }

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

    //TODO: Move to BudgetFragment
    private void writeNewCategory(final String category) {
        final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        //Get last Sunday in milliseconds
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date startDate = calendar.getTime();
        final long time = DateManager.trimMilliseconds(startDate.getTime());
        mDatabase.child(DBHelper.USERS).child(userID).child(DBHelper.PERIODS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Get key for period to write
                Iterable<DataSnapshot> periods = dataSnapshot.getChildren();
                String periodToWrite = "";
                for(DataSnapshot period : periods) {
                    if ((long)period.getValue() == time) {
                        periodToWrite = period.getKey();
                        break;
                    }
                }
                String categoryKey = mDatabase.child(DBHelper.PERIODS).child(periodToWrite).child(DBHelper.CATEGORIES).push().getKey();
                mDatabase.child(DBHelper.PERIODS).child(periodToWrite).child(DBHelper.CATEGORIES).child(categoryKey).setValue(category);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
