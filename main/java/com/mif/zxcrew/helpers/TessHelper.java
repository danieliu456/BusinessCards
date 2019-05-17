package com.mif.zxcrew.helpers;

import android.content.ContextWrapper;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * TessHelper.java
 * Purpose: Simplified usage of tesseract
 * Documentation of Tesseract JAVA API: https://rmtheis.github.io/tess-two/javadoc/index.html
 *
 * TessHelper() Constructor used for initialization of tesseract;
 *
 * copyTessDataFiles() Prepares language files for the Tesseract from assets location to reachable place inside app;
 *
 * prepareDirectories() prepares directories for libraries, tesseract languages;
 *
 * @author Aivaras Ivoskus
 * @version 1.0
 */
public class TessHelper {

    public final String TAG = "TESSERACT";

    private TessBaseAPI tess;

    private static final String DATA_PATH = "OCRCard/";
    private static final String TESSDATA = "tessdata";

    private static File tessDataDir;

    public TessHelper(AssetManager assets, ContextWrapper wrapper, String lang){

        // Prepare directories if needed
        System.out.println("Going to init Tesseract: ");
        try {
            prepareDirectories(wrapper);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Copy data
        copyTessDataFiles(TESSDATA, assets, wrapper);

        tess = new TessBaseAPI();

        // Path is to the directory where tessdata is located
        tess.init(wrapper.getDir("Tesseract",0).getAbsolutePath().toString(),"eng");
        tess.setDebug(true);
        tess.setVariable("load_system_dawg", "false");
        tess.setVariable("load_freq_dawg", "false");
        Log.d(TAG, "TessHelper initialized without a problem");
    }

    public void setTessPageMode(int mode){
        tess.setPageSegMode(mode);
    }

    public void setTessBitmap(Bitmap bitmap){
        tess.setImage(bitmap);
    }

    public String getText(){
        return tess.getUTF8Text();
    }

    public String[] getTextPerBitmap(Bitmap[] bitmaps) {
        String[] strings = new String[bitmaps.length];
        // last bitmap in array is entire picture;
        for(int i = 0; i < bitmaps.length - 1; i++){
            tess.setImage(bitmaps[i]);
            strings[i] = tess.getUTF8Text();
        }
        return strings;
    }

    private void copyTessDataFiles(String path, AssetManager assets, ContextWrapper wrapper) {

        try {

            String fileList[] = assets.list(path);

            for (String fileName : fileList) {

                File file = new File(tessDataDir.getAbsoluteFile().toString() +"/" + fileName);
                if(file.exists()) {
                    Log.i(TAG, fileName + " already exists, wont upload and gonna skip");
                    continue;
                }

                InputStream in = assets.open(path + "/" + fileName);

                System.out.println("Going to copy to " + tessDataDir.getAbsoluteFile().toString() +"/" + fileName);
                OutputStream out = new FileOutputStream (file);

                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;

                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }

                in.close();
                out.close();

                Log.d(TAG, "Copied " + fileName + "to tessdata");
            }
        } catch (IOException e) {
            Log.e(TAG, "Unable to copy files to tessdata " + e.toString());
        }
    }

    private void prepareDirectories(ContextWrapper wrapper){

        Log.d(TAG, "Making folders for Tesseract");

        // Create Folder within folder
        // tessdata is obligatory for the Tesseract system
        File mainDir = wrapper.getDir("Tesseract",0);
        tessDataDir = new File(mainDir, "tessdata");
        tessDataDir.mkdir();

    }

    public static File GetTessDataDir(){
        if(tessDataDir != null)
            return tessDataDir;
        else
            return null;
    }
}
