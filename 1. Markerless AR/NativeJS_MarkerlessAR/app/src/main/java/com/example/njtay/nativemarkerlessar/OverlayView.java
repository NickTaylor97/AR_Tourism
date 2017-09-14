package com.example.njtay.nativemarkerlessar;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;


/**
 * Created by njtay on 27/06/2017.
 */

public class OverlayView extends View implements SensorEventListener, LocationListener {

    public static final String DEBUG_TAG = "OverlayView Log";
    String accelData = "Accelerometer Data";
    String compassData = "Compass Data";
    String gyroData = "Gyro Data";

    public Bitmap keteBitmap;

    static final float ALPHA = 0.25f; // if ALPHA = 1 OR 0, no filter applies.

    float verticalFOV = 0;
    float horizontalFOV = 0;

    SensorManager sensors;
    Sensor accelSensor;
    Sensor compassSensor;
    Sensor gyroSensor;

    boolean isAccelAvailable;
    boolean isCompassAvailable;
    boolean isGyroAvailable;

    int bitmapLeft;
    int bitmapTop;
    int bitmapRight;
    int bitmapBottom;

    float curBearingToUni;

    float rotation[] = new float[9];
    float identity[] = new float[9];
    float cameraRotation[] = new float[9];
    float orientation[] = new float[3];
    boolean gotRotation;

    LocationManager locationManager;

    private final static Location targetLoc = new Location("manual");
    static {
        targetLoc.setLatitude(-37.807167478442366d);
        targetLoc.setLongitude(175.3019263743863d);
        targetLoc.setAltitude(10d);
    }

    float[] m_lastAccels = new float[3];
    float[] m_lastComp = new float[3];;



