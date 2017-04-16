package com.example.ronjc.tiptracker.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.example.ronjc.tiptracker.R;

import java.util.HashMap;
import java.util.List;

/**
 * http://www.androidhive.info/2013/07/android-expandable-list-view-tutorial/
 * Created by ronjc on 4/15/2017.
 */

public class ExpandableListAdapter extends BaseExpandableListAdapter{

    private Context context;
    private List<String> headerList;
    private HashMap<String, List<String>> childList;

    public ExpandableListAdapter(Context context, List<String> headerList, HashMap<String, List<String>> chidList) {
        this.context = context;
        this.headerList = headerList;
        this.childList = chidList;
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
        String headerTitle = (String) getGroup(groupPosition);
        if(convertView == null) {
            LayoutInflater mLayoutInflator = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = mLayoutInflator.inflate(R.layout.list_group, null);
        }
        TextView lblListHeader = (TextView)convertView.findViewById(R.id.lblListHeader);

        Typeface bitter = FontManager.getTypeface(convertView.getContext(), FontManager.BITTER);
        lblListHeader.setTypeface(bitter, Typeface.BOLD);
        lblListHeader.setText(headerTitle);
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
}
