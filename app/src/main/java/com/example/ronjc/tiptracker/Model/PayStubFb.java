package com.example.ronjc.tiptracker.Model;

/**
 * Created by Arthur on 4/14/2017.
 */

public class PayStubFb {
    private String amount;
    private String description;

    public PayStubFb(){

    }

    public PayStubFb(String amount, String description){
        this.amount = amount;
        this.description = description;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
