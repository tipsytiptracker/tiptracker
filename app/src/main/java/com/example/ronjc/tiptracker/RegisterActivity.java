package com.example.ronjc.tiptracker;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;

import android.os.Build;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A registration screen that allows the user to create an account.
 */
public class RegisterActivity extends AppCompatActivity {

    // UI references.
    @BindView(R.id.text_input_layout_register_email)
    TextInputLayout mTextInputLayoutEmail;
    @BindView(R.id.text_input_layout_register_password)
    TextInputLayout mTextInputLayoutPassword;
    @BindView(R.id.text_input_layout_register_confirm_password)
    TextInputLayout mTextInputLayoutConfirmPassword;
    @BindView(R.id.register_email)
    AutoCompleteTextView mEmailView;
    @BindView(R.id.register_password)
    EditText mPasswordView;
    @BindView(R.id.register_confirm_password)
    EditText mPasswordConfirmView;
    @BindView(R.id.register_button)
    Button mRegisterButton;
    ProgressDialog mProgressDialog;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ButterKnife.bind(this);

        String customFont = "fonts/bitter.ttf";
        Typeface typeface = Typeface.createFromAsset(getAssets(), customFont);

        mTextInputLayoutEmail.setTypeface(typeface);
        mTextInputLayoutPassword.setTypeface(typeface);
        mTextInputLayoutConfirmPassword.setTypeface(typeface);

        //Sets the Font of everything else to Bitter
        Typeface iconFont = FontManager.getTypeface(getApplicationContext(), FontManager.BITTER);
        FontManager.markAsIconContainer(findViewById(R.id.register_activity), iconFont);

        //Allows registration via key event
        mPasswordConfirmView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    String email = mEmailView.getText().toString();
                    String password = mPasswordView.getText().toString();
                    String confirmPassword = mPasswordConfirmView.getText().toString();
                    createAccount(email, password, confirmPassword, textView);
                    return true;
                }
                return false;
            }
        });

        mRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mEmailView.getText().toString();
                String password = mPasswordView.getText().toString();
                String confirmPassword = mPasswordConfirmView.getText().toString();
                createAccount(email, password, confirmPassword, view);
            }
        });

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                    startActivity(intent);
                }
            }
        };

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(getString(R.string.creating_account));
        mProgressDialog.setMessage(getString(R.string.please_wait));
        mProgressDialog.setCancelable(false);
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    /**
     * Account that attempts to create user account.
     * Validates input and checks if passwords match, if true, attempts user account creation.
     * Otherwise, sets error is a validation check failed, or Snackbar if Firebase Account creation fails
     *
     * @param email Email entered by user
     * @param password Password entered by user
     * @param confirmPassword Password confirmation entered by user
     * @param view View to pass for Snackbar
     */
    private void createAccount(String email, String password, String confirmPassword, final View view) {
        Validator validator = new Validator();
        boolean cancel = false;
        View focusView = null;

        //Validate Email field
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!validator.validateEmail(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        //Validate Password field
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (!validator.validatePassword(password)) {
            mPasswordView.setError((getString(R.string.error_invalid_password)));
            focusView = mPasswordView;
            cancel = true;
        }

        //Validate Confirm Password field
        if (TextUtils.isEmpty(confirmPassword)) {
            mPasswordConfirmView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (!validator.validatePassword(confirmPassword)) {
            mPasswordConfirmView.setError((getString(R.string.error_invalid_password)));
            focusView = mPasswordView;
            cancel = true;
        }

        //Check to see that password match
        if (!password.equals(confirmPassword)) {
            mPasswordView.setError(getString(R.string.error_passwords_dont_match));
            mPasswordConfirmView.setError(getString(R.string.error_passwords_dont_match));
            cancel = true;
            focusView = mPasswordView;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                showProgress(false);
                                Snackbar.make(view, R.string.register_failed, Snackbar.LENGTH_LONG)
                                        .show();
                            } else {
                                showProgress(false);
                            }
                        }
                    });
        }
    }

    private void showProgress(boolean show) {
        if(show) {
            mProgressDialog.show();
        } else {
            mProgressDialog.dismiss();
        }
    }
}

