package com.example.ronjc.tiptracker;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ronjc.tiptracker.utils.Camera;
import com.example.ronjc.tiptracker.utils.OCR;

public class TestActivity extends AppCompatActivity {
    Camera mCam;
    OCR mOCR;
    final int REQUEST_TAKE_PHOTO = 1;
    String path;
    TextView textView;
    ImageView imageView;
    Bitmap bitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        textView = (TextView) findViewById(R.id.textView);
        imageView = (ImageView) findViewById(R.id.imageView);
    }

    public void takePic(View view) {
        mCam = new Camera(this);
        mCam.takePicture();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            bitmap = mCam.getBitmap();
            imageView.setImageBitmap(bitmap);
            mOCR = new OCR(this, bitmap);
            textView.setText(mOCR.getNetPay());
        }
    }

    public void goOCR(View view) {
        mOCR = new OCR(this, bitmap);
        textView.setText(mOCR.getNetPay());
    }
}
