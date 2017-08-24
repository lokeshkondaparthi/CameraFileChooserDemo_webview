package com.dev.camerafilechooserdemo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/*
* This activity loads url and in that when click on upload button, show chooser dialog,
* chooser dialog has 2 things(camera and files) , any of selcted, Uri will need to send
* to servier throgh this line  mValueCal.onReceiveValue(xxxxx);
*   Here choosing camera is tricky, when you caputre photo , it may not saved properly,
*   even you may get wrong URI,to overcome that one XML page in resource directory and in has xml file
*   which is added in your provider tag in Manifest.xml
*
*   Note: Permissions must be granted.
*
*   How to caputre image properly: https://developer.android.com/training/camera/photobasics.html  
* */
public class MainActivity extends AppCompatActivity {


    private TextView tvResult;
    private String mCurrentPhotoPath;
    private WebView mWebview;
    private ValueCallback<Uri[]> mValueCal;
    private int FILECHOOSER_RESULTCODE=13;
    public static final String url = "http://your.site.com";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mWebview = (WebView) findViewById(R.id.mWebview);
        tvResult = (TextView) findViewById(R.id.tv_result);


        WebSettings webSettings = mWebview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(false);
        webSettings.setSupportMultipleWindows(true);

        mWebview.setWebChromeClient(new WebChromeClient(){
            //For Android 5.0+
            public boolean onShowFileChooser(
                    WebView webView, ValueCallback<Uri[]> filePathCallback,
                    FileChooserParams fileChooserParams) {
                try {
                    if (mValueCal != null) {
                        mValueCal.onReceiveValue(null);
                    }
                }catch (Exception e){}

                mValueCal = filePathCallback;

                Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if(takePicture.resolveActivity(getPackageManager()) != null) {
                    File mOutputFile  = null;
                    try {
                        mOutputFile = createImageFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    /* Checking mOutputFile is null*/
                    if (mOutputFile != null) {
                        Uri photoURI = FileProvider.getUriForFile(MainActivity.this,
                                "com.dev.camerafilechooserdemo",
                                mOutputFile);
                        /* If extra MediaStore.EXTRA_OUTPUT is added , in onActivityResult  would
                        * pass null data and writes URI only.*/
                        takePicture.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    } else {
                        mOutputFile = null;
                    }
                }
                Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                contentSelectionIntent.setType("*/*");
                Intent[] intentArray;
                    intentArray = new Intent[]{takePicture};


                Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
                startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);
                return true;
            }
        });
        mWebview.loadUrl(url);
    }

    public void imagePicker(View view) {

    }



    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }



    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
            Toast.makeText(this, "Uri is null", Toast.LENGTH_SHORT).show();
            File f = new File(mCurrentPhotoPath);
            Uri contentUri = Uri.fromFile(f);
            if(contentUri!= null)
            tvResult.setText(contentUri.toString());
        /* If you choose camera , intent data #imageReturnedIntent would definitely be null here*/
            if ( imageReturnedIntent == null)
            mValueCal.onReceiveValue(new Uri[]{contentUri});
            else
                mValueCal.onReceiveValue(new Uri[]{imageReturnedIntent.getData()});
        }
}
