package com.example.njtay.nativemarkerlessar;

import android.animation.ObjectAnimator;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class CameraActivity extends AppCompatActivity {

    private Camera mCamera = null;
    private CameraView mCameraView = null;


    float x,y,z = 5;
    int ix, iy, iz = 0;

    ImageView keteImage;
    ObjectAnimator animX;
    float keteX, keteY = 0;


    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        try{
            mCamera = Camera.open();//you can use open(int) to use different cameras
        } catch (Exception e){
            Log.d("ERROR", "Failed to get camera: " + e.getMessage());
        }

        if(mCamera != null) {
            mCameraView = new CameraView(this, mCamera);//create a SurfaceView to show camera data

            FrameLayout camera_view = (FrameLayout)findViewById(R.id.camera_view);
            camera_view.addView(mCameraView);//add the SurfaceView to the layout
            OverlayView arContent = new OverlayView(getApplicationContext());
            //set camera field of view
            arContent.setCamFOV(mCameraView.getVVA(), mCameraView.getHVA());



            camera_view.addView(arContent);


        }


    }





}
