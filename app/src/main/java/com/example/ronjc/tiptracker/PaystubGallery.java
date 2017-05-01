package com.example.ronjc.tiptracker;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Gallery;
import android.widget.GridView;

import com.example.ronjc.tiptracker.utils.PaystubPagerAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PaystubGallery extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paystub_gallery);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.tiptrackerlogo3);
        getSupportActionBar().setTitle("");
        ButterKnife.bind(this);

        GridView gridView = (GridView)findViewById(R.id.gridView);
        gridView.setAdapter(new PaystubPagerAdapter(this));


    }
}
