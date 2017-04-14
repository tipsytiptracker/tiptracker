package com.example.ronjc.tiptracker;

import android.graphics.Typeface;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.ronjc.tiptracker.utils.FontManager;
import com.example.ronjc.tiptracker.utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.internal.Util;

public class BudgetGoalActivity extends AppCompatActivity {

    @BindView(R.id.set_budget_et) EditText setBudget;
    @BindView(R.id.set_goal_tv) TextView goal;
    String changedGoal;
    @BindView(R.id.change_budget_btn) Button budgetBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_goal);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.tiptrackerlogo3);
        getSupportActionBar().setTitle("");
        ButterKnife.bind(this);


        Typeface iconFont = FontManager.getTypeface(getApplicationContext(), FontManager.BITTER);
        FontManager.markAsIconContainer(findViewById(R.id.budget_goal_activity), iconFont);

        String customFont = "fonts/bitter.ttf";
        Typeface typeface = Typeface.createFromAsset(getAssets(), customFont);
        Utils.BUDGET = Integer.parseInt(changedGoal);

        budgetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                changedGoal = setBudget.getText().toString();
                if(changedGoal.matches(".*[0-9].*")){
                    goal.setText("Current Budget: $" + Utils.BUDGET);

                }
                else {
                    goal.setError("Please enter in a valid budget goal");
                }
            }
        });

    }
}
