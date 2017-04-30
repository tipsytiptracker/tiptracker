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
 * Optical Character Recognizer Class
 * Uses Google Vision OCR to read text from an image
 * Contains methods for find the total of a receipt and the netpay of a paystub
 */

public class OCR {

    private SparseArray<TextBlock> textBlocks;

    //Constructor needs context and a bitmap of the image to detect
    public OCR(Context context, Bitmap bitmap) {
        TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();
        Frame imageFrame = new Frame.Builder()
                .setBitmap(bitmap)
                .build();
        textBlocks = textRecognizer.detect(imageFrame);
    }

    /**
     * Name: getTotal
     * Purpose:  Reads an image and looks for the word total, then finds
     * the value that corresponds to total and returns it.
     *
     * @return total: string containing total for the receipt
     */
    public String getTotal() {
        String total = "";
        TextBlock paragraph;
        List<? extends com.google.android.gms.vision.text.Text> lineList;
        List<? extends com.google.android.gms.vision.text.Text> wordList;
        Rect lineRect = new Rect(0, 0, 0, 0);
        Rect wordRect = new Rect(0, 0, 0, 0);

        // First Loop finds the word "Total"
        //For each Text Block
        for (int i = 0; i < textBlocks.size(); i++) {
            paragraph = textBlocks.get(textBlocks.keyAt(i));
            lineList = paragraph.getComponents();
            //For each line in a Text Block
            for (int j = 0; j < lineList.size(); j++) {
                wordList = lineList.get(j).getComponents();
                //For Each word in a line
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
                        }
                    }
                }
            }
        }

        //Second loop finds the value corresponding to the bounding box of the word "Total"
        secondLoop:
        //For each Text Block
        for (int i = 0; i < textBlocks.size(); i++) {
            paragraph = textBlocks.get(textBlocks.keyAt(i));
            lineList = paragraph.getComponents();
            //For each line in a text block
            for (int j = 0; j < lineList.size(); j++) {
                wordList = lineList.get(j).getComponents();
                //For each word in a line
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
                        break secondLoop;
                    }
                }
            }
        }

        //Error checking
        if (total.equals("")) {
            Log.d("myTotal", "blank");
            total = "0.00";
        }

        if (total.contains("$") || total.contains("s") || total.contains("S") || total.contains("g")) {
            Log.d("myTotal", total);
            total = total.substring(1);
        }

        try {
            Double.parseDouble(total);
        } catch (Exception e) {
            total = "0.00";
        }
        return total;
    }

    /**
     * Name: getNetPay
     * Purpose:  Finds the Net Pay for a pay stub and returns the value.
     *
     * @return total: string containing the net pay value for the paystub
     */
    public String getNetPay() {
        String netPay = "";
        TextBlock paragraph;
        List<? extends com.google.android.gms.vision.text.Text> lineList;
        List<? extends com.google.android.gms.vision.text.Text> wordList;
        Rect lineRect = new Rect(0, 0, 0, 0);
        Rect wordRect = new Rect(0, 0, 0, 0);

        //First loop finds the phrase"Net Pay"
        firstLoop:
        //For each text block
        for (int i = 0; i < textBlocks.size(); i++) {
            paragraph = textBlocks.get(textBlocks.keyAt(i));
            lineList = paragraph.getComponents();
            //For each line in a text block
            for (int j = 0; j < lineList.size(); j++) {
                wordList = lineList.get(j).getComponents();
                //For each word in a line
                if (lineList.get(j).getValue().equalsIgnoreCase("net pay") ||
                        lineList.get(j).getValue().equalsIgnoreCase("net pay:") ||
                        lineList.get(j).getValue().equalsIgnoreCase("net pay :")) {
                    lineRect = lineList.get(j).getBoundingBox();
                    break firstLoop;
                }
            }
        }

        //Second loop finds the value corresponding to the bounding box of the phrase "Net Pay"
        secondLoop:
        //For each text block
        for (int i = 0; i < textBlocks.size(); i++) {
            paragraph = textBlocks.get(textBlocks.keyAt(i));
            lineList = paragraph.getComponents();
            //For each line in a text block
            for (int j = 0; j < lineList.size(); j++) {
                wordList = lineList.get(j).getComponents();
                //For each word in a line
                for (int k = 0; k < wordList.size(); k++) {
                    if (wordList.get(k).getBoundingBox().top > lineRect.top - 45 &&
                            wordList.get(k).getBoundingBox().top < lineRect.top + 45 &&
                            !lineList.get(j).getBoundingBox().equals(lineRect) &&
                            wordList.get(k).getBoundingBox().left > lineRect.left) {
                        if (k + 1 < wordList.size()) {
                            if (wordList.get(k + 1).getValue().equals("$") ||
                                    wordList.get(k + 1).getValue().equals("s")) {
                                netPay = wordList.get(k + 2).getValue();
                            } else
                                netPay = wordList.get(k + 1).getValue();
                        } else
                            netPay = wordList.get(k).getValue();
                        break secondLoop;
                    }
                }
            }
        }
        //Error Checking
        if (netPay.equals("")) {
            Log.d("myTotal", "blank");
            netPay = "0.00";
        }
        if (netPay.contains("$") || netPay.contains("s") || netPay.contains("S") || netPay.contains("g")) {
            Log.d("myTotal", netPay);
            netPay = netPay.substring(1);
        }

        try {
            Double.parseDouble(netPay);
        } catch (Exception e) {
            netPay = "0.00";
        }
        return netPay;
    }
}

