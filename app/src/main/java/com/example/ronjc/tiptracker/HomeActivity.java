package com.example.ronjc.tiptracker;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.icu.text.NumberFormat;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ronjc.tiptracker.model.Expense;
import com.example.ronjc.tiptracker.model.Income;
import com.example.ronjc.tiptracker.model.PayStub;
import com.example.ronjc.tiptracker.model.Period;
import com.example.ronjc.tiptracker.model.User;
import com.example.ronjc.tiptracker.utils.DBHelper;
import com.example.ronjc.tiptracker.utils.DateManager;
import com.example.ronjc.tiptracker.utils.FontManager;
import com.example.ronjc.tiptracker.utils.Utils;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Controller for HomeActvity
 *
 * @author Ronald Mangiliman
 */
public class HomeActivity extends AppCompatActivity {

    //Bind UI elements to references
    @BindView(R.id.mtile_text1)
    TextView mTileText1;
    @BindView(R.id.mtile_text2)
    TextView mTileText2;
    @BindView(R.id.mtile_text3)
    TextView mTileText3;
    @BindView(R.id.mtile_text4)
    TextView mTileText4;
    @BindView(R.id.current_budget)
    TextView mCurrentBudget;

    @BindView(R.id.current_budget_val)
    TextView mCurrentBudgetVal;

    @BindView(R.id.home_activity)
    RelativeLayout mRelativeLayout;

    //TODO: Bind these with ButterKnife
    private RelativeLayout mLogoutTile, mStubTile, mBudgetGoalTile, mManageBudgetTile;

    //Firebase and Google Sign In references
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabaseReference;
    private GoogleApiClient mGoogleApiClient;

    private double totalbudget = 0.00;
    private String periodID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ButterKnife.bind(this);
        //Create Firebase Auth State listener
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                mFirebaseUser = firebaseAuth.getCurrentUser();
                if(mFirebaseUser == null) {
                    //If user is not logged in, then redirect to MainActivity
                    Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                    startActivity(intent);
                    HomeActivity.this.finish();
                } else {
                    //If user does not exist in database, create element for that user
                    mDatabaseReference = FirebaseDatabase.getInstance().getReference();
                    mDatabaseReference.child("users").child(mFirebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(!dataSnapshot.exists()) {
                                User user = new User(mFirebaseUser.getEmail(), new ArrayList<PayStub>(), new ArrayList<Period>(),
                                        new ArrayList<Income>(), new ArrayList<Expense>(), new ArrayList<Income>(), new ArrayList<Expense>(),
                                        0.00);
                                mDatabaseReference.child("users").child(mFirebaseUser.getUid()).setValue(user.toMap());
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        };

        //Add logo to ActionBar
        //TODO: this can go in utils most likely. It is used throughout the app
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.tiptrackerlogo3);
        getSupportActionBar().setTitle("");

        //Set font styling
        Typeface iconFont = FontManager.getTypeface(getApplicationContext(), FontManager.FONTAWESOME);
        FontManager.markAsIconContainer(findViewById(R.id.metro_tiles), iconFont);
        Typeface typeface = Typeface.createFromAsset(getAssets(), FontManager.BITTER);
        mTileText1.setTypeface(typeface);
        mTileText2.setTypeface(typeface);
        mTileText3.setTypeface(typeface);
        mTileText4.setTypeface(typeface);
        mCurrentBudgetVal.setTypeface(typeface);
        mCurrentBudget.setTypeface(typeface);

        ButterKnife.bind(this);

        //Create google sign in
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new ConnectionFailed())
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mAuth = FirebaseAuth.getInstance();
        mStubTile = (RelativeLayout) findViewById(R.id.metro_tile_1);
        mBudgetGoalTile = (RelativeLayout) findViewById(R.id.metro_tile_2);
        mManageBudgetTile = (RelativeLayout) findViewById(R.id.metro_tile_3);
        mLogoutTile = (RelativeLayout) findViewById(R.id.metro_tile_4);
        TileListener mTileListener = new TileListener();

