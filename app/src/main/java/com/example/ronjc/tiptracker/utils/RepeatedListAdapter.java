package com.example.ronjc.tiptracker.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.ronjc.tiptracker.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static com.example.ronjc.tiptracker.utils.FontManager.BITTER;
import static com.example.ronjc.tiptracker.utils.FontManager.FONTAWESOME;
import static com.example.ronjc.tiptracker.utils.FontManager.getTypeface;

/**
 *
 * @author Ronald Mangiliman
 * Created by Ronald Mangiliman on 4/30/2017.
 */

public class RepeatedListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> headerList;
    private HashMap<String, List<String>> childList;
    private HashMap<String, List<String>> idList;
    private String[] typeArray, frequencyArray;
    private Typeface bitter;
    private String periodID,selectedType;
    private DatabaseReference dbRef;
    private FirebaseUser user;
    private FirebaseAuth mAuth;
    private String key="";


    public RepeatedListAdapter(Context context, List<String> headerList, HashMap<String, List<String>> childList,
                               HashMap<String, List<String>> idList, String[] typeArray, String[] frequencyArray) {
        this.context = context;
        this.headerList = headerList;
        this.childList = childList;
        this.idList = idList;
        this.typeArray = typeArray;
        this.frequencyArray = frequencyArray;

        dbRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean iaLastChild, View convertView, ViewGroup viewGroup) {
        final String childText = (String) getChild(groupPosition, childPosition);
        if (convertView == null) {
            LayoutInflater mLayoutInflator = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = mLayoutInflator.inflate(R.layout.list_item, null);
        }
        TextView textListChild = (TextView) convertView.findViewById(R.id.lblListItem);
        TextView removeIcon = (TextView) convertView.findViewById(R.id.remove_icon);
        Typeface bitter = FontManager.getTypeface(convertView.getContext(), BITTER);
        Typeface fontAwesome = FontManager.getTypeface(convertView.getContext(), FONTAWESOME);
        textListChild.setTypeface(bitter);
        removeIcon.setTypeface(fontAwesome);
        textListChild.setText(childText);
        removeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmRemoval(groupPosition, childPosition);
            }
        });
        return convertView;
    }
    @Override
    public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, final ViewGroup viewGroup) {
        ButterKnife.bind(viewGroup);
        final String headerTitle = (String) getGroup(groupPosition);
        LayoutInflater mLayoutInflator = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mLayoutInflator.inflate(R.layout.list_group, null);
        }
        TextView lblListHeader = (TextView) convertView.findViewById(R.id.lblListHeader);
        TextView plusIcon = (TextView) convertView.findViewById(R.id.add_icon);

        final Typeface bitter = FontManager.getTypeface(convertView.getContext(), BITTER);
        final Typeface fontAwesome = FontManager.getTypeface(convertView.getContext(), FontManager.FONTAWESOME);
        lblListHeader.setTypeface(bitter, Typeface.BOLD);
        plusIcon.setTypeface(fontAwesome);
        lblListHeader.setText(headerTitle);

        plusIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayAddRepeatedDialog(headerList.get(groupPosition));
            }
        });
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return false;
    }

    @Override
    public int getGroupCount() {
        return this.headerList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.childList.get(this.headerList.get(groupPosition)).size();
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.childList.get(this.headerList.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.headerList.get(groupPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }


    /**
     *
     */
    public void displayAddRepeatedDialog(final String category) {
        bitter = FontManager.getTypeface(context, BITTER);

        LayoutInflater mLayoutInflator = (LayoutInflater)context.getSystemService(LAYOUT_INFLATER_SERVICE);
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
        View mView = mLayoutInflator.inflate(R.layout.add_repeated_dialog, null);
        ((TextView)mView.findViewById(R.id.add_repeated_header)).setTypeface(bitter);

        TextInputLayout itemNameTextInput = (TextInputLayout)mView.findViewById(R.id.add_repeated_name_text_input);
        TextInputLayout itemAmountTextInput = (TextInputLayout)mView.findViewById(R.id.add_repeated_amount_text_input);
        final EditText itemNameEditText = (EditText)mView.findViewById(R.id.add_repeated_name);
        final EditText itemAmountEditText = (EditText)mView.findViewById(R.id.repeated_amount);
//        final Spinner repeatedSpinner = (Spinner)mView.findViewById(R.id.repeated_type_spinner);
        final Spinner frequencySpinner = (Spinner)mView.findViewById(R.id.repeated_frequency_spinner);
        final Button repeatedDialogButton = (Button) mView.findViewById(R.id.add_repeated_dialog_button);

//        //Create array adapter for spinner
//        ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<String>(context, R.layout.custom_spinner_item, typeArray) {
//            @NonNull
//            @Override
//            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
//                View mView = super.getView(position, convertView, parent);
//                ((TextView)mView).setTypeface(bitter);
//                return mView;
//            }
//
//            @Override
//            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
//                View mView = super.getDropDownView(position, convertView, parent);
//                ((TextView)mView).setTypeface(bitter);
//                return mView;
//            }
//        };
//        mArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        repeatedSpinner.setAdapter(mArrayAdapter);

        ArrayAdapter<String> mFrequencyAdapter = new ArrayAdapter<String>(context, R.layout.custom_spinner_item, frequencyArray) {
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
        mFrequencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        frequencySpinner.setAdapter(mFrequencyAdapter);

        //Set font styling to Bitter
        itemNameTextInput.setTypeface(bitter);
        itemAmountTextInput.setTypeface(bitter);
        itemNameEditText.setTypeface(bitter);
        itemAmountEditText.setTypeface(bitter);
        repeatedDialogButton.setTypeface(bitter);

        //set on item selected listener for spinner
//        repeatedSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                selectedType = adapterView.getSelectedItem().toString();
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//
//            }
//        });

        //Create and show dialog
        mBuilder.setView(mView);
        final AlertDialog mAlertDialog = mBuilder.create();

        //Button to set repeated income and expenses in the Firebase Database
        repeatedDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String itemName = itemNameEditText.getText().toString();
                String itemAmount = itemAmountEditText.getText().toString()
                        .replace("$","").replace(".","").replace(",","");
                BigDecimal amount = new BigDecimal(itemAmount);
                DecimalFormat decimalFormat = new DecimalFormat("0.00");
                if(category.equals("Repeated Incomes")) {
                    if(frequencySpinner.getSelectedItem().equals("Monthly")){
                        BigDecimal divisor = new BigDecimal(4);
                        amount = amount.divide(divisor);
                    }
                    if(frequencySpinner.getSelectedItem().equals("Annually")){
                        BigDecimal divisor = new BigDecimal(52);
                        amount = amount.divide(divisor);
                    }

                    amount = amount.setScale(2,BigDecimal.ROUND_HALF_UP);


                    key = dbRef.child(DBHelper.USERS).child(user.getUid()).child("RepeatedIncome").push().getKey();
                    dbRef.child(DBHelper.USERS).child(user.getUid()).child("RepeatedIncome")
                            .child(key).child("name").setValue(itemName);
                    dbRef.child(DBHelper.USERS).child(user.getUid()).child("RepeatedIncome").child(key)
                            .child("amount").setValue(amount.doubleValue()/100);

                    Snackbar.make(view,"Income was added!",Snackbar.LENGTH_SHORT).show();
                    String incomeString = itemName + ": $" + decimalFormat.format(amount.doubleValue()/100);
                    childList.get(headerList.get(0)).add(incomeString);
                    mAlertDialog.dismiss();
                    notifyDataSetChanged();
                }
                if(category.equals("Repeated Expenses")) {
                    if(frequencySpinner.getSelectedItem().equals("Monthly")){
                        BigDecimal divisor = new BigDecimal(4);
                        amount = amount.divide(divisor);
                    }
                    if(frequencySpinner.getSelectedItem().equals("Annually")){
                        BigDecimal divisor = new BigDecimal(12);
                        amount = amount.divide(divisor);
                    }

                    amount = amount.setScale(2,BigDecimal.ROUND_HALF_UP);


                    key = dbRef.child(DBHelper.USERS).child(user.getUid()).child("RepeatedExpense").push().getKey();
                    dbRef.child(DBHelper.USERS).child(user.getUid()).child("RepeatedExpense")
                            .child(key).child("name").setValue(itemName);
                    dbRef.child(DBHelper.USERS).child(user.getUid()).child("RepeatedExpense")
                            .child(key).child("amount").setValue(amount.doubleValue()/100);

                    Snackbar.make(view,"Expense was added!",Snackbar.LENGTH_SHORT).show();
                    String expenseString = itemName + ": $" + decimalFormat.format(amount.doubleValue()/100);
                    childList.get(headerList.get(1)).add(expenseString);
                    mAlertDialog.dismiss();
                    notifyDataSetChanged();
                }


            }
        });

        mAlertDialog.show();
    }

    private void confirmRemoval(final int groupPosition, final int childPosition) {

        //Build Dialog
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
        LayoutInflater mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View mView = mLayoutInflater.inflate(R.layout.confirm_removal, null);
        Typeface bitter = getTypeface(context, FontManager.BITTER);
        TextView confirmRemovalHeader = (TextView)mView.findViewById(R.id.confirm_removal_header);
        TextView areYouSure = (TextView)mView.findViewById(R.id.confirm_text_view);
        Button confirmRemovalButton = (Button)mView.findViewById(R.id.confirm_removal_button);
        Button cancelRemovalButton = (Button)mView.findViewById(R.id.cancel_removal_button);

        //Set font styling
        areYouSure.setTypeface(bitter);
        confirmRemovalHeader.setTypeface(bitter);
        confirmRemovalButton.setTypeface(bitter);
        cancelRemovalButton.setTypeface(bitter);
        mBuilder.setView(mView);

        //Show Dialog
        final AlertDialog alertDialog = mBuilder.create();
        alertDialog.show();

        //Remove item and dismiss dialog
        confirmRemovalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeItem(groupPosition, childPosition);
                alertDialog.dismiss();
            }
        });

        //Dismiss dialog without removing item
        cancelRemovalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
    }

    /**
     * Removes item from user view and in the database
     *
     * @param groupPosition position of the group of item being clicked.
     * @param childPosition position of the child within the group
     */
    private void removeItem(int groupPosition, int childPosition) {
        //Formatting
        DecimalFormat decimalFormat = new DecimalFormat("0.00");

        //Retrieve the entry being clicked
        String item = childList.get(headerList.get(groupPosition)).get(childPosition);

        String typeToDelete = headerList.get(groupPosition).equals("Repeated Incomes") ? DBHelper.REPEATED_INCOME : DBHelper.REPEATED_EXPENSE;

        //Remove child entry from HashMap
        childList.get(headerList.get(groupPosition)).remove(childPosition);

        //Store the ID of removed item for later use
        String itemID = idList.get(headerList.get(groupPosition)).get(childPosition);

        //Remove ID from HashMap locally
        idList.get(headerList.get(groupPosition)).remove(childPosition);


        //Remove the item from database.
        dbRef.child(DBHelper.USERS).child(user.getUid()).child(typeToDelete).child(itemID).setValue(null);

        //Notify adapter of data change and update view
        notifyDataSetChanged();
    }
}
