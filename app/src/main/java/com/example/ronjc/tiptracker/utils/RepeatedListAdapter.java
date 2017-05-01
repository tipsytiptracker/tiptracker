package com.example.ronjc.tiptracker.utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

import java.util.HashMap;
import java.util.List;

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

    public RepeatedListAdapter(Context context, List<String> headerList, HashMap<String, List<String>> childList,
                                HashMap<String, List<String>> idList) {
        this.context = context;
        this.headerList = headerList;
        this.childList = childList;
        this.idList = idList;
    }

    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
        return null;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        return null;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return false;
    }

    @Override
    public int getGroupCount() {
        return 0;
    }

    @Override
    public int getChildrenCount(int i) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public Object getChild(int i, int i1) {
        return null;
    }

    @Override
    public long getGroupId(int i) {
        return 0;
    }

    @Override
    public Object getGroup(int i) {
        return null;
    }

    @Override
    public long getChildId(int i, int i1) {
        return 0;
    }
}
