package com.example.ronjc.tiptracker.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.SparseArray;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.util.List;

/**
 * Created by Alex on 4/15/2017.
 */

public class OCR {

    private TextRecognizer textRecognizer;
    private Bitmap bitmap;
    private SparseArray<TextBlock> textBlocks;

    public OCR(Context context, Bitmap bitmap) {
        textRecognizer = new TextRecognizer.Builder(context).build();
        Frame imageFrame = new Frame.Builder()
                .setBitmap(bitmap)
                .build();
        textBlocks = textRecognizer.detect(imageFrame);
    }

    public String getTotal() {
        String total = "";
        List<? extends com.google.android.gms.vision.text.Text> lineList;
        List<? extends com.google.android.gms.vision.text.Text> wordList;
        outerloop:
        for (int i = 0; i < textBlocks.size(); i++) {
            TextBlock textBlock = textBlocks.get(textBlocks.keyAt(i));
            lineList = textBlock.getComponents();
            for (int j = 0; j < lineList.size(); j++) {
                wordList = lineList.get(j).getComponents();
                for (int k = 0; k < wordList.size(); k++) {
                    if (wordList.get(k).getValue().equalsIgnoreCase("total")) {
                        if (wordList.get(k + 1).getValue().equals("$") ||
                                wordList.get(k + 1).getValue().equalsIgnoreCase("s") )
                            total = wordList.get(k + 2).getValue();
                         else
                            total = wordList.get(k + 1).getValue();
                        break outerloop;
                    }
                }
            }
        }
        return total;
    }
}
