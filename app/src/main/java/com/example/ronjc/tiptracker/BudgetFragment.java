package com.example.ronjc.tiptracker;

import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.example.ronjc.tiptracker.model.Expense;
import com.example.ronjc.tiptracker.model.Income;
import com.example.ronjc.tiptracker.utils.ExpandableListAdapter;
import com.example.ronjc.tiptracker.utils.FontManager;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import butterknife.BindView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BudgetFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link BudgetFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BudgetFragment extends Fragment {

    private static final String PAGE_KEY = "page";
    private static final String LIST_KEY = "list";
    private int page;
    private ExpandableListAdapter listAdapter;
    private ExpandableListView expandableListView;
    private ArrayList<?> list;
    private ArrayList<String> headerList;
    private HashMap<String, List<String>> childList;
    private OnFragmentInteractionListener mListener;

    public BudgetFragment() {
        // Required empty public constructor
    }

    public static BudgetFragment newInstance(int page, ArrayList<? extends Serializable> list, Date startDate) {
        BudgetFragment mBudgetFragment = new BudgetFragment();
        Bundle args = new Bundle();
        args.putInt(PAGE_KEY, page);
        args.putSerializable(LIST_KEY, list);
        mBudgetFragment.setArguments(args);
        return mBudgetFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            page = getArguments().getInt(PAGE_KEY, 0);
            list = (ArrayList<?>) getArguments().getSerializable(LIST_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_budget, container, false);
        expandableListView = (ExpandableListView) view.findViewById(R.id.budget_list);
        prepareListData();
        listAdapter = new ExpandableListAdapter(view.getContext(), headerList, childList);
        expandableListView.setAdapter(listAdapter);
        return view;

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    //TODO: Replace with real data
    private void prepareListData() {
        ArrayList<Income> incomes;
        ArrayList<Expense> expenses;
        headerList = new ArrayList<String>();
        childList = new HashMap<String, List<String>>();
        ArrayList<ArrayList<String>> listOfLists = new ArrayList<ArrayList<String>>();
        DecimalFormat decimalFormat = new DecimalFormat("0.00");

        if (list.size() != 0) {
            if(list.get(0) instanceof Income) {
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
}
