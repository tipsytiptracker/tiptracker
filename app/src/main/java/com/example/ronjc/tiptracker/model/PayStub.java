package com.example.ronjc.tiptracker.model;

/**
 * Created by Arthur on 4/14/2017.
 */

public class PayStub {
    private double amount;
    private String description;
    private String url = "";
    private String userID = "";
    private long datePosted;

    public PayStub(){

    }

    public PayStub(double amount, String description, String userID, long datePosted){
        this.amount = amount;
        this.description = description;
        this.userID = userID;
        this.datePosted = datePosted;
    }

    public PayStub(double amount, String description, String userID, long datePosted, String url){
        this.amount = amount;
        this.description = description;
        this.datePosted = datePosted;
        this.url = url;
        this.userID = userID;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getDatePosted() {
        return datePosted;
    }

    public void setDatePosted(long datePosted) {
        this.datePosted = datePosted;
    }

    public String getUserID() {
        return userID;
    }
}
