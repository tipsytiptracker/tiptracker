package com.example.ronjc.tiptracker;

import android.graphics.Typeface;
import android.icu.text.DecimalFormat;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ronjc.tiptracker.Model.PayStubFb;
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
    FirebaseUser user;
    FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_stubs);

        ButterKnife.bind(this);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.tiptrackerlogo3);
        getSupportActionBar().setTitle("");

        Typeface iconFont = FontManager.getTypeface(getApplicationContext(), FontManager.BITTER);
        FontManager.markAsIconContainer(findViewById(R.id.paystubs_activity), iconFont);

        Typeface typeface = Typeface.createFromAsset(getAssets(), FontManager.FONTAWESOME);
        mPictureTile.setTypeface(typeface);

        user =FirebaseAuth.getInstance().getCurrentUser();
        myRef = FirebaseDatabase.getInstance().getReference(user.toString());

        mPaystubButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder mBuilder = new AlertDialog.Builder(PayStubsActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.add_manual_paystub, null);

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
                            amount.setError("This Field must not be empty");
                            desc.setError("This Field must not be empty");
                        }

                        else{
                            float val = Float.parseFloat(amount.getText().toString());
                            DecimalFormat f = new DecimalFormat("##.00"); //round 2 decimal places
                            final String value = f.format(val);
                            final String descrip = desc.getText().toString();
                            Toast.makeText(getApplicationContext(), "Your Paystub was added!", Toast.LENGTH_SHORT).show();
                            myRef.push().setValue(new PayStubFb(value,descrip));
                            dialog.dismiss();
                        }
                    }
                });


                dialog.show();
            }
        });
    }
}
