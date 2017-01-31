package com.example.john.ocrapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    ImageView imageView;
    Button btnCamera,btnGallery,btnConvert;
    static final  int CAM_REQUEST = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView =(ImageView)findViewById(R.id.imageView);
        btnCamera = (Button)findViewById(R.id.btnOpenCamera);
        btnGallery = (Button)findViewById(R.id.btnGallery);
        btnConvert = (Button)findViewById(R.id.btnConvert);

        if (btnCamera != null) {
            btnCamera.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File file = getFile();
                    camera_intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                    startActivityForResult(camera_intent,CAM_REQUEST);
                }
            });
        }

        if (btnGallery != null) {
            btnGallery.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Context context = getApplicationContext();
                    CharSequence text = "You opened the gallery";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();

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
    private File getFile(){
        File folder = new File("sdcard/camera_app");
        if(!folder.exists()){
            folder.mkdir();
        }
        File image_file = new File(folder,"cam_image.jpg");
        return image_file;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        String path = "sdcard/camera_app/cam_image.jpg";
        imageView.setImageDrawable(Drawable.createFromPath(path));

    }

}
