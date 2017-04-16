package com.example.ronjc.tiptracker.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by ronjc on 4/15/2017.
 */

public class Income implements Serializable{
    private String id;
    private String name;
    private BigDecimal amount;
    private Date date;
    private String category;

    public Income() {}

    public Income(String id, String name, BigDecimal amount, Date date, String category) {
        this.id = id;
        this.name = name;
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

    public BigDecimal getAmount() {
        return amount;
    }

    public Date getDate() {
        return date;
    }

    public String getCategory() {
        return category;
    }
}
