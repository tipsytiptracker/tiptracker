package com.example.ronjc.tiptracker;

import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.example.ronjc.tiptracker.utils.FontManager;

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

    }
}
