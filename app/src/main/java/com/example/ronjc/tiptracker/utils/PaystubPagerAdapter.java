package com.example.ronjc.tiptracker.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.ronjc.tiptracker.PayStubsActivity;
import com.example.ronjc.tiptracker.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by geneh on 5/1/2017.
 */

public class PaystubPagerAdapter extends BaseAdapter {

    private Context context;
    ArrayList<String> uri = new ArrayList<>();

    DatabaseReference dbRef;
    FirebaseAuth mAuth;
    FirebaseUser user;


    public ArrayList<String> getImages(){

        dbRef.child(DBHelper.USERS).child(user.getUid()).child("paystubs").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {int x=0;
                for(DataSnapshot child: dataSnapshot.getChildren()){
                    if(!child.child("url").getValue().toString().equals("")){
                        uri.add(child.child("url").getValue().toString());

                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return uri;
    }
    public PaystubPagerAdapter(Context context, DatabaseReference dbRef1, FirebaseAuth mAuth1, FirebaseUser user1){
        this.context=context;
        this.dbRef = dbRef1;
        this.mAuth = mAuth1;
        this.user = user1;
    }

    @Override
    public int getCount() {
        return getImages().size();

    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        getImages();
        ImageView image = (ImageView)view.findViewById(R.id.gallery_items);

        for(int x = 0; x<uri.size();x++){
            Picasso.with(context).load("http://www.thekrausemouse.com/wp-content/uploads/2016/03/Sample.jpg").into(image);
        }


        return image;
    }
}