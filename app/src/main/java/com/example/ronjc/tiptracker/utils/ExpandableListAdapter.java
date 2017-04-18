package com.example.ronjc.tiptracker.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.example.ronjc.tiptracker.model.Expense;
import com.example.ronjc.tiptracker.model.Income;
import com.example.ronjc.tiptracker.utils.FontManager;
import com.example.ronjc.tiptracker.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.List;

import static android.R.style.Theme_Material_Light_Dialog_Alert;
import static android.app.AlertDialog.THEME_HOLO_LIGHT;
import static com.example.ronjc.tiptracker.utils.FontManager.BITTER;
import static com.example.ronjc.tiptracker.utils.FontManager.getTypeface;
import static com.example.ronjc.tiptracker.utils.FontManager.markAsIconContainer;

/**
 * http://www.androidhive.info/2013/07/android-expandable-list-view-tutorial/
 * Created by ronjc on 4/15/2017.
 */

public class ExpandableListAdapter extends BaseExpandableListAdapter{

    private Context context;
    private List<String> headerList;
    private HashMap<String, List<String>> childList;
    private String userID;

    public ExpandableListAdapter(Context context, List<String> headerList, HashMap<String, List<String>> chidList, String userID) {
        this.context = context;
        this.headerList = headerList;
        this.childList = chidList;
        this.userID = userID;
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

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup viewGroup) {
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
                View mView = mLayoutInflator.inflate(R.layout.add_budget_dialog, null);
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
                        View pencilView = mLayoutInflator.inflate(R.layout.add_budget_manually, null);
                        ((TextView)pencilView.findViewById(R.id.budget_manually_header)).setTypeface(bitter);
                        TextView pencilHeader = (TextView) pencilView.findViewById(R.id.budget_manually_header);
                        pencilHeader.setTypeface(bitter);
                        pencilHeader.setText(context.getString(R.string.add) + " " + headerTitle);
                        TextInputLayout itemName = (TextInputLayout) pencilView.findViewById(R.id.add_item_name_text_input);
                        TextInputLayout itemAmount = (TextInputLayout) pencilView.findViewById(R.id.add_item_amount_text_input);
                        itemName.setTypeface(bitter);
                        itemAmount.setTypeface(bitter);

                        pencilBuilder.setView(pencilView);
                        AlertDialog pencilDialog = pencilBuilder.create();
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

    private void writeNewCategory(String catgory) {
        //Write new category to Firebase
    }

    private void writeNewIncome(String name, String amount, String category) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        double doubleAmount = Double.parseDouble(amount.substring(1));
        Income income = new Income(userID, name, doubleAmount, System.currentTimeMillis(), category, userID);
        //TODO: Push to database
//        mDatabase.child("users").child(userID).child()


    }

    private void writeNewExpense(Expense expense) {

    }

}
