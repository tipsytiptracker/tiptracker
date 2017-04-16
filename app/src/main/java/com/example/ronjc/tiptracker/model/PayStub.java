package com.example.ronjc.tiptracker.model;

/**
 * Created by Arthur on 4/14/2017.
 */

public class PayStub {
    private double amount;
    private String description;
    private String url = "";

    public PayStub(){

    }

    public PayStub(double amount, String description){
        this.amount = amount;
        this.description = description;
    }

    public PayStub(double amount, String description, String url){
        this.amount = amount;
        this.description = description;
        this.url = url;
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
}
