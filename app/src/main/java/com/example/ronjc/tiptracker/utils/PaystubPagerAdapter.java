package com.example.ronjc.tiptracker.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

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
    DatabaseReference dbRef;
    FirebaseAuth mAuth;
    FirebaseUser user;
    ArrayList<String> uri = new ArrayList<>();

    public ArrayList<String> getImages(){
        dbRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        dbRef.child(DBHelper.USERS).child(user.getUid()).child("paystubs").child("url").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot child: dataSnapshot.getChildren()){
                    uri.add(child.getValue().toString());
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return uri;
    }
    public PaystubPagerAdapter(Context context){
        this.context=context;
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
        final ImageView image = new ImageView(context);
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                for(int x = 0; x<uri.size();x++){
                    image.setImageURI(Uri.parse(uri.get(x)));
                }

            }
        };
        handler.postDelayed(runnable,1000);
        return image;
    }
}