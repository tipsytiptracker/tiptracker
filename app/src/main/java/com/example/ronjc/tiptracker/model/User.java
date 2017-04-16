package com.example.ronjc.tiptracker.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by ronjc on 4/16/2017.
 */

public class User {
    private String email;
    private List<PayStub> payStubs;
    private List<Period> periods;
    private List<Income> incomes;
    private List<Expense> expenses;
    private List<Income> repeatedIncomes;
    private List<Expense> repeatedExpenses;
    private double currentBudget;

    public User() {}

    public User(String email, List<PayStub> payStubs, List<Period> periods, List<Income> incomes,
                List<Expense> expenses, List<Income> repeatedIncomes, List<Expense> repeatedExpenses,
                double currentBudget) {

        this.email = email;
        this.payStubs = payStubs;
        this.periods = periods;
        this.incomes = incomes;
        this.expenses = expenses;
        this.repeatedIncomes = repeatedIncomes;
        this.repeatedExpenses = repeatedExpenses;

        /*
         * For better precision, use BigDecimal to take in double value
         */
        BigDecimal bigDecimal = new BigDecimal(currentBudget);
        bigDecimal = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP);
        currentBudget = bigDecimal.doubleValue();
        this.currentBudget = currentBudget;
    }


    public String getEmail() {
        return email;
    }

    public List<PayStub> getPayStubs() {
        return payStubs;
    }

    public List<Period> getPeriods() {
        return periods;
    }

    public List<Income> getIncomes() {
        return incomes;
    }

    public List<Expense> getExpenses() {
        return expenses;
    }

    public List<Income> getRepeatedIncomes() {
        return repeatedIncomes;
    }

    public List<Expense> getRepeatedExpenses() {
        return repeatedExpenses;
    }

    public double getCurrentBudget() {
        return currentBudget;
    }
}
