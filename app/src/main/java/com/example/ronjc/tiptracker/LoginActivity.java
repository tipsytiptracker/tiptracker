package com.example.ronjc.tiptracker;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.ronjc.tiptracker.utils.FontManager;
import com.example.ronjc.tiptracker.utils.Validator;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Controller for custom login screen that offers login via email/password.
 *
 * @author Ronald Mangiliman
 */
//Test comments
public class LoginActivity extends AppCompatActivity {

    //Bind UI to references
    @BindView(R.id.text_input_layout_email)
    TextInputLayout mTextInputLayoutEmail;
    @BindView(R.id.text_input_layout_password)
    TextInputLayout mTextInputLayoutPassword;
    @BindView(R.id.login_email)
    AutoCompleteTextView mEmailView;
    @BindView(R.id.login_password)
    EditText mPasswordView;
    @BindView(R.id.login_button)
    Button mEmailSignInButton;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private ProgressDialog mProgressDialog;

    //AdMob Object
    InterstitialAd mInterstitialAd;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);

        //Create Ad object with new ID
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");

        //Create AdListener for when Ad is closed
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                startActivity(intent);
                LoginActivity.this.finish();
            }
        });

        //Load an initial Ad
        requestNewInterstitial();

        //Auth stateListener. Will redirect user to HomeActivity if auth state changes
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    if (mInterstitialAd.isLoaded()) {
                        mInterstitialAd.show();
                    } else {
                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                        startActivity(intent);
                        LoginActivity.this.finish();
                    }
                }
            }
        };
        // Allows login via key event
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    String email = mEmailView.getText().toString();
                    String password = mPasswordView.getText().toString();
                    login(email, password);
                    return true;
                }
                return false;
            }
        });

        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mEmailView.getText().toString();
                String password = mPasswordView.getText().toString();
                login(email, password);
            }
        });
        //Sets the Font of floating text to Bitter
        Typeface typeface = Typeface.createFromAsset(getAssets(), FontManager.BITTER);
        mTextInputLayoutEmail.setTypeface(typeface);
        mTextInputLayoutPassword.setTypeface(typeface);
        //Sets the Font of everything else to Bitter
        Typeface iconFont = FontManager.getTypeface(getApplicationContext(), FontManager.BITTER);
        FontManager.markAsIconContainer(findViewById(R.id.login_activity), iconFont);

        /*
            Create progress dialog
            http://stackoverflow.com/questions/12841803/best-way-to-show-a-loading-progress-indicator
         */
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(getString(R.string.logging_in));
        mProgressDialog.setMessage(getString(R.string.please_wait));
        mProgressDialog.setCancelable(false);
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

    /**
     * Method that attempts to log user in with account they had previously made through application.
     * Validates user input, if all checks are passed, logs user in.
     * Otherwise, displays Snackbar error message.
     * @param email email user enters for attempted login
     * @param password password user enters for attempted login
     */
    private void login(String email, String password) {
        Validator validator = new Validator();

        boolean cancel = false;
        View focusView = null;

        //Input validation
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!validator.validateEmail(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (!validator.validatePassword(password)) {
            mPasswordView.setError((getString(R.string.error_invalid_password)));
            focusView = mPasswordView;
            cancel = true;
        }

        //If any input validation fails, request focus on last view that failed validation
        if (cancel) {
            focusView.requestFocus();
        } else {
            //Else, show progess dialog
            showProgress(true);
            //Attempt sign in
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                showProgress(false);
                                Snackbar.make(mEmailView, R.string.login_failed, Snackbar.LENGTH_LONG)
                                        .show();
                            } else {
                                showProgress(false);
                            }
                        }
                    });
        }
    }

    /**
     * Simple method to show or hide dialog
     * @param show boolean that determines whether to show or hide dialog
     */
    private void showProgress(boolean show) {
        if(show) {
            mProgressDialog.show();
        } else {
            mProgressDialog.dismiss();
        }
    }

    /**
     * Method for loading a new Ad to be displayed
     */
    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("25F3940193EFFA565BAD3A399BC0776D")
                .build();

        mInterstitialAd.loadAd(adRequest);
    }
}

