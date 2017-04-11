package com.example.ronjc.tiptracker;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.example.ronjc.tiptracker.utils.FontManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeActivity extends AppCompatActivity {

    private RelativeLayout mLogoutTile, mStubTile, mBudgetGoalTile, mManageBudgetTile;
    private FirebaseAuth mAuth;
    private GoogleApiClient mGoogleApiClient;

    @BindView(R.id.home_activity)
    RelativeLayout mRelativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Typeface iconFont = FontManager.getTypeface(getApplicationContext(), FontManager.FONTAWESOME);
        FontManager.markAsIconContainer(findViewById(R.id.metro_tiles), iconFont);

        ButterKnife.bind(this);

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
        mStubTile.setOnClickListener(mTileListener);
        mBudgetGoalTile.setOnClickListener(mTileListener);
        mManageBudgetTile.setOnClickListener(mTileListener);
        mLogoutTile.setOnClickListener(mTileListener);

    }

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
    }

    class TileListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.metro_tile_1: {
                    mStubTile.requestFocus();
                    break;
                }
                case R.id.metro_tile_2: {
                    mBudgetGoalTile.requestFocus();
                    break;
                }
                case R.id.metro_tile_3: {
                    mManageBudgetTile.requestFocus();
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

    private class ConnectionFailed implements GoogleApiClient.OnConnectionFailedListener {
        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            Snackbar.make(mRelativeLayout, getString(R.string.connection_failed), Snackbar.LENGTH_LONG);
        }
    }


}
