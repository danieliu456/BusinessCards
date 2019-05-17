package com.mif.zxcrew.helpers;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 *  OpenCVHelper.java
 *
 *  class helps to control and manipulate OpenCV functions
 *
 *  GetBitmapBoundingBoxes() sets cropped bitmaps to array;
 *
 *  @author  Aivaras Ivoskus
 * @author  Daniel Spakovskij
 */
public class OpenCVHelper {

    public static final String TAG = "OPENCVHELPER";
    public static native long[] boundingBoxes(long matAddrRgba, boolean drawRect, boolean cardPosition);

    public static Bitmap[] GetBitmapBoundingBoxes(Bitmap bitmap, boolean drawRect, boolean cardPosition){

        System.loadLibrary("native-lib");
        System.loadLibrary("opencv_java3");

        Log.i(TAG, "Received bitmap of type: " + bitmap.getConfig().toString());

        // Scaling
        float aspectRatio = bitmap.getWidth() /
                (float) bitmap.getHeight();
        int width = 960 * 2;
        int height = Math.round(width / aspectRatio);

        bitmap = Bitmap.createScaledBitmap(
                bitmap, width, height, true);

        // Conversion to mat
        Mat mat = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC4);
        Utils.bitmapToMat(bitmap, mat);
        long[] addresses =  boundingBoxes(mat.getNativeObjAddr(), drawRect, cardPosition);

        Mat[] croppedMats = new Mat[addresses.length];
        Bitmap bitmaps[] = new Bitmap[addresses.length + 1];

        for(int i = 0; i < addresses.length; i++){
            croppedMats[i] = new Mat(addresses[i]);
            Log.i(TAG, "Address received: " + addresses[i]);
            bitmaps[i] = Bitmap.createBitmap(croppedMats[i].cols(),croppedMats[i].rows(),Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(croppedMats[i], bitmaps[i]);
            croppedMats[i].release();
        }

        bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bitmap);
        bitmaps[bitmaps.length - 1] = bitmap;

        Utils.matToBitmap(mat, bitmap);
        Log.i(TAG, "Going to return bounding box amount: " + croppedMats.length);
        return bitmaps;
    }

}
