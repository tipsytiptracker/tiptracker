package com.example.ronjc.tiptracker.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.R.attr.angle;
import static android.R.attr.bitmap;

/**
 * Created by Alex on 4/10/2017.
 * Camera Utility for creating intents to take pictures.
 * Create an object by calling the constructor and passing
 * the context.
 *
 * Call the takePicture() method to start a camera intent.
 * Getter methods getUri and getPath can return the Uri
 * or file path for the most recently created intent.
 *
 * In the calling activity create an onActivityResult() method
 * to get the Uri after the intent is finished, like so:
 *
 * @Override
 * protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 * super.onActivityResult(requestCode, resultCode, data);
 * if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
 *       Uri uri = cam.getUri();
 *       //do whatever you want with uri
 *   }
 * }
 *
 */

public class Camera implements Parcelable{

    private String mCurrentPhotoPath;//holds a file path for the photo
    private Uri photoUri;//holds the uri for the photo
    private Context context;//activity context
    private Bitmap bitmap;
    public static final int REQUEST_TAKE_PHOTO = 1;
    private String header;
    private String type;
    String imageFileName;

    //Constructor, pass activity context
    public Camera (Context context){
        this.context = context;
    }

    /**
     * Name: createImageFile
     * Purpose: Creates a temporary file with a unique name
     * @return temporary file in storage directory
     * @throws IOException
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    /**
     * Name: getImageFilename
     * Purpose: return the filename for the image
     * @return imageFilename
     */
    public String getImageFileName() {
        return imageFileName;
    }

    /**
     * Name: getUri
     * Purpose: return the URI for the photo that was taken
     * @return Uri photoUri
     */
    public Uri getUri (){
        return photoUri;
    }

    /**
     * Name: getPath
     * Purpose: return the file path for the photo taken
     * @return String holding file path
     */
    public String getPath (){
        return mCurrentPhotoPath;
    }

    /**
     * Name: getBitmap
     * Purpose: create a bitmap for the current Camera object
     * @return Bitmap from the photo taken
     */
    public Bitmap getBitmap () {
        bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
        fixOrientation();
        return bitmap;
    }

    /**
     * Name: takePicture
     * Purpose:  start an image capture intent.  Stores the image in
     * a unique file created by createImageFile() function.
     */
    public void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File, write code here
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(context,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                ((Activity)context).startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    /**
     * Name: fixOrientation
     * Purpose:  Determine what angle the picture was rotated when stored,
     * then create a bitmap that contains the correct orientation of the picture.
     */
    private void fixOrientation () {
        ExifInterface ei = null;
        Matrix matrix;
        try {
            ei = new ExifInterface(mCurrentPhotoPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        switch(orientation) {

            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix = new Matrix();
                matrix.postRotate(90);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),
                        matrix, true);
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix = new Matrix();
                matrix.postRotate(180);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),
                        matrix, true);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix = new Matrix();
                matrix.postRotate(270);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),
                        matrix, true);
                break;
        }
    }

    public Camera(Parcel in) {
        String[] data = new String[1];
        in.readStringArray(data);
        this.mCurrentPhotoPath = data[0];
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeStringArray(new String[] {this.mCurrentPhotoPath});
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Camera createFromParcel(Parcel in) {
            return new Camera(in);
        }

        public Camera[] newArray(int size) {
            return new Camera[size];
        }
    };

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