    public OverlayView(Context context) {
        super(context);
        sensors = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelSensor = sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        compassSensor = sensors.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        gyroSensor = sensors.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        isAccelAvailable = sensors.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_NORMAL);
        isCompassAvailable = sensors.registerListener(this, compassSensor, SensorManager.SENSOR_DELAY_NORMAL);
        isGyroAvailable = sensors.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_NORMAL);

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);


        String best = locationManager.getBestProvider(criteria, true);

        try {
            locationManager.requestLocationUpdates(best, 50, 0, this);
            Log.v("Test","Best provider: " + best);

        }
        catch(SecurityException ex)
        {

        }

        try{
            Camera mCamera = Camera.open();//you can use open(int) to use different cameras
            if(mCamera != null) {
                CameraView mCameraView = new CameraView(getContext(), mCamera);
                horizontalFOV = mCameraView.getHVA();
                verticalFOV = mCameraView.getVVA();

            }
        } catch (Exception e){
            Log.d("ERROR", "Failed to get camera: " + e.getMessage());
        }

        try {
            // get input stream
            AssetManager assetManager = context.getAssets();
            InputStream ims = assetManager.open("kete.png");
            keteBitmap = BitmapFactory.decodeStream(ims);
            // load image as Drawable

            // set image to ImageView
            ;
        }
        catch(IOException ex) {

            return;
        }






    }


    protected void onCreate(Bundle savedInstanceState)
    {

    }



    public void setCamFOV(float vertFOV, float horFOV)
    {
            verticalFOV = vertFOV;
            horizontalFOV = horFOV;
    }

    @Override
    protected void onDraw(Canvas canvas) {


        super.onDraw(canvas);

        Paint contentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Paint targetPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        targetPaint.setColor(Color.GREEN);
        targetPaint.setStrokeWidth(10);
        contentPaint.setTextAlign(Paint.Align.CENTER);
        contentPaint.setTextSize(20);
        contentPaint.setColor(Color.RED);
        //canvas.drawText(accelData, canvas.getWidth() / 2, canvas.getHeight() / 4, contentPaint);
        //canvas.drawText(compassData, canvas.getWidth() / 2, canvas.getHeight() / 2, contentPaint);
        //canvas.drawText(gyroData, canvas.getWidth()/2, (canvas.getHeight()*3)/4, contentPaint);
        Log.d("stats", "What is the vert field? " + String.valueOf(verticalFOV));
        if (lastLocation != null) {
            curBearingToUni = lastLocation.bearingTo(targetLoc);


            //canvas.drawText(String.valueOf(lastLocation.getLatitude()) + "   " + String.valueOf(lastLocation.getLongitude()) ,canvas.getWidth()/2, (canvas.getHeight()*7)/8, contentPaint);
            Log.d("Loc", "What is the difference in m  " + String.valueOf(lastLocation.distanceTo(targetLoc)));

        }


        canvas.rotate((float)(0.0f- Math.toDegrees(orientation[2])));

        float dx = (float) ( (canvas.getWidth()/ horizontalFOV) * (Math.toDegrees(orientation[0])-curBearingToUni));
        float dy = (float) ( (canvas.getHeight()/ verticalFOV) * Math.toDegrees(orientation[1]));



        //canvas.translate(0.0f, 0.0f- dy);


        //canvas.drawLine(0, canvas.getHeight()/2, canvas.getWidth(), canvas.getHeight()/2, targetPaint);

        //canvas.translate(0.0f-dx, 0.0f);


        //canvas.drawCircle(canvas.getWidth()/2, canvas.getHeight()/2, 50f, targetPaint);

        //Log.d("location", "Latitude :  " + String.valueOf(lastLocation.getLatitude()) + "Longitude :  " + String.valueOf(lastLocation.getLongitude()));

        bitmapLeft = (canvas.getWidth()/2) - 320;
        bitmapTop = (canvas.getHeight() * 2) - Math.round(dy);
        bitmapRight = (canvas.getWidth()/2) + 320;
        bitmapBottom = (canvas.getHeight() * 2 + 544) - Math.round(dy);

        if(lastLocation != null)
        {
            //if(lastLocation.distanceTo(targetLoc) < 30)
            //{
                if(keteBitmap != null)
                {
                    Log.d("test", "bitmap is null");
                    canvas.drawBitmap(keteBitmap, null, new Rect(bitmapLeft , bitmapTop ,bitmapRight, bitmapBottom), null);
                }
                else {

                }
            //}




        }


    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        float x = event.getX();
        float y = event.getY();
        switch(event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                //Check if the x and y position of the touch is inside the bitmap
                if( x > bitmapLeft && x < bitmapRight && y > bitmapTop && y < bitmapBottom  )
                {
                    Uri uri = Uri.parse("http://www.google.com/#q=kete");
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getContext().startActivity(intent);
                }
                return true;
        }
        return false;
    }

    public float[] lowPass(float[] newVals, float[] oldVals)
    {

        if ( oldVals == null ) {
            return newVals;
        }
        for ( int i=0; i<newVals.length; i++ )
        {
            oldVals[i] = oldVals[i] + ALPHA * (newVals[i] - oldVals[i]);
        }
        return oldVals;


    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        StringBuilder msg = new StringBuilder(event.sensor.getName()).append(" ");
        for(float value: event.values)
        {
            msg.append("[").append(value).append("]");
        }

        switch(event.sensor.getType())
        {
            case Sensor.TYPE_ACCELEROMETER:
                accelData = msg.toString();

                m_lastAccels = lowPass(event.values.clone(), m_lastAccels);


                //System.arraycopy(event.values, 0, m_lastAccels, 0, 3);
                break;
            case Sensor.TYPE_GYROSCOPE:
                gyroData = msg.toString(); //Not even used
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                compassData = msg.toString();

                m_lastComp = lowPass(event.values.clone(), m_lastComp);
                //System.arraycopy(event.values, 0, m_lastComp, 0, 3);
                break;

        }

        if (m_lastAccels != null && m_lastComp != null) {
            gotRotation = SensorManager.getRotationMatrix(rotation,
                    identity, m_lastAccels, m_lastComp);
            if (gotRotation) {

                // remap such that the camera is pointing straight down the Y axis
                SensorManager.remapCoordinateSystem(rotation, SensorManager.AXIS_X,
                        SensorManager.AXIS_Z, cameraRotation);

                // orientation vector

                SensorManager.getOrientation(cameraRotation, orientation);
                if (gotRotation) {



                    // remap such that the camera is pointing along the positive direction of the Y axis
                    SensorManager.remapCoordinateSystem(rotation, SensorManager.AXIS_X,
                            SensorManager.AXIS_Z, cameraRotation);

                    // orientation vector
                    //Log.d("test", "What is field of view? " + String.valueOf(horizontalFOV));

                    SensorManager.getOrientation(cameraRotation, orientation);




                }

            }

        }

        this.invalidate();

    }

    private Location lastLocation = null;

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if(lastLocation != null)
        {
            double latDiff = Math.abs(location.getLatitude() - lastLocation.getLatitude());
            double longDiff = Math.abs(location.getLongitude() - lastLocation.getLongitude());
            if(latDiff > 0.000001 || longDiff > 0.000001) {
                Log.d("Loc", "  LatDiff:  " + String.valueOf(latDiff) + "  LongDiff:  " + String.valueOf(longDiff));
            }
        }

        lastLocation = location;

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}