package com.example.ronjc.tiptracker.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by ronjc on 4/15/2017.
 */

public class Expense implements Serializable{
    private String id;
    private String name;
    private double amount;
    private long date;
    private String category;

    public Expense() {}

    public Expense(String id, String name, double amount, long date, String category) {
        this.id = id;
        this.name = name;
        BigDecimal bigDecimal = new BigDecimal(amount);
        bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP);
        amount = bigDecimal.doubleValue();
        this.amount = amount;
        this.date = date;
        this.category = category;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getAmount() {
        return amount;
    }

    public long getDate() {
        return date;
    }

    public String getCategory() {
        return category;
    }
}
