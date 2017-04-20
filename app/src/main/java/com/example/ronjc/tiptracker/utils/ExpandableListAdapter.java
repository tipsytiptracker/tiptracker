package com.example.ronjc.tiptracker.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ronjc.tiptracker.model.Expense;
import com.example.ronjc.tiptracker.model.Income;
import com.example.ronjc.tiptracker.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.ronjc.tiptracker.utils.FontManager.BITTER;
import static com.example.ronjc.tiptracker.utils.FontManager.getTypeface;

/**
 * TODO: This Class needs a lot of clean up!
 *
 * Custom adapter to create expandable list view
 * Categories for incomes and expenses act as the "headers" for list item group.
 *
 * Much of the code was based off/taken from:
 *
 * http://www.androidhive.info/2013/07/android-expandable-list-view-tutorial/
 *
 * @author Ronald Mangiliman
 * Created on 4/15/2017.
 */

public class ExpandableListAdapter extends BaseExpandableListAdapter{

    private final static String INCOME = "income";

    private Context context;
    private List<String> headerList;
    private HashMap<String, List<String>> childList;
    private String userID;
    private String type;
    private String categoryKey;


    public ExpandableListAdapter(Context context, List<String> headerList, HashMap<String, List<String>> chidList, String userID, String type) {
        this.context = context;
        this.headerList = headerList;
        this.childList = chidList;
        this.userID = userID;
        this.type = type;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.childList.get(this.headerList.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean iaLastChild, View convertView, ViewGroup viewGroup) {
        final String childText = (String) getChild(groupPosition, childPosition);
        if (convertView == null) {
            LayoutInflater mLayoutInflator = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = mLayoutInflator.inflate(R.layout.list_item, null);
        }
        TextView textListChild = (TextView) convertView.findViewById(R.id.lblListItem);
        textListChild.setText(childText);

        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.childList.get(this.headerList.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.headerList.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this.headerList.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    //TODO: In particular, this block needs a lot of clean up
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, final ViewGroup viewGroup) {
        ButterKnife.bind(viewGroup);
        final String headerTitle = (String) getGroup(groupPosition);
        final LayoutInflater mLayoutInflator = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(convertView == null) {
            convertView = mLayoutInflator.inflate(R.layout.list_group, null);
        }
        TextView lblListHeader = (TextView)convertView.findViewById(R.id.lblListHeader);
        TextView plusIcon = (TextView)convertView.findViewById(R.id.add_icon);

        Typeface bitter = FontManager.getTypeface(convertView.getContext(), BITTER);
        Typeface fontAwesome = FontManager.getTypeface(convertView.getContext(), FontManager.FONTAWESOME);
        lblListHeader.setTypeface(bitter, Typeface.BOLD);
        plusIcon.setTypeface(fontAwesome);
        lblListHeader.setText(headerTitle);
        plusIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: Clean this code up
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
                final View mView = mLayoutInflator.inflate(R.layout.add_budget_dialog, null);
                final Typeface bitter = getTypeface(context, FontManager.BITTER);
                Typeface fontAwesome = getTypeface(context, FontManager.FONTAWESOME);
                TextView cameraIcon = (TextView) mView.findViewById(R.id.camera_icon);
                TextView pencilIcon = (TextView) mView.findViewById(R.id.pencil_icon);
                ((TextView) mView.findViewById(R.id.budget_dialog_header)).setTypeface(bitter);
                ((TextView) mView.findViewById(R.id.camera_icon_text)).setTypeface(bitter);
                ((TextView) mView.findViewById(R.id.pencil_icon_text)).setTypeface(bitter);

                cameraIcon.setTypeface(fontAwesome);
                pencilIcon.setTypeface(fontAwesome);
                mBuilder.setView(mView);
                final AlertDialog dialog = mBuilder.create();

                //TODO: Set on click listener for camera here

                pencilIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        AlertDialog.Builder pencilBuilder = new AlertDialog.Builder(context);
                        final View pencilView = mLayoutInflator.inflate(R.layout.add_budget_manually, null);
                        ((TextView)pencilView.findViewById(R.id.budget_manually_header)).setTypeface(bitter);
                        TextView pencilHeader = (TextView) pencilView.findViewById(R.id.budget_manually_header);
                        pencilHeader.setTypeface(bitter);
                        pencilHeader.setText(context.getString(R.string.add) + " " + headerTitle);
                        final TextInputLayout itemName = (TextInputLayout) pencilView.findViewById(R.id.add_item_name_text_input);
                        final EditText itemNameEditText = (EditText) pencilView.findViewById(R.id.add_item_name);
                        itemNameEditText.setTypeface(bitter);
                        TextInputLayout itemAmount = (TextInputLayout) pencilView.findViewById(R.id.add_item_amount_text_input);
                        final EditText itemAmountEditText = (EditText) pencilView.findViewById(R.id.item_amount);
                        itemAmountEditText.setTypeface(bitter);
                        Button itemButton = (Button) pencilView.findViewById(R.id.add_item_manually_button);
                        itemName.setTypeface(bitter);
                        itemAmount.setTypeface(bitter);
                        itemButton.setTypeface(bitter);
                        pencilBuilder.setView(pencilView);
                        final AlertDialog pencilDialog = pencilBuilder.create();
                        //TRIPLE NESTED ONCLICKLISTENERS?!?!?!
                        itemButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if(type.equals(DBHelper.INCOMES)) {
                                    writeNewIncome(itemNameEditText.getText().toString(), itemAmountEditText.getText().toString(), headerTitle);
                                } else {
                                    writeNewExpense(itemNameEditText.getText().toString(), itemAmountEditText.getText().toString(), headerTitle);
                                }
                                pencilDialog.dismiss();
                            }
                        });
                        pencilDialog.show();
                    }
                });
                dialog.show();
            }
        });
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }

    private void writeNewIncome(final String name, String amount, final String category) {
        final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        //Get rid of dollar sign and any commas
        final double doubleAmount = Double.parseDouble(amount.substring(1).replace(",", ""));
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
                String incomeKey = mDatabase.child(DBHelper.PERIODS).child(periodToWrite).child(DBHelper.INCOMES).push().getKey();
                mDatabase.child(DBHelper.PERIODS).child(periodToWrite).child(DBHelper.INCOMES).child(incomeKey).setValue(true);
                Income income = new Income(userID, name, doubleAmount, System.currentTimeMillis(), category, userID);
                mDatabase.child(DBHelper.INCOMES).child(incomeKey).setValue(income);
                Toast.makeText(context, context.getString(R.string.income_added), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void writeNewExpense(String name, String amount, String category) {
        final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        double doubleAmount = Double.parseDouble(amount.substring(1));
        final Expense expense = new Expense(userID, name, doubleAmount, System.currentTimeMillis(), category, userID);
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
                String expenseKey = mDatabase.child(DBHelper.PERIODS).child(periodToWrite).child(DBHelper.EXPENSES).push().getKey();
                mDatabase.child(DBHelper.PERIODS).child(periodToWrite).child(DBHelper.EXPENSES).child(expenseKey).setValue(true);
                mDatabase.child(DBHelper.EXPENSES).child(expenseKey).setValue(expense);
                Toast.makeText(context, context.getString(R.string.income_added), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