        //Set on click listeners for home buttons
        mStubTile.setOnClickListener(mTileListener);
        mBudgetGoalTile.setOnClickListener(mTileListener);
        mManageBudgetTile.setOnClickListener(mTileListener);
        mLogoutTile.setOnClickListener(mTileListener);

    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        totalbudget = 0.00;
        getPeriodId();
    }

    /**
     * Logs user out of account
     */
    private void logout() {
        mAuth.signOut();
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                    }
                });
        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        startActivity(intent);
        HomeActivity.this.finish();
    }


    /**
     * gets the incomes of user for that week
     */
    private void getTotalIncomes(){
        final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();

        myRef.child("periods").child(periodID).child("totalIncome").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){
                    totalbudget += Double.parseDouble(dataSnapshot.getValue().toString());
                }

                else{//creates a totalIncome key if it doesn't exist in db
                    myRef.child("periods").child(periodID).child("totalIncome").setValue(0);
                    totalbudget += 0;
                }
                getTotalExpenses();


            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void checkPos(){ //checks if the budget is positive or negative
        if(totalbudget >= 0) {
            DecimalFormat f = new DecimalFormat("0.00");
            mCurrentBudgetVal.setText("+ $" + f.format(totalbudget) + " :)");
        }

        else{
            DecimalFormat f = new DecimalFormat("0.00");
            mCurrentBudgetVal.setText("- $" + f.format(totalbudget).replace("-","") + " :(");
        }
    }

    /**
     * gets expenses of user for that week
     */
    private void getTotalExpenses(){
        final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();

        myRef.child("periods").child(periodID).child("totalExpenses").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    totalbudget -= Double.parseDouble(dataSnapshot.getValue().toString());
                }

                else{
                    myRef.child("periods").child(periodID).child("totalExpenses").setValue(0);
                    totalbudget -= 0;
                }
                getBudgetGoal();

            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * gets budget goal of user for that week
     */
    private void getBudgetGoal(){
        final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        final FirebaseUser user = auth.getCurrentUser();

        myRef.child("users").child(user.getUid()).child("current_budget").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    totalbudget -= Double.parseDouble(dataSnapshot.getValue().toString());
                }

                else{ //if budget goal does not exist
                    myRef.child("users").child(user.getUid()).child("current_budget").setValue(0);
                    totalbudget -= 0;
                }
                checkPos();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void getPeriodId(){
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        myRef.child(DBHelper.USERS).child(user.getUid()).child(DBHelper.PERIODS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Get key for period to write
                Iterable<DataSnapshot> periods = dataSnapshot.getChildren();
                //Get last Sunday in milliseconds
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                Date startDate = calendar.getTime();
                final long time = DateManager.trimMilliseconds(startDate.getTime());

                for (DataSnapshot period : periods) {
                    if ((long) period.getValue() == time) {
                        periodID = period.getKey();
                        break;
                    }
                }
                getTotalIncomes();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



    /**
     * Tile listener sub class
     *
     * OnClick listener for buttons on Home Activity.
     * Will start activities for their respective button
     * Logout button calls logout method
     */
    private class TileListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.metro_tile_1: {
                    mStubTile.requestFocus();
                    Intent intent = new Intent(HomeActivity.this, PayStubsActivity.class);
                    startActivity(intent);
                    break;
                }
                case R.id.metro_tile_2: {
                    mBudgetGoalTile.requestFocus();
                    Intent intent = new Intent(HomeActivity.this, BudgetManagement.class);
                    startActivity(intent);
                    break;
                }
                case R.id.metro_tile_3: {
                    mManageBudgetTile.requestFocus();
                    Intent intent = new Intent(HomeActivity.this,BudgetGoalActivity.class);
                    startActivity(intent);
                    break;
                }
                case R.id.metro_tile_4: {
                    mLogoutTile.requestFocus();
                    logout();
                    break;
                }
            }
        }
    }

    /**
     * Required sub class that implements OnConnectionFailedListener.
     * Used when creating GoogleApiClient
     *
     * Simply displays a Snackbar if connection fails.
     */
    private class ConnectionFailed implements GoogleApiClient.OnConnectionFailedListener {
        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            Snackbar.make(mRelativeLayout, getString(R.string.connection_failed), Snackbar.LENGTH_LONG);
        }
    }
}
