package com.example.ronjc.tiptracker;

import android.graphics.Typeface;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PayStubsActivity extends AppCompatActivity {

    @BindView(R.id.paystubs_tv)
    TextView mPaystubTextView;
    @BindView(R.id.paystubs_add_paystub_button)
    Button mPaystubButton;
    @BindView(R.id.view_paystubs)
    Button mViewPaystubButton;
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
                            long dateAdded = System.currentTimeMillis(); //gets the milliseconds of the current time
                            Toast.makeText(getApplicationContext(), "Your Paystub was added!", Toast.LENGTH_SHORT).show();

//                            myRef = FirebaseDatabase.getInstance().getReference().child("users")
//                                    .child(user.getUid()).child("Paystubs");
                            PayStub payStub = new PayStub(value, descrip, user.getUid(),dateAdded);
                            myRef.child("users").child(user.getUid()).child("paystubs").push().setValue(payStub);
//                            Toast.makeText(getApplicationContext(), "" + user.getUid(), Toast.LENGTH_LONG).show();
//                            myRef.setValue(new PayStub(value,descrip));

                            dialog.dismiss();
                        }//end else
                    }
                });
                dialog.show();
            }
        }); //end of add manual paystub button

        mViewPaystubButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder mBuilder2 = new AlertDialog.Builder(PayStubsActivity.this);
                View mView2 = getLayoutInflater().inflate(R.layout.fragment_view_all_paystubs, null);

                final TextView amount_tv = (TextView)mView2.findViewById(R.id.paystubs_am);
                final TextView date_tv = (TextView)mView2.findViewById(R.id.paystubs_date);

                Typeface iconFont3 = FontManager.getTypeface(getApplicationContext(), FontManager.BITTER);
                FontManager.markAsIconContainer(mView2.findViewById(R.id.paystubs_am), iconFont3);
                FontManager.markAsIconContainer(mView2.findViewById(R.id.paystubs_title), iconFont3);
                FontManager.markAsIconContainer(mView2.findViewById(R.id.paystubs_date), iconFont3);

                mBuilder2.setView(mView2);
                final AlertDialog dialog = mBuilder2.create();

                myRef.child("users").child(user.getUid()).child("paystubs").orderByChild("datePosted").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //gets all of the paystubs of the user
                        String amount_tit = "Amount\n\n";
                        String date_tit = "Date Added\n\n";

                        ArrayList<String> list1 = new ArrayList<String>();
                        ArrayList<String> list3 = new ArrayList<String>();

                        for (DataSnapshot child: dataSnapshot.getChildren()) {

                            PayStub users = child.getValue(PayStub.class);

                            String get_amount = "$" + users.getAmount() + "\n";

                            long get_date = users.getDatePosted();

                            DateFormat df = new SimpleDateFormat("MM/dd/yy");
                            String gd = df.format(get_date) + "\n";

                            list1.add(get_amount);
                            list3.add(gd);

                        }

                        Collections.reverse(list1); //reverses arraylist by date so it prints latest first
                        Collections.reverse(list3);

                        String amounts = TextUtils.join("", list1); //transfers arraylist string
                        String dates = TextUtils.join("", list3);

                        amount_tv.setText(amount_tit + amounts);
                        date_tv.setText(date_tit + dates);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                dialog.show();
            }
        }); //end of the view all paystubs buttons


    }
}
