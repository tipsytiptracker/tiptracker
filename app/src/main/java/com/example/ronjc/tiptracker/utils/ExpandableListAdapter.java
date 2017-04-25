package com.example.ronjc.tiptracker.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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

import org.w3c.dom.Text;

import java.math.BigDecimal;
import java.nio.DoubleBuffer;
import java.security.DomainCombiner;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.ronjc.tiptracker.utils.FontManager.BITTER;
import static com.example.ronjc.tiptracker.utils.FontManager.FONTAWESOME;
import static com.example.ronjc.tiptracker.utils.FontManager.getTypeface;
import static java.security.AccessController.getContext;

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

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> headerList;
    private HashMap<String, List<String>> childList;
    private HashMap<String, List<String>> idList;
    private String userID;
    private String type;
    private String currentPeriodID;
    private TextView totalTextView;
    private Camera mCamera;

    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    public ExpandableListAdapter(Context context, List<String> headerList, HashMap<String,
                                List<String>> childList, HashMap<String, List<String>> idList,
                                 String userID, String type, String currentPeriodID,
                                 TextView totalTextView, Camera camera) {
        this.context = context;
        this.headerList = headerList;
        this.childList = childList;
        this.idList = idList;
        this.userID = userID;
        this.type = type;
        this.currentPeriodID = currentPeriodID;
        this.totalTextView = totalTextView;
        this.mCamera = camera;
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

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, final ViewGroup viewGroup) {
        ButterKnife.bind(viewGroup);
        final String headerTitle = (String) getGroup(groupPosition);
        final LayoutInflater mLayoutInflator = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

        //Display option dialog
        plusIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayOptions(mLayoutInflator, headerTitle, bitter, fontAwesome);
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

    /**
     * Writes new income to database by taking in Strings of income name, amount, and category.
     *
     * @param name name of income
     * @param amount string amount of income (used to parse into double)
     * @param category category the income belongs to
     */
    private void writeNewIncome(final String name, final String amount, final String category) {
        //Get rid of dollar sign and any commas
        final double doubleAmount = Double.parseDouble(amount.substring(1).replace(",", ""));
        String incomeKey = mDatabase.child(DBHelper.PERIODS).child(currentPeriodID).child(DBHelper.INCOMES).push().getKey();
        mDatabase.child(DBHelper.PERIODS).child(currentPeriodID).child(DBHelper.INCOMES).child(incomeKey).setValue(true);

        //TODO: Find solution for this. Currently, if they add to this from a past or future period, then its date will be out of the bounds of the actual period
        Income income = new Income(incomeKey, name, doubleAmount, System.currentTimeMillis(), category, userID);
        mDatabase.child(DBHelper.INCOMES).child(incomeKey).setValue(income);

        //alert user of success
        Toast.makeText(context, context.getString(R.string.income_added), Toast.LENGTH_SHORT).show();
        String stringToAdd = name + ": " + amount;

        //Get correct ArrayList via category, and then all the new entry
        childList.get(category).add(stringToAdd);

        //update the total amount
        updateTotal(doubleAmount);
    }

    /**
     * Very similiar to the writeNewIncome, except with expenses. Who would of thunk?
     *
     * @param name name of expense
     * @param amount amount of expense
     * @param category category of expense
     */
    private void writeNewExpense(String name, String amount, String category) {
        double doubleAmount = Double.parseDouble(amount.substring(1));
        String expenseKey = mDatabase.child(DBHelper.PERIODS).child(currentPeriodID).child(DBHelper.EXPENSES).push().getKey();
        final Expense expense = new Expense(expenseKey, name, doubleAmount, System.currentTimeMillis(), category, userID);
        mDatabase.child(DBHelper.PERIODS).child(currentPeriodID).child(DBHelper.EXPENSES).child(expenseKey).setValue(true);
        mDatabase.child(DBHelper.EXPENSES).child(expenseKey).setValue(expense);
        Toast.makeText(context, context.getString(R.string.expense_added), Toast.LENGTH_SHORT).show();
        String stringToAdd = name + ": " + amount;
        childList.get(category).add(stringToAdd);
        updateTotal(doubleAmount);
    }

    /**
     * Updates the total amount displayed on budget fragment. (Only when total increases)
     *
     * @param doubleAmount amount to add
     */
    private void updateTotal(double doubleAmount) {
        //Decimal Format for displaying double in correct currency format. Avoids possibility
        //of losing last decimal when it is 0. ex. $5.50 will show as $5.5 without it.
        DecimalFormat decimalFormat = new DecimalFormat("0.00");

        //Retrieve current total
        String total = totalTextView.getText().toString().substring(8);
        double parsedTotal = Double.parseDouble(total);

        //Use BigDecimal for arithmetic operations to conserve precision
        BigDecimal bigDecimal1 = new BigDecimal(doubleAmount);
        bigDecimal1 = bigDecimal1.setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal bigDecimal2 = new BigDecimal(parsedTotal);
        bigDecimal2 = bigDecimal2.setScale(2, BigDecimal.ROUND_HALF_UP);
        parsedTotal = bigDecimal1.add(bigDecimal2).doubleValue();
        total = context.getString(R.string.total) + decimalFormat.format(parsedTotal);
        totalTextView.setText(total);
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

        //Take only the actual amount (everything after the dollar sign)
        String itemAmount = item.substring(item.lastIndexOf("$") + 1);

        //Retrieve current total amount
        String total = totalTextView.getText().toString().substring(8);

        //Parse number strings to doubles
        double parsedItemAmount = Double.parseDouble(itemAmount);
        double parsedTotal = Double.parseDouble(total);

        //Use big decimal for precision
        BigDecimal bigDecimal1 = new BigDecimal(parsedItemAmount);
        bigDecimal1 = bigDecimal1.setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal bigDecimal2 = new BigDecimal(parsedTotal);
        bigDecimal2 = bigDecimal2.setScale(2, BigDecimal.ROUND_HALF_UP);

        //subtract and convert to double
        parsedTotal = bigDecimal2.subtract(bigDecimal1).doubleValue();

        //New total string to display
        total = context.getString(R.string.total) + decimalFormat.format(parsedTotal);

        //Remove child entry from HashMap
        childList.get(headerList.get(groupPosition)).remove(childPosition);

        //Store the ID of removed item for later use
        String itemID = idList.get(headerList.get(groupPosition)).get(childPosition);

        //Remove ID from HashMap locally
        idList.get(headerList.get(groupPosition)).remove(childPosition);

        //Notify adapter of data change and update view
        notifyDataSetChanged();
        totalTextView.setText(total);

        //Get the type of item being removed (incomes or expenses)
        String typeToDelete = type.equals(DBHelper.INCOMES) ? DBHelper.INCOMES : DBHelper.EXPENSES;

        //Remove the income from database. Both in incomes tree and reference to it in periods tree
        mDatabase.child(typeToDelete).child(itemID).setValue(null);
        mDatabase.child(DBHelper.PERIODS).child(currentPeriodID).child(typeToDelete).child(itemID).setValue(null);
    }

    /**
     * Displays a dialog to confirm or cancel removal of an item
     *
     * @param groupPosition position of group that item clicked on belongs to
     * @param childPosition position of child within group
     */
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
     *
     * @param mLayoutInflator
     * @param bitter
     * @param headerTitle
     */
    private void creatFromPicture(LayoutInflater mLayoutInflator, Typeface bitter, String headerTitle) {
        mCamera.takePicture();
//            displayAddDialog(mLayoutInflator, bitter, headerTitle, ocrString);
    }

    /**
     * Display dialog called after pencil icon is clicked. This is for manual entry
     *
     * @param mLayoutInflator layout inflator
     * @param bitter bitter typeface for dialog
     * @param headerTitle category to be displayed in dialog
     */
    private void displayAddDialog(LayoutInflater mLayoutInflator, Typeface bitter, final String headerTitle) {
        //Build dialog
        AlertDialog.Builder pencilBuilder = new AlertDialog.Builder(context);
        final View pencilView = mLayoutInflator.inflate(R.layout.add_budget_manually, null);
        ((TextView) pencilView.findViewById(R.id.budget_manually_header)).setTypeface(bitter);
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

        //Set Typeface
        itemName.setTypeface(bitter);
        itemAmount.setTypeface(bitter);
        itemButton.setTypeface(bitter);

        //Show dialog
        pencilBuilder.setView(pencilView);
        final AlertDialog pencilDialog = pencilBuilder.create();

        //Write new income or expense
        itemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (type.equals(DBHelper.INCOMES)) {
                    writeNewIncome(itemNameEditText.getText().toString(), itemAmountEditText.getText().toString(), headerTitle);
                } else {
                    writeNewExpense(itemNameEditText.getText().toString(), itemAmountEditText.getText().toString(), headerTitle);
                }
                pencilDialog.dismiss();
            }
        });
        pencilDialog.show();
    }

    /**
     * Display add dialog after taking picture for OCR
     *
     * @param mLayoutInflator Layout Inflator
     * @param bitter Typeface
     * @param headerTitle category
     * @param cameraAmount String amount returned from OCR object
     */
    private void displayAddDialog(LayoutInflater mLayoutInflator, Typeface bitter, final String headerTitle, String cameraAmount) {

        //Create dialog
        AlertDialog.Builder pencilBuilder = new AlertDialog.Builder(context);
        final View pencilView = mLayoutInflator.inflate(R.layout.add_budget_manually, null);
        ((TextView) pencilView.findViewById(R.id.budget_manually_header)).setTypeface(bitter);
        TextView pencilHeader = (TextView) pencilView.findViewById(R.id.budget_manually_header);
        pencilHeader.setTypeface(bitter);
        pencilHeader.setText(context.getString(R.string.add) + " " + headerTitle);
        final TextInputLayout itemName = (TextInputLayout) pencilView.findViewById(R.id.add_item_name_text_input);
        final EditText itemNameEditText = (EditText) pencilView.findViewById(R.id.add_item_name);
        itemNameEditText.setTypeface(bitter);
        TextInputLayout itemAmount = (TextInputLayout) pencilView.findViewById(R.id.add_item_amount_text_input);
        final EditText itemAmountEditText = (EditText) pencilView.findViewById(R.id.item_amount);
        itemAmountEditText.setText(cameraAmount);
        itemAmountEditText.setTypeface(bitter);
        Button itemButton = (Button) pencilView.findViewById(R.id.add_item_manually_button);

        //Set font styling
        itemName.setTypeface(bitter);
        itemAmount.setTypeface(bitter);
        itemButton.setTypeface(bitter);

        //Show dialog
        pencilBuilder.setView(pencilView);
        final AlertDialog pencilDialog = pencilBuilder.create();

        //Write new income or expense
        itemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (type.equals(DBHelper.INCOMES)) {
                    writeNewIncome(itemNameEditText.getText().toString(), itemAmountEditText.getText().toString(), headerTitle);
                } else {
                    writeNewExpense(itemNameEditText.getText().toString(), itemAmountEditText.getText().toString(), headerTitle);
                }
                pencilDialog.dismiss();
            }
        });
        pencilDialog.show();
    }

    /**
     * Displays an options dialog that allows user to choose between manual entry or OCR entry
     *
     * @param mLayoutInflator layout inflator
     * @param headerTitle category to be displayed on dialog
     * @param bitter typeface for dialog
     * @param fontAwesome font awesome icon styling
     */
    private void displayOptions(final LayoutInflater mLayoutInflator, final String headerTitle, final Typeface bitter, final Typeface fontAwesome) {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
        final View mView = mLayoutInflator.inflate(R.layout.add_budget_dialog, null);
        TextView cameraIcon = (TextView) mView.findViewById(R.id.camera_icon);
        TextView pencilIcon = (TextView) mView.findViewById(R.id.pencil_icon);
        ((TextView) mView.findViewById(R.id.budget_dialog_header)).setTypeface(bitter);
        ((TextView) mView.findViewById(R.id.camera_icon_text)).setTypeface(bitter);
        ((TextView) mView.findViewById(R.id.pencil_icon_text)).setTypeface(bitter);

        cameraIcon.setTypeface(fontAwesome);
        pencilIcon.setTypeface(fontAwesome);
        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();

        cameraIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                creatFromPicture(mLayoutInflator, bitter, headerTitle);
            }
        });

        pencilIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                displayAddDialog(mLayoutInflator, bitter, headerTitle);
            }
        });
        dialog.show();
    }
}
