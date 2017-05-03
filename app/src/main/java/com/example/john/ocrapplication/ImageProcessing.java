package com.example.john.ocrapplication;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Environment;

import java.io.File;

public class ImageProcessing {
    int widthOfImage, heightOfImage;
    //do binirization
//    public Bitmap BinarizeImage(Bitmap imageBitmap) {
//        widthOfImage = imageBitmap.getWidth();
//        heightOfImage = imageBitmap.getHeight();
//
//        Bitmap bitmapGreyScale = Bitmap.createBitmap(widthOfImage, heightOfImage, Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(bitmapGreyScale);
//        Paint paint = new Paint();
//        ColorMatrix colorMatrix = new ColorMatrix();
//        colorMatrix.setSaturation(0);
//        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);
//        paint.setColorFilter(filter);
//        canvas.drawBitmap(imageBitmap, 0, 0, paint);
//
//        return bitmapGreyScale;
//    }

    public Bitmap findContours(Bitmap bitmap){

        for(int x=0;x<=bitmap.getWidth();x++){
            for(int y =0;x<=bitmap.getHeight();y++){
                int currentLocation = bitmap.getPixel(x,y);
                if(currentLocation == Color.BLACK){
                    // y +1 is black

                    int j=y;
                    while(bitmap.getPixel(x,j)== Color.BLACK){
                        j++;
                    }
                    j--;
                    int endyloc = bitmap.getPixel(x,j);

                    int k=x;
                    while(bitmap.getPixel(k,y)== Color.BLACK){
                        k++;
                    }
                    k--;
                    int endXloc = bitmap.getPixel(k,y);

                    int f=j;
                    while(bitmap.getPixel(k,f)== Color.BLACK){
                        f++;
                    }
                    f--;
                    int lastLoc = bitmap.getPixel(k,f);
                    for(int firstx=x; firstx<=k; firstx++){
                    for(int firsty=y;firsty>=j;firsty++){
                            bitmap.setPixel(firstx,firsty,Color.WHITE);
                        }
                    }
                }
            }
        }
        return bitmap;
    }
    //Inverse the color
    //erosion
    //Seperate contours
    //Find all contours
    //Delete all contours that width more than height  or do edge detection
    // find large rectangle that contains remain contours
    //http://stackoverflow.com/questions/33881175/remove-background-noise-from-image-to-make-text-more-clear-for-ocr
    //https://nayefreza.wordpress.com/2013/05/15/outer-contour-tracing-using-square-tracing-algorithm-for-binary-image-java-implementation/
}
