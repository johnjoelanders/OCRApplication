package com.example.john.ocrapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    ImageView imageView;
    Button btnCamera, btnGallery, btnConvert;
    static final int CAM_REQUEST = 1;
    static final int GALLERY_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView) findViewById(R.id.imageView);
        btnCamera = (Button) findViewById(R.id.btnOpenCamera);
        btnGallery = (Button) findViewById(R.id.btnGallery);
        btnConvert = (Button) findViewById(R.id.btnConvert);

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
                    Context context = getApplicationContext();
                    CharSequence text = "You clicked convert";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();

                }
            });
        }
    }

    private File getFile() {
        File folder = new File("sdcard/camera_app");
        if (!folder.exists()) {
            folder.mkdir();
        }
        File image_file = new File(folder, "cam_image.jpg");
        return image_file;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode){
            case GALLERY_REQUEST:
                Uri imageUri = data.getData();
                InputStream inputStream;
                try {
                    inputStream = getContentResolver().openInputStream(imageUri);
                    Bitmap image = BitmapFactory.decodeStream(inputStream);
                    imageView.setImageBitmap(convertImageToGreyScale(image));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Cannot open image", Toast.LENGTH_SHORT).show();
                }
                break;
            case CAM_REQUEST:
                String path = "sdcard/camera_app/cam_image.jpg";
                imageView.setImageDrawable(Drawable.createFromPath(path));
                break;
        }

    }
    //Convert to greyscale
    public Bitmap convertImageToGreyScale(Bitmap imageBitmap){
        int widthOfImage,heightOfImage;
        widthOfImage = imageBitmap.getWidth();
        heightOfImage = imageBitmap.getHeight();

        Bitmap bitmapGreyScale = Bitmap.createBitmap(widthOfImage,heightOfImage,Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmapGreyScale);
        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);
        paint.setColorFilter(filter);
        canvas.drawBitmap(imageBitmap,0,0,paint);
        return bitmapGreyScale;
    }

}
