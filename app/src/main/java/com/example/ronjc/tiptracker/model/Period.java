package com.example.ronjc.tiptracker.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by ronjc on 4/15/2017.
 */

public class Period {
    private Date startDate;
    private Date endDate;
    private ArrayList<Income> incomes;
    private ArrayList<Expense> expenses;
    private BigDecimal budgetGoal;

    public Period() {}

    public Period(Date startDate, Date endDate, ArrayList<Income> incomes, ArrayList<Expense> expenses, BigDecimal budgetGoal) {
        this.startDate = startDate;
        this.endDate =  endDate;
        this.incomes = incomes;
        this.expenses = expenses;
        this.budgetGoal = budgetGoal;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public ArrayList<Income> getIncomes() {
        return incomes;
    }

    public ArrayList<Expense> getExpenses() {
        return expenses;
    }

    public BigDecimal getBudgetGoal() {
        return budgetGoal;
    }
}
