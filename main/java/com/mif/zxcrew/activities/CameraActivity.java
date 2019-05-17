package com.mif.zxcrew.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.hardware.Camera;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.mif.zxcrew.helpers.OpenCVHelper;
import com.mif.zxcrew.helpers.TessHelper;
import com.mif.zxcrew.ocrcards.CameraSurface;
import com.mif.zxcrew.ocrcards.Database;
import com.mif.zxcrew.ocrcards.R;
import com.mif.zxcrew.txtclassifier.Card;
import com.mif.zxcrew.txtclassifier.Classifier;
import com.mif.zxcrew.txtclassifier.Contact;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * CameraActivity.java
 *
 * Purpose: Activity for displaying camera view, importing photo from gallery and to guarantee best photo properties;
 *
 *  dispatchTakeGalleryPhotoIntent() takes photo intent from gallery;
 *
 *  findBackFacingCamera() finds the Camera that is facing the back of telephone;
 *
 *  chooseCamera() activates the camera found in the back;
 *
 *  releaseCamera() releases the memory and the camera when leaving/pausing activity;
 * 
 *  getPictureCallback() called and returns a photo when camera takes a photo;
 * 
 *  progressDialog() calls progress dialog into the screen view;
 * 
 *  getSimilarContacts() returns contacts similar to scanned name and surname;
 * 
 *  appendCard() appends card to an existing contact;
 * 
 * @author Aurimas Garnevicius
 * @author Aivaras Ivoskus
 * @author Daniel Spakovskij
 */
public class CameraActivity extends AppCompatActivity {

    public static final String TAG = "CAMERA_ACTIVITY";
    public static final int GALLERY_RETURN = 100;

    private static  final int FOCUS_AREA_SIZE = 300;

    private Camera mCamera;
    private CameraSurface mPreview;
    private Camera.PictureCallback mPicture;
    private FloatingActionButton capture;
    private ImageButton flash, card;
    private LinearLayout hintLand;
    private LinearLayout hintPort;
    private Boolean cardPosition; //True = landscape, false = portrait
    private String lang;
    private Context myContext;
    private ProgressDialog progress;
    private LinearLayout cameraPreview;
    private boolean cameraFront = false;
    public static Bitmap bitmap;

    BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                }

                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.i(TAG, "Going to build new Content View");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        myContext = this;

        mCamera =  Camera.open();
        mCamera.setDisplayOrientation(90);
        cameraPreview =  findViewById(R.id.cPreview);
        mPreview = new CameraSurface(myContext, mCamera);
        cameraPreview.addView(mPreview);

        cameraPreview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    focusOnTouch(event);
                }
                return true;
            }
        });

        //Flash
        flash = (ImageButton) findViewById(R.id.flash);
        flash.setBackgroundResource(R.drawable.id_flash_off);
        flash.setOnClickListener(new OnClickListener() {
            Camera.Parameters params = mCamera.getParameters();
            boolean state = false;
            public void onClick(View view) {
                state = !state;

                if(!state){
                    params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    flash.setBackgroundResource(R.drawable.id_flash_off);
                }else{
                    params.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                    flash.setBackgroundResource( R.drawable.id_flash_on);
                }
                mCamera.setParameters(params);
            }
        });

        //Language spinner
        final Spinner mSpinner = (Spinner) findViewById(R.id.language);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(CameraActivity.this, android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.languagesList));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);
        lang = mSpinner.getSelectedItem().toString().toLowerCase();

        //Card position
        hintLand = (LinearLayout) findViewById(R.id.landscape);
        hintPort = (LinearLayout) findViewById(R.id.portrait);
        hintPort.setVisibility(View.GONE);

        card = (ImageButton) findViewById(R.id.card);
        card.setBackgroundResource(R.drawable.ic_card_landscape);
        cardPosition = true;
        card.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                cardPosition = !cardPosition;
                if (!cardPosition) {
                    hintLand.setVisibility(View.GONE);
                    hintPort.setVisibility(View.VISIBLE);
                    card.setBackgroundResource(R.drawable.ic_card_portrait);
                }else {
                    hintPort.setVisibility(View.GONE);
                    hintLand.setVisibility(View.VISIBLE);
                    card.setBackgroundResource(R.drawable.ic_card_landscape);
                }
            }
        });

        //Capture
        capture = (FloatingActionButton) findViewById(R.id.btnCam);
        capture.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Take picture to be called");

                mCamera.takePicture(null, null, mPicture);
                Log.i(TAG, mPicture.toString());
            }
        });

        ImageButton button = (ImageButton) findViewById(R.id.btnGallery);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakeGalleryPhotoIntent();
            }
        });



        releaseCamera();
        chooseCamera();


        Log.i(TAG, "Starting Camera Preview");
        mCamera.startPreview();
    }

    private void dispatchTakeGalleryPhotoIntent(){

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_RETURN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == GALLERY_RETURN) {
            if (data == null) {
                //Display an error
                return;
            }

            try {
                // Convert file to bitmap
                InputStream inputStream = this.getContentResolver().openInputStream(data.getData());
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                scanBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private int findBackFacingCamera() {
        int cameraId = -1;
        //Search for the back facing camera
        //get the number of cameras
        int numberOfCameras = Camera.getNumberOfCameras();
        //for every camera check
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                cameraFront = false;
                break;

            }

        }
        return cameraId;
    }

    public void onResume() {
        Log.i(TAG, "Resuming Activity");
        super.onResume();

        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync("3.4.3",
                    this, mLoaderCallback);
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

        if(mCamera == null) {
            mCamera = Camera.open();
            mCamera.setDisplayOrientation(90);
            mPicture = getPictureCallback();
            mPreview.refreshCamera(mCamera);
            Log.d(TAG, "null");
        }else {
            Log.d(TAG,"no null");
        }

    }

    public void chooseCamera() {

        int cameraId = findBackFacingCamera();
        if (cameraId >= 0) {
            //open the backFacingCamera
            //set a picture callback
            //refresh the preview
            mCamera = Camera.open(cameraId);
            mCamera.setDisplayOrientation(90);
            mPicture = getPictureCallback();
            mPreview.refreshCamera(mCamera);
        }
        
    }

    @Override
    protected void onPause() {
        super.onPause();
        //when on Pause, release camera in order to be used from other applications
        releaseCamera();
    }

    private void releaseCamera() {
        // stop and release camera
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    private void focusOnTouch(MotionEvent event) {
        if (mCamera != null ) {

            Camera.Parameters parameters = mCamera.getParameters();
            if (parameters.getMaxNumMeteringAreas() > 0){
                Log.i(TAG,"fancy !");
                Rect rect = calculateFocusArea(event.getX(), event.getY());

                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();
                meteringAreas.add(new Camera.Area(rect, 800));
                parameters.setFocusAreas(meteringAreas);

                mCamera.autoFocus(mAutoFocusTakePictureCallback);
            }else {
                mCamera.autoFocus(mAutoFocusTakePictureCallback);
            }
        }
    }

    private Rect calculateFocusArea(float x, float y) {
        int left = clamp(Float.valueOf((x / cameraPreview.getWidth()) * 2000 - 1000).intValue(), FOCUS_AREA_SIZE);
        int top = clamp(Float.valueOf((y / cameraPreview.getHeight()) * 2000 - 1000).intValue(), FOCUS_AREA_SIZE);

        return new Rect(left, top, left + FOCUS_AREA_SIZE, top + FOCUS_AREA_SIZE);
    }

    private int clamp(int touchCoordinateInCameraReper, int focusAreaSize) {
        int result;
        if (Math.abs(touchCoordinateInCameraReper)+focusAreaSize/2>1000){
            if (touchCoordinateInCameraReper>0){
                result = 1000 - focusAreaSize/2;
            } else {
                result = -1000 + focusAreaSize/2;
            }
        } else{
            result = touchCoordinateInCameraReper - focusAreaSize/2;
        }
        return result;
    }

    private void scanBitmap(Bitmap bitmap){
        // OpenCV part
        Log.i(TAG, "Going to get bounding boxes");

        //rotation
        Matrix matrix = new Matrix();

        if(!cardPosition) {
            matrix.postRotate(90);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }

        Bitmap[] bitmaps = OpenCVHelper.GetBitmapBoundingBoxes(bitmap, false, cardPosition);

        // Tesseract part
        Log.i(TAG, "Going to get the text out of the bounding boxes");
        TessHelper helper = new TessHelper(getAssets(), CameraActivity.this, lang);
        helper.setTessPageMode(TessBaseAPI.PageSegMode.PSM_AUTO);
        String[] texts = helper.getTextPerBitmap(bitmaps);

        for(int i = 0; i < texts.length; i++){
            Log.i(TAG, String.valueOf(i) + ": " + texts[i]);
        }

        String concated = "";
        for(int i = texts.length-1 ; i >= 0 ; i--)
            concated += texts[i] + "\n";

        final Classifier classifier = new Classifier(concated, lang);
        classifier.getCard().setBitmap(bitmaps[bitmaps.length-1]);

        final Contact contact = new Contact();
        contact.setName(classifier.getCard().getName());
        contact.setLastname(classifier.getCard().getLname());
        Contact[] similarContacts = Database.getSimilarContacts(contact);
        final LinkedList<Contact> cont = new LinkedList<>();
        for(Contact c: similarContacts)
            cont.add(c);

        if(similarContacts.length != 0) {
            similarContacts(cont, classifier.getCard(), contact);
            
            // This will also require seperation of code below into a different method, ask Aivaras for more info if needed
        } else {
            appendCard(contact, classifier.getCard(), true);
        }



//        progress.dismiss();
    }



    private Camera.PictureCallback getPictureCallback() {

        Camera.PictureCallback picture = new Camera.PictureCallback() {


            @Override
            public void onPictureTaken(byte[] data, Camera camera) {

                bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                Log.i(TAG, "Picture taken, going to decode it to bitmap");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog();
                    }
                });

                // Create new thread and run it
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        scanBitmap(bitmap);
                    }
                };

                thread.run();
            }
        };
        return picture;
    }

    private Camera.AutoFocusCallback mAutoFocusTakePictureCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            if (success) {
                Log.i(TAG,"Focused on the Card");
            } else {
                Log.i(TAG,"Failed to focus on the Card");
            }
        }
    };

    private void progressDialog(){

        progress = new ProgressDialog(CameraActivity.this);
        progress.setTitle("Loading");
        progress.setMessage("Scanning card information.");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();
        Log.i("PROGRESS BAR" , "paejo");
    }

    private void similarContacts(LinkedList<Contact> similarContacts, final Card card, final Contact originalContact){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.custom_contacts_dialog, null, false);
        builder.setView(view);

        ConstraintLayout consLayout = (ConstraintLayout) view;
        LinearLayout linearLayout = view.findViewById(R.id.contactSimilar);

        for(final Contact c: similarContacts) {
            view = getLayoutInflater().inflate(R.layout.activity_list_item, linearLayout, false);
            linearLayout.addView(view);

            TextView item = (TextView) view.findViewById(R.id.item);
            TextView subItem = (TextView) view.findViewById(R.id.subitem);
            TextView subItem2 = (TextView) view.findViewById(R.id.subitem2);
            ImageView image = (ImageView) view.findViewById(R.id.image);
            item.setText(c.getName() + "  " + c.getLastname());
            subItem.setText(c.getCard(0).getCompany());
            subItem2.setText(c.getCard(0).getTelNo());
            image.setImageBitmap(c.getCard(0).getBitmap());

            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    appendCard(c, card, false);
                }
            });
        }

        Button button = consLayout.findViewById(R.id.newBut);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                appendCard(originalContact, card, true);
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void appendCard(Contact contact, Card card, boolean isNew){
        card.setBelongsTo(contact);
        Database.InsertResult insertResult;
        if(isNew) {
             insertResult = Database.insertAndSaveContact(this, contact, card);
        }else{
            insertResult = Database.updateContact(this, contact, card);
        }
        Intent intent = new Intent(CameraActivity.this, ContactEditActivity.class);
        intent.putExtra(ContactEditActivity.KEY_CARD_INDEX, insertResult.getCardIndex());
        intent.putExtra(ContactEditActivity.KEY_CONTACT_INDEX, insertResult.getContactIndex());
        startActivity(intent);
    }



}
