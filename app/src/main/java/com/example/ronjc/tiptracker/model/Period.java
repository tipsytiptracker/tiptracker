package com.example.ronjc.tiptracker.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Period data model
 *
 * @author Ronald Mangiliman
 * Created on 4/15/2017.
 */

public class Period {
    private long startDate;
    private long endDate;
    private ArrayList<Income> incomes;
    private ArrayList<Expense> expenses;
    private double budgetGoal;
    private double totalIncome;
    private double totalExpenses;
    private List<String> categories;

    public Period() {}

    public Period(long startDate, long endDate, ArrayList<Income> incomes, ArrayList<Expense> expenses, double budgetGoal, double totalIncome, double totalExpenses) {
        this.startDate = startDate;
        this.endDate =  endDate;
        this.incomes = incomes;
        this.expenses = expenses;

        BigDecimal bigDecimal = new BigDecimal(budgetGoal);
        bigDecimal = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP);
        budgetGoal = bigDecimal.doubleValue();
        this.budgetGoal = budgetGoal;
        bigDecimal = new BigDecimal(totalIncome);
        bigDecimal = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP);
        totalIncome = bigDecimal.doubleValue();
        this.totalIncome = totalIncome;
        bigDecimal = new BigDecimal(totalExpenses);
        bigDecimal = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP);
        totalExpenses = bigDecimal.doubleValue();
        this.totalExpenses = totalExpenses;
    }

    public long getStartDate() {
        return startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public ArrayList<Income> getIncomes() {
        return incomes;
    }

    public ArrayList<Expense> getExpenses() {
        return expenses;
    }

    public double getBudgetGoal() {
        return budgetGoal;
    }

    public double getTotalIncome() {
        return totalIncome;
    }

    public double getTotalExpenses() {
        return totalExpenses;
    }

}
