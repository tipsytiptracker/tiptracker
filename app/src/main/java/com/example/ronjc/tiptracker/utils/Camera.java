package com.example.ronjc.tiptracker.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Alex on 4/11/2017.
 */

public class Camera {

    private String mCurrentPhotoPath;//holds a file path for the photo
    private Uri photoUri;//holds the uri for the photo
    private Context context;//activity context
    private static final int REQUEST_TAKE_PHOTO = 1;

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
        String imageFileName = "JPEG_" + timeStamp + "_";
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
}
