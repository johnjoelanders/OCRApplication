package com.example.john.ocrapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.googlecode.tesseract.android.TessBaseAPI;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class MainActivity extends AppCompatActivity{

    ImageView imageView;
    Button btnCamera, btnGallery, btnConvert;
    private static final String TAG = MainActivity.class.getSimpleName();
    Uri outputFileUri;
    private static final String lang = "eng";
    String extractedText;
    Bitmap imageBitmap;
    private TessBaseAPI tessBaseApi;
    TextView textView;
    static final int CAM_REQUEST = 1;
    static final int GALLERY_REQUEST = 2;
    String IMGS_PATH = Environment.getExternalStorageDirectory().toString() + "/TesseractSample/imgs";
    String img_path = IMGS_PATH + "/ocr.jpg";
    private static final String DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/TesseractSample/";
    private static final String TESSDATA = "tessdata";
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView) findViewById(R.id.imageView);
        btnCamera = (Button) findViewById(R.id.btnOpenCamera);
        btnGallery = (Button) findViewById(R.id.btnGallery);
        btnConvert = (Button) findViewById(R.id.btnConvert);
        textView = (TextView) findViewById(R.id.textResult);

        /*if (!OpenCVLoader.initDebug()) {
            Log.e(TAG,"Failed to start open cv ");
        }*/

        if (btnCamera != null) {
            btnCamera.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File file = getFile();
                    camera_intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                    startActivityForResult(camera_intent, CAM_REQUEST);
                }
            });
        }

        if (btnGallery != null) {
            btnGallery.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent photoFromGallery = new Intent(Intent.ACTION_PICK);

                    File picturePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                    String picturePathAsString = picturePath.getPath();
                    Uri data = Uri.parse(picturePathAsString);

                    photoFromGallery.setDataAndType(data, "image/*");
                    startActivityForResult(photoFromGallery, GALLERY_REQUEST);
                }
            });
        }
        if (btnConvert != null) {
            btnConvert.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    try{
                        doOCR();
                    }catch (Exception e){
                        Log.e(TAG,"This is the exception we are failing "+e.toString());
                        e.printStackTrace();
                    }
                }
            });
        }
    }
    public void onActivityResult(int requestCode, int resultCode,Intent data) {
        switch(requestCode){
            case GALLERY_REQUEST:
                try {
                    Uri imageUri = data.getData();
                    InputStream in = getContentResolver().openInputStream(imageUri);
                    OutputStream out = new FileOutputStream(img_path);

                    byte[] buf = new byte[1024];
                    int len;

                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    in.close();
                    out.close();
                    imageBitmap = BitmapFactory.decodeFile(img_path);
                    imageView.setImageBitmap(imageBitmap);

                } catch (IOException e){
                    Log.e(TAG, "ERROR: Cannot open image "+ e );
                }
                break;
            case CAM_REQUEST:
                prepareDirectory(IMGS_PATH);
                outputFileUri = Uri.fromFile(new File(img_path));
                imageBitmap = BitmapFactory.decodeFile(img_path);
                imageView.setImageBitmap(imageBitmap);
                break;
        }
    }
    private File getFile() {
        return new File(img_path);
    }
    private void doOCR() {
        prepareTesseract();
        startOCR(convertColorIntoBlackAndWhiteImage(imageBitmap));
    }

    private void prepareDirectory(String path) {

        File dir = new File(path);
        Log.i(TAG,"This is the path"+path);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.e(TAG, "ERROR: Creation of directory " + path);
            }
        } else {
            Log.i(TAG, "Created directory " + path);
        }
    }


    private void prepareTesseract() {
        try {
            prepareDirectory(Environment.getExternalStorageDirectory().toString() + "/TesseractSample/tessdata");
        } catch (Exception e) {
            e.printStackTrace();
        }

        moveFiles(TESSDATA);
    }

    private void moveFiles(String path) {
        try {
            String fileList[] = getAssets().list(path);

            for (String fileName : fileList) {

                String pathToDataFile = DATA_PATH + path + "/" + fileName;
                if (!(new File(pathToDataFile)).exists()) {

                    InputStream in = getAssets().open(path + "/" + fileName);

                    OutputStream out = new FileOutputStream(pathToDataFile);

                    byte[] buf = new byte[1024];
                    int len;

                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    in.close();
                    out.close();

                    Log.d(TAG, "Copied " + fileName + "to tessdata");
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Unable to copy files to tessdata " + e.toString());
        }
    }

    private String extractTextFromBitmap(Bitmap bitmap) {
        try{
            tessBaseApi = new TessBaseAPI();
        }catch (Exception e){
            Log.e(TAG, e.getMessage());
        }
        tessBaseApi.init(Environment.getExternalStorageDirectory() + "/TesseractSample/",lang);
        tessBaseApi.setImage(bitmap);
        try {
            extractedText = tessBaseApi.getUTF8Text();
        } catch (Exception e) {
            Log.e(TAG, "Error in recognizing text.");
        }
        tessBaseApi.end();
        return extractedText;
    }

    private void startOCR(final Bitmap bitmap) {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog.show(this, "Processing image",
                    "Please wait...", true);
        }
        else {
            mProgressDialog.show();
        }
        new Thread(new Runnable() {
            public void run() {

                final String result = extractTextFromBitmap(bitmap).toLowerCase();

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if (result != null && !result.equals("")) {
                            String s = result.trim();
                            textView.setText(result);

                            imageView.setImageBitmap(bitmap);

                            Log.i(TAG, "S is "+s);
                        }
                        mProgressDialog.dismiss();
                    }

                });

            }
        }).start();
    }
    private Bitmap convertColorIntoBlackAndWhiteImage(Bitmap originalBitmap) {
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);

        ColorMatrixColorFilter colorMatrixFilter = new ColorMatrixColorFilter(colorMatrix);

        Bitmap blackAndWhiteBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);

        Paint paint = new Paint();
        paint.setColorFilter(colorMatrixFilter);

        Canvas canvas = new Canvas(blackAndWhiteBitmap);
        canvas.drawBitmap(blackAndWhiteBitmap, 0, 0, paint);
        /*****************************************************
         * Title: How to use OpenCV to process image so that the text become sharp and clear?
         * Author: Bruce
         * Site owner/sponsor: stackoverflow.com
         * Date: 2013
         * Code version: edited Jul 28 '13 at 16:22
         * Availability: http://stackoverflow.com/questions/17874149/how-to-use-opencv-to-process-image-so-that-the-text-become-sharp-and-clear
         (Accessed 04 May 2017)
         *****************************************************/

        /*Mat imageMat = new Mat();
        Utils.bitmapToMat(originalBitmap, imageMat);
        Imgproc.cvtColor(imageMat, imageMat, Imgproc.COLOR_BGR2GRAY);
        Imgproc.medianBlur(imageMat, imageMat, 3);
        Imgproc.threshold(imageMat, imageMat, 0, 255, Imgproc.THRESH_OTSU);
        Utils.matToBitmap(imageMat,blackAndWhiteBitmap);*/
        //End of [non-original or refactored] code
        return blackAndWhiteBitmap;
    }
}
