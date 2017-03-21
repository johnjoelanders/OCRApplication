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
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
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
    static final int CONVERT = 1;

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

        if (btnConvert != null) {
            btnConvert.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                        startCameraActivity();

                }
            });
        }
    }
    private void startCameraActivity(){
        try {
            String IMGS_PATH = Environment.getExternalStorageDirectory().toString() + "/TesseractSample/imgs";
            prepareDirectory(IMGS_PATH);

            String img_path = IMGS_PATH + "/ocr.jpg";

            outputFileUri = Uri.fromFile(new File(img_path));

            final Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, CONVERT);
                imageBitmap = BitmapFactory.decodeFile(img_path);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }
    public void onActivityResult(int requestCode, int resultCode,Intent data) {
        if (requestCode == CONVERT) {
            doOCR();
        } else {
            Toast.makeText(this, "ERROR: Image was not obtained.", Toast.LENGTH_SHORT).show();
        }
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
        tessBaseApi.init(DATA_PATH,lang);
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
            mProgressDialog = ProgressDialog.show(this, "Processing",
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

            };
        }).start();
    }
    private Bitmap convertColorIntoBlackAndWhiteImage(Bitmap orginalBitmap) {
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);

        ColorMatrixColorFilter colorMatrixFilter = new ColorMatrixColorFilter(
                colorMatrix);

        Bitmap blackAndWhiteBitmap = orginalBitmap.copy(
                Bitmap.Config.ARGB_8888, true);

        Paint paint = new Paint();
        paint.setColorFilter(colorMatrixFilter);

        Canvas canvas = new Canvas(blackAndWhiteBitmap);
        canvas.drawBitmap(blackAndWhiteBitmap, 0, 0, paint);

        return blackAndWhiteBitmap;
    }
//     class OcrOperationAsync extends AsyncTask<Void,Void,String>{
//
//         @Override
//         protected String doInBackground(Void... params) {
//             try{
//                 BitmapFactory.Options options = new BitmapFactory.Options();
//                 options.inSampleSize = 4;
//                 Bitmap bitmap = BitmapFactory.decodeFile(outputFileUri.getPath(), options);
//                 result = extractText(bitmap);
//             }catch(Exception e){
//                 Log.e(TAG,"The error is "+e);
//             }
//             return result;
//         }
//     }

}
