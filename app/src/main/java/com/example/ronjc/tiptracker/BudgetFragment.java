package com.example.ronjc.tiptracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import com.example.ronjc.tiptracker.utils.Camera;
import com.example.ronjc.tiptracker.utils.DBHelper;
import com.example.ronjc.tiptracker.utils.DateManager;
import com.example.ronjc.tiptracker.utils.ExpandableListAdapter;
import com.example.ronjc.tiptracker.utils.FontManager;
import com.example.ronjc.tiptracker.utils.OCR;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


import static android.app.Activity.RESULT_OK;
import static com.example.ronjc.tiptracker.utils.FontManager.BITTER;
import static com.example.ronjc.tiptracker.utils.FontManager.FONTAWESOME;


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
    private static final String TOTAL_KEY = "total";
    private static final String CAMERA_KEY = "camera";
    private static final String LONGITUDE_KEY = "longitude";
    private static final String LATITUDE_KEY = "latitude";

    private String type = "";

    //User ID being passed from ExpandableListAdapter
    private String userID = "";
    private int page;
    private String currentPeriodID = "";
    private double total = 0.00;
    private ExpandableListAdapter listAdapter;
    private ExpandableListView expandableListView;

    //List of either Incomes or Expenses to display
    private ArrayList<?> list;

    //List of category "headers"
    private ArrayList<String> headerList;

    //HashMap of children
    private HashMap<String, List<String>> childList;
    //HashMap of IDs
    private HashMap<String, List<String>> idList;

    private OnFragmentInteractionListener mListener;

    private ArrayList<Double> amountsByCategory;

    private LinearLayout mPieLayout;
    private PieChart mPieChart;

    private DatabaseReference mDatabaseReference;

    private ViewGroup viewGroup;

    DecimalFormat decimalFormat = new DecimalFormat("0.00");

    private Camera mCamera;

    private long longitude, latitude;

    public BudgetFragment() {
        // Required empty public constructor
    }

    public static BudgetFragment newInstance(int page, ArrayList<? extends Serializable> list,
                                             Date startDate, String userID, String currentPeriodID,
                                             double total, Camera camera, long longitude, long latitude) {
        BudgetFragment mBudgetFragment = new BudgetFragment();
        Bundle args = new Bundle();
        args.putInt(PAGE_KEY, page);
        args.putSerializable(LIST_KEY, list);
        args.putString(USER_ID_KEY, userID);
        args.putString(PERIOD_KEY, currentPeriodID);
        args.putDouble(TOTAL_KEY, total);
        args.putParcelable(CAMERA_KEY, camera);
        args.putLong(LONGITUDE_KEY, longitude);
        args.putLong(LATITUDE_KEY, latitude);
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
            total = getArguments().getDouble(TOTAL_KEY, 0);
            mCamera = (Camera) getArguments().getParcelable(CAMERA_KEY);
            longitude = getArguments().getLong(LONGITUDE_KEY);
            latitude = getArguments().getLong(LATITUDE_KEY);
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_budget, container, false);
        viewGroup = container;
        type = page == 1 ? DBHelper.INCOMES : DBHelper.EXPENSES;
        expandableListView = (ExpandableListView) view.findViewById(R.id.budget_list);
        final TextView mTotalTextView = (TextView)view.findViewById(R.id.total_tv);
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
                listAdapter = new ExpandableListAdapter(view.getContext(), headerList, childList,
                                    idList, userID, type, currentPeriodID, mTotalTextView, mCamera,
                                    amountsByCategory, longitude, latitude);
                expandableListView.setAdapter(listAdapter);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        Button addCategoryButton = (Button)view.findViewById(R.id.add_category_button);
        TextView mPieChart = (TextView)view.findViewById(R.id.pie_chart_icon);
        final Typeface bitter = FontManager.getTypeface(view.getContext(), BITTER);
        Typeface fontAwesome = FontManager.getTypeface(view.getContext(), FONTAWESOME);
        addCategoryButton.setTypeface(bitter);
        mTotalTextView.setTypeface(bitter);
        mPieChart.setTypeface(fontAwesome);

        mPieChart.setOnClickListener(new PieChartListener());
        mTotalTextView.setText(getString(R.string.total) + decimalFormat.format(total));
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

    /**
     * Prepares the data to be utilized by the ExpandableListAdapter.
     */
    private void prepareListData() {
        ArrayList<Income> incomes;
        ArrayList<Expense> expenses;

        //HashMap of Categories with LIst String representing incomes
        childList = new HashMap<String, List<String>>();

        //HashMap of Categories and List of Income IDs belinging to that income
        idList = new HashMap<String, List<String>>();

        //Amounts categorized by...category lol
        amountsByCategory = new ArrayList<Double>();

        //Initial local lists of ArrayLists
        ArrayList<ArrayList<String>> listOfLists = new ArrayList<ArrayList<String>>();
        ArrayList<ArrayList<String>> listOfIDLists= new ArrayList<ArrayList<String>>();

        //For formatting
        DecimalFormat decimalFormat = new DecimalFormat("0.00");

        //BigDecimal references for accurate precision. For calculating category totals
        BigDecimal bigDecimal1;
        BigDecimal bigDecimal2;

        //Check if incomes or expenses. 1 is incomes, 2 is expenses
        if(type.equals(DBHelper.INCOMES)) {

            //If there are no children for categories, then fill HashMaps with empty arrays
            if(list.size() == 0) {
                for (int i = 0; i < headerList.size(); i++) {
                    childList.put(headerList.get(i), new ArrayList<String>());
                    idList.put(headerList.get(i), new ArrayList<String>());
                    amountsByCategory.add(0.00);
                }
            //Else, populate lists of lists with empty lists and add 0.00 for each category
            } else {
                for(int i = 0; i < headerList.size(); i++) {
                    listOfLists.add(new ArrayList<String>());
                    listOfIDLists.add(new ArrayList<String>());
                    amountsByCategory.add(0.00);

                }

                String stringToAdd;
                incomes = (ArrayList<Income>)list;

                //For some reason, arraylist contains null elements on creation. remove them using this line
                incomes.removeAll(Collections.singleton(null));

                //For each income
                for(Income income : incomes) {
                    //Find the index of the category it belongs to in headerList
                    int index = headerList.indexOf(income.getCategory());

                    //Create String to display in expandable list
                    stringToAdd = income.getName() + ": $" + decimalFormat.format(income.getAmount());

                    //Add to corresponding lists in arraylists of lists
                    listOfLists.get(index).add(stringToAdd);
                    listOfIDLists.get(index).add(income.getId());

                    //Use Big Decimal to retain precision
                    bigDecimal1 = new BigDecimal(amountsByCategory.get(index));
                    bigDecimal2 = new BigDecimal(income.getAmount());
                    bigDecimal1 = bigDecimal1.setScale(2, BigDecimal.ROUND_HALF_UP);
                    bigDecimal2 = bigDecimal2.setScale(2, BigDecimal.ROUND_HALF_UP);

                    //Change the corresponding total by category with new total
                    amountsByCategory.set(index, bigDecimal1.add(bigDecimal2).doubleValue());
                }
                //Create HashMaps
                for(int i = 0; i < headerList.size(); i++) {
                    childList.put(headerList.get(i), listOfLists.get(i));
                    idList.put(headerList.get(i), listOfIDLists.get(i));
                }
            }
        //Same as above, but for expenses
        } else {
            if(list.size() == 0) {
                for (int i = 0; i < headerList.size(); i++) {
                    childList.put(headerList.get(i), new ArrayList<String>());
                    idList.put(headerList.get(i), new ArrayList<String>());
                    amountsByCategory.add(0.00);
                }
            } else {
                for(int i = 0; i < headerList.size(); i++) {
                    listOfLists.add(new ArrayList<String>());
                    listOfIDLists.add(new ArrayList<String>());
                    amountsByCategory.add(0.00);
                }
                String stringToAdd;
                expenses = (ArrayList<Expense>)list;
                expenses.removeAll(Collections.singleton(null));
                for(Expense expense : expenses) {
                    int index = headerList.indexOf(expense.getCategory());
                    stringToAdd = expense.getName() + ": $" + decimalFormat.format(expense.getAmount());
                    listOfLists.get(index).add(stringToAdd);
                    listOfIDLists.get(index).add(expense.getId());
                    bigDecimal1 = new BigDecimal(amountsByCategory.get(index));
                    bigDecimal2 = new BigDecimal(expense.getAmount());
                    bigDecimal1 = bigDecimal1.setScale(2, BigDecimal.ROUND_HALF_UP);
                    bigDecimal2 = bigDecimal2.setScale(2, BigDecimal.ROUND_HALF_UP);
                    amountsByCategory.set(index, bigDecimal1.add(bigDecimal2).doubleValue());
                }
                for(int i = 0; i < headerList.size(); i++) {
                    childList.put(headerList.get(i), listOfLists.get(i));
                    idList.put(headerList.get(i), listOfIDLists.get(i));
                }
            }
        }
    }

    /**
     * Method that writes a new category entry into the database and updates view with that entry
     * @param category new category
     */
    private void writeNewCategory(final String category) {
        //Database reference
        final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

        //Get new key of category item being pushed
        String categoryKey = mDatabase.child(DBHelper.PERIODS).child(currentPeriodID).child(DBHelper.CATEGORIES).child(type).push().getKey();

        //Actually push and set value for new child
        mDatabase.child(DBHelper.PERIODS).child(currentPeriodID).child(DBHelper.CATEGORIES).child(type).child(categoryKey).setValue(category);

        //Alert user
        Snackbar.make(getActivity().findViewById(R.id.activity_budget_management), getString(R.string.category_added), Snackbar.LENGTH_SHORT).show();

        //Update local data
        headerList.add(category);
        childList.put(category, new ArrayList<String>());
        idList.put(category, new ArrayList<String>());
        amountsByCategory.add(0.00);

        //Update view
        listAdapter.notifyDataSetChanged();
    }

    private class PieChartListener implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View pieView = layoutInflater.inflate(R.layout.piechart, viewGroup, false);
            mPieLayout = (LinearLayout) pieView.findViewById(R.id.pie_chart_outer_layout);
            AlertDialog.Builder pieChartBuilder = new AlertDialog.Builder(getContext());
            mPieChart = (PieChart) pieView.findViewById(R.id.actual_pie_chart);

            mPieChart.setUsePercentValues(true);
            mPieChart.setDrawHoleEnabled(true);
            mPieChart.setHoleRadius(16);
            mPieChart.setTransparentCircleRadius(20);
            mPieChart.setRotationAngle(0);
            mPieChart.setRotationEnabled(true);

            addData();

            pieChartBuilder.setView(pieView);
            final AlertDialog dialog = pieChartBuilder.create();
            dialog.show();
        }

        private void addData() {
            List<PieEntry> yValues = new ArrayList<PieEntry>();
            for (int i = 0; i < amountsByCategory.size(); i++) {
                yValues.add(new PieEntry(amountsByCategory.get(i).floatValue(), i));
            }
            PieDataSet mDataSet = new PieDataSet(yValues, getString(R.string.pie_chart_label));
            mDataSet.setSliceSpace(3);
            mDataSet.setSelectionShift(5);

            ArrayList<Integer> colors = new ArrayList<Integer>();

            for (int c : ColorTemplate.VORDIPLOM_COLORS) {
                colors.add(c);
            }


            for (int c : ColorTemplate.JOYFUL_COLORS) {
                colors.add(c);
            }

            for (int c : ColorTemplate.COLORFUL_COLORS) {
                colors.add(c);
            }

            for (int c : ColorTemplate.LIBERTY_COLORS) {
                colors.add(c);
            }

            for (int c : ColorTemplate.PASTEL_COLORS) {
                colors.add(c);

            }

            colors.add(ColorTemplate.getHoloBlue());
            mDataSet.setColors(colors);

            PieData data = new PieData(mDataSet);
            data.setValueFormatter(new PercentFormatter());
            data.setValueTextSize(11f);
            data.setValueTextColor(Color.BLACK);

            mPieChart.setData(data);

            mPieChart.highlightValues(null);

            mPieChart.invalidate();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Camera.REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            Bitmap mBitmap = mCamera.getBitmap();
            OCR mOCR = new OCR(getContext(), mBitmap);
            String testString = mOCR.getTotal();
            Toast.makeText(getContext(), testString, Toast.LENGTH_SHORT).show();
        }
    }
}
