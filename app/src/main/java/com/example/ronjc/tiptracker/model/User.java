package com.example.ronjc.tiptracker.model;

import com.google.firebase.database.Exclude;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * User data model
 *
 * @author Ronald Mangiliman
 * Created on 4/16/2017.
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


    //Accessors
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

    /**
     * Method that returns a map of the user's properties
     *
     * @return Map the map of the user's properties
     */
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("email", email);
        result.put("paystubs", payStubs);
        result.put("periods", periods);
        result.put("incomes", incomes);
        result.put("expenses", expenses);
        result.put("repeated_income", repeatedIncomes);
        result.put("repeated_expenses", repeatedExpenses);
        result.put("current_budget", currentBudget);
        return result;
    }
}
