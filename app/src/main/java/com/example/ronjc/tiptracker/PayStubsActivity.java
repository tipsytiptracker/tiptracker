package com.example.ronjc.tiptracker;

import android.graphics.Typeface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ronjc.tiptracker.model.PayStub;
import com.example.ronjc.tiptracker.utils.FontManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PayStubsActivity extends AppCompatActivity {

    @BindView(R.id.paystubs_tv)
    TextView mPaystubTextView;
    @BindView(R.id.paystubs_add_paystub_button)
    Button mPaystubButton;
    @BindView(R.id.paystubs_picture_tile_text)
    TextView mTileText;
    @BindView(R.id.paystubs_picture_tile)
    TextView mPictureTile;

    DatabaseReference myRef;
    FirebaseAuth firebaseAuth;
    FirebaseAuth.AuthStateListener mAuthListener;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_stubs);

        ButterKnife.bind(this);

        firebaseAuth = FirebaseAuth.getInstance();
        myRef = FirebaseDatabase.getInstance().getReference();
        user  = firebaseAuth.getCurrentUser();

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.tiptrackerlogo3);
        getSupportActionBar().setTitle("");

        Typeface iconFont = FontManager.getTypeface(getApplicationContext(), FontManager.BITTER);
        FontManager.markAsIconContainer(findViewById(R.id.paystubs_activity), iconFont);

        Typeface typeface = Typeface.createFromAsset(getAssets(), FontManager.FONTAWESOME);
        mPictureTile.setTypeface(typeface);





        mPaystubButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder mBuilder = new AlertDialog.Builder(PayStubsActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.add_manual_paystub, null);

                Typeface iconFont2 = FontManager.getTypeface(getApplicationContext(), FontManager.BITTER);
                FontManager.markAsIconContainer(mView.findViewById(R.id.paystub_amount), iconFont2);
                FontManager.markAsIconContainer(mView.findViewById(R.id.paystub_desc), iconFont2);
                FontManager.markAsIconContainer(mView.findViewById(R.id.paystub_title), iconFont2);

                final EditText amount = (EditText)mView.findViewById(R.id.paystub_amount);
                final EditText desc = (EditText)mView.findViewById(R.id.paystub_desc);
                final Button submit = (Button)mView.findViewById(R.id.paystub_submit);
                final CheckBox addIncome = (CheckBox) mView.findViewById(R.id.paystub_addtoIncome);

                mBuilder.setView(mView);
                final AlertDialog dialog = mBuilder.create();

                submit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(amount.getText().toString().trim().length() == 0 || desc.getText().toString().trim().length() == 0){
                            //checks if the 2 fields do not have any white space
                            amount.setError("You must enter a value!");
                            desc.setError("This Field must not be empty");
                        }

                        else{
                            //excludes the dollar sign from the string
                            final double value = Double.parseDouble(amount.getText().toString().substring(1));
                            final String descrip = desc.getText().toString();
                            Toast.makeText(getApplicationContext(), "Your Paystub was added!", Toast.LENGTH_SHORT).show();

//                            myRef = FirebaseDatabase.getInstance().getReference().child("users")
//                                    .child(user.getUid()).child("Paystubs");
                            PayStub payStub = new PayStub(value, descrip, user.getUid());
                            myRef.child("users").child(user.getUid()).child("paystubs").push().setValue(payStub);
//                            Toast.makeText(getApplicationContext(), "" + user.getUid(), Toast.LENGTH_LONG).show();
//                            myRef.setValue(new PayStub(value,descrip));

                            dialog.dismiss();
                        }//end else
                    }
                });


                dialog.show();
            }
        });
    }
}
