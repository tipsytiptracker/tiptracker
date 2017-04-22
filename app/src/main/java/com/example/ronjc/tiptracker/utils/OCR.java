package com.example.ronjc.tiptracker.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;
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
        TextBlock paragraph;
        List<? extends com.google.android.gms.vision.text.Text> lineList;
        List<? extends com.google.android.gms.vision.text.Text> wordList;
        Rect lineRect = new Rect(0, 0, 0, 0);
        Rect wordRect = new Rect(0, 0, 0, 0);

        firstloop:
        for (int i = 0; i < textBlocks.size(); i++) {
            paragraph = textBlocks.get(textBlocks.keyAt(i));
            lineList = paragraph.getComponents();
            for (int j = 0; j < lineList.size(); j++) {
                wordList = lineList.get(j).getComponents();
                for (int k = 0; k < wordList.size(); k++) {
                    if (wordList.get(k).getValue().equalsIgnoreCase("total") || wordList.get(k).getValue().equalsIgnoreCase("total:")) {
                        lineRect = lineList.get(j).getBoundingBox();
                        wordRect = wordList.get(k).getBoundingBox();
                        if (k + 1 < wordList.size()) {
                            if (wordList.get(k + 1).getValue().equals("$") ||
                                    wordList.get(k + 1).getValue().equals("s")) {
                                total = wordList.get(k + 2).getValue();
                            } else
                                total = wordList.get(k + 1).getValue();
                            break firstloop;
                        }
                    }
                }
            }
        }

        secondloop:
        for (int i = 0; i < textBlocks.size(); i++) {
            paragraph = textBlocks.get(textBlocks.keyAt(i));
            lineList = paragraph.getComponents();

            for (int j = 0; j < lineList.size(); j++) {
                wordList = lineList.get(j).getComponents();

                for (int k = 0; k < wordList.size(); k++) {
                    if (wordList.get(k).getBoundingBox().top > wordRect.top - 30 &&
                            wordList.get(k).getBoundingBox().top < wordRect.top + 30 &&
                            !lineList.get(j).getBoundingBox().equals(lineRect)) {
                        if (k + 1 < wordList.size()) {
                            if (wordList.get(k + 1).getValue().equals("$") ||
                                    wordList.get(k + 1).getValue().equals("s")) {
                                total = wordList.get(k + 2).getValue();
                            } else
                                total = wordList.get(k + 1).getValue();
                        } else
                            total = wordList.get(k).getValue();
                        break secondloop;
                    }
                }
            }
        }
        
        if (total.equals("")) {
            Log.d("myTotal", "blank");
            total = "0.00";
        }
        if (total.contains("$") || total.contains("s") || total.contains("S") || total.contains("g")) {
            Log.d("myTotal", total);
            total = total.substring(1);
        }
        return total;
    }
}

