package com.example.ronjc.tiptracker.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Helper class for managing fonts
 *
 * Majority of code used was from:
 * https://code.tutsplus.com/tutorials/how-to-use-fontawesome-in-an-android-app--cms-24167
 *
 * @author Ronald Mangiliman
 * Created on 4/5/2017.
 */

public class FontManager {
    //Fonts folder
    private static final String ROOT = "fonts/";

    //References to each separate true type font files
    //Font Awesome for Icons
    public static final String FONTAWESOME = ROOT + "fontawesome-webfont.ttf";
    //Bitter for everything else
    public static final String BITTER = ROOT + "bitter.ttf";

    //Creates a typeface from specified ttf file
    public static Typeface getTypeface(Context context, String font) {
        return Typeface.createFromAsset(context.getAssets(), font);
    }

    /**
     * Recursive method that sets typeface of each TextView child of given view
     * @param v Parent View
     * @param typeface typeface to set
     */
    public static void markAsIconContainer(View v, Typeface typeface) {
        if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            for (int i = 0; i < vg.getChildCount(); i++) {
                View child = vg.getChildAt(i);
                markAsIconContainer(child, typeface);
            }
        } else if (v instanceof TextView) {
            ((TextView) v).setTypeface(typeface);
        }
    }
}
