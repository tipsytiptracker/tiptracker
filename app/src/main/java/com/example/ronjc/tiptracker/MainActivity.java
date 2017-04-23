package com.example.ronjc.tiptracker;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.ronjc.tiptracker.utils.FontManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Controller for MainActivity
 *
 * @author Ronald Mangiliman
 */
public class MainActivity extends AppCompatActivity {

    //Request code for Google Sign In
    private final int RC_SIGN_IN = 1;

    //Google Sign In and Firebase references
    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private ProgressDialog mProgressDialog;

    //Set references to UI Elements
    @BindView(R.id.ma_email_pass_sign_in_button)
    Button mEmailPasswordSignInButton;
    @BindView(R.id.ma_google_sign_in_button)
    SignInButton mGoogleSignInButton;
    @BindView(R.id.ma_register_button)
    Button mRegisterButton;
    @BindView(R.id.main_activity)
    LinearLayout mLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        //Configure Google sign in
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new ConnectionFailed())
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mFirebaseAuth = FirebaseAuth.getInstance();

        //Create Firebase Auth State listener
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser mFirebaseUser = firebaseAuth.getCurrentUser();
                if(mFirebaseUser != null) {
                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                    startActivity(intent);
                }
            }
        };

        //Create progress dialog
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(getString(R.string.logging_in));
        mProgressDialog.setMessage(getString(R.string.please_wait));
        mProgressDialog.setCancelable(false);


        //Set font styling
        Typeface iconFont = FontManager.getTypeface(getApplicationContext(), FontManager.BITTER);
        FontManager.markAsIconContainer(findViewById(R.id.main_activity), iconFont);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    /**
     * On click method for MainActivity UI buttons
     * @param view
     */
    @OnClick({R.id.ma_email_pass_sign_in_button, R.id.ma_google_sign_in_button, R.id.ma_register_button})
    public void onClick(View view) {
        switch (view.getId()) {
            //Starts activity for custom login
            case R.id.ma_email_pass_sign_in_button: {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                break;
            }
            //Displays google sign in
            case R.id.ma_google_sign_in_button: {
                Intent intent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(intent, RC_SIGN_IN);
                break;
            }
            //Starts activity for registration
            case R.id.ma_register_button: {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    /**
     * Method that handles the sign for google sign in result
     * If sign in is successful, the user is signed into Firebase with their google account
     *
     * @param result google sign in result
     */
    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount account = result.getSignInAccount();
            firebaseAuthWithGoogle(account);
        }
    }

    /**
     * Method that attempts to authenticate with google account
     * @param account google account
     */
    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        showProgress(true);
        AuthCredential mAuthCredential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(mAuthCredential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        showProgress(false);
                        if(!task.isSuccessful()) {
                            Snackbar.make(mLinearLayout, getString(R.string.google_sign_in_failed), Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
    }

    /**
     * Sub class that implements OnConnectionFailedListener.
     *
     * If connection fails, display Snackbar with error message.
     */
    private class ConnectionFailed implements GoogleApiClient.OnConnectionFailedListener {
        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            Snackbar.make(mLinearLayout, getString(R.string.connection_failed), Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * Simple method that shows or dimesses progress dialog
     * @param show show if true, dismiss if false
     */
    private void showProgress(boolean show) {
        if(show) {
            mProgressDialog.show();
        } else {
            mProgressDialog.dismiss();
        }
    }
}
