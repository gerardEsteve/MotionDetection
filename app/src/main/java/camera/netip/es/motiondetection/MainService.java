package camera.netip.es.motiondetection;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.Date;

import camera.netip.es.motiondetection.config.CameraFacing;
import camera.netip.es.motiondetection.config.CameraFocus;
import camera.netip.es.motiondetection.config.CameraImageFormat;
import camera.netip.es.motiondetection.config.CameraResolution;
import camera.netip.es.motiondetection.config.CameraRotation;

public class MainService extends HiddenCameraService {

    // Specific settings
    private static final int mPixelThreshold = 50; // Difference in pixel (RGB)
    boolean primero = true,segundo = true;
    Bitmap first,second;
    public int firstHeight, firstWidth, secondHeight, secondWidth;
    public int[] firstPixels, secondPixels;
    Context activity;
    private MyBinder myBinder;

    public void passContext(Activity parent) {
        Log.i("passContext", "passContext" );
        activity = parent;
        waitAndStartAgain(1000);
    }

    /**
     * clase para obtener la referencia al servicio
     */
    public class MyBinder extends Binder {
        MainService getService() {
            return MainService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    @Override
    public void onImageCapture(@NonNull File imageFile) {
        String tmp = "son diferentes";
        boolean b = false;
        // Convert file to bitmap.
        Bitmap bitmap = getBitmapFromFile(imageFile);
        if (primero || segundo){
            iniciarValores(bitmap);
            return;
        }

        //Preparamos la comparación con la imagen anterior

        //Comparamos los 2 bitmaps
        // b = true -> son diferentes
        // b = false -> son iguales
        b = compararBitmaps(bitmap);
        if (b){
            // Las 2 imagenes comparadas son diferentes, con lo que
            // hemos detectado un cambio superior al threshold

            //TODO añadir accion de movimiento detectado

        }
        else{
            // Las 2 imagenes comparadas son iguales, con lo que
            // no ha habido movimientos detectados por la camara
            tmp = "son iguales";
            // TODO añadir accion de imagenes iguales

        }
        Log.i("Image CapturedService", "comparacion : " + tmp);

        waitAndStartAgain(1000);
        // Guardamos la imagen en la carpeta saved_images
        //SaveImage(bitmap);
    }

    @Override
    public void onCameraError(int errorCode) {
        switch (errorCode) {
            case CameraError.ERROR_CAMERA_OPEN_FAILED:
                //Camera open failed. Probably because another application
                //is using the camera
                Toast.makeText(activity, R.string.error_cannot_open, Toast.LENGTH_LONG).show();
                break;
            case CameraError.ERROR_IMAGE_WRITE_FAILED:
                //Image write failed. Please check if you have provided WRITE_EXTERNAL_STORAGE permission
                Toast.makeText(activity, R.string.error_cannot_write, Toast.LENGTH_LONG).show();
                break;
            case CameraError.ERROR_CAMERA_PERMISSION_NOT_AVAILABLE:
                //camera permission is not available
                //Ask for the camera permission before initializing it.
                Toast.makeText(activity, R.string.error_cannot_get_permission, Toast.LENGTH_LONG).show();
                break;
            case CameraError.ERROR_DOES_NOT_HAVE_OVERDRAW_PERMISSION:
                //Display information dialog to the user with steps to grant "Draw over other app"
                //permission for the app.
                HiddenCameraUtils.openDrawOverPermissionSetting(activity);
                break;
            case CameraError.ERROR_DOES_NOT_HAVE_FRONT_CAMERA:
                Toast.makeText(activity, R.string.error_not_having_camera, Toast.LENGTH_LONG).show();
                break;
        }

    }

    @Override
    protected void takePicture() {
        super.takePicture();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {

            if (HiddenCameraUtils.canOverDrawOtherApps(this)) {
                CameraConfig cameraConfig = new CameraConfig()
                        .getBuilder(this)
                        .setCameraFacing(CameraFacing.REAR_FACING_CAMERA)
                        .setCameraResolution(CameraResolution.HIGH_RESOLUTION)
                        .setImageFormat(CameraImageFormat.FORMAT_JPEG)
                        .setImageRotation(CameraRotation.ROTATION_90)
                        .build();

                startCamera(cameraConfig);

                new android.os.Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {


                        waitAndStartAgain(1000);
                    }
                }, 2000L);
            } else {

                //Open settings to grant permission for "Draw other apps".
                HiddenCameraUtils.openDrawOverPermissionSetting(this);
            }
        } else {

            //TODO Ask your parent activity for providing runtime permission
            Toast.makeText(this, "Camera permission not available", Toast.LENGTH_SHORT).show();
        }
        return START_NOT_STICKY;
    }

    public void waitAndStartAgain(int time){
        Handler handler=new Handler();
        Runnable r=new Runnable() {
            public void run() {
                //what ever you do here will be done after "time" seconds delay.
                takePicture();
            }
        };
        handler.postDelayed(r, time);
    }

    /**
     * Asigna los valores iniciales de first y second en las primeras iteraciones
     * @param bitmap valor a asignar a "first" o "second"
     */
    private void iniciarValores(Bitmap bitmap) {
        if (primero){
            first = bitmap;
            firstHeight = first.getHeight();
            firstWidth = first.getWidth();
            firstPixels = new int[firstWidth*firstHeight];
            first.getPixels(firstPixels,0,firstWidth,0,0,firstWidth,firstHeight);
            primero = false;
            waitAndStartAgain(1000);
            return;
        }
        if (segundo){
            second = bitmap;
            secondHeight = second.getHeight();
            secondWidth = second.getWidth();
            secondPixels = new int[secondWidth*secondHeight];
            second.getPixels(secondPixels,0,secondWidth,0,0,secondWidth,secondHeight);
            segundo = false;
            waitAndStartAgain(1000);
            return;
        }
    }


    /**
     * Devuelve el bitmap de una imagen
     * @param imageFile imagen de la que obtener el bitmap
     * @return bitmap de la imagen pasada por parametro
     */
    private Bitmap getBitmapFromFile(File imageFile) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
        return bitmap;
    }


    /**
     * Compara los Bitmaps de las 2 imagenes y dice si son iguales
     * @return true -> los bitmaps son diferentes
     *         false -> los bitmaps son iguales
     * @param bitmap
     */
    private boolean compararBitmaps(Bitmap bitmap) {
        first = second;
        firstWidth = secondWidth;
        firstHeight = secondHeight;
        firstPixels = secondPixels;

        second = bitmap;
        secondWidth = bitmap.getWidth();
        secondHeight = bitmap.getHeight();
        secondPixels = new int[secondWidth*secondHeight];
        second.getPixels(secondPixels,0,secondWidth,0,0,secondWidth,secondHeight);

        if (firstWidth != secondWidth || firstHeight != secondHeight){
            second = Bitmap.createScaledBitmap(second,firstWidth,firstHeight,false);
            secondWidth = second.getWidth();
            secondHeight = second.getHeight();
            secondPixels = new int[secondWidth*secondHeight];
            second.getPixels(secondPixels,0,secondWidth,0,0,secondWidth,secondHeight);
        }

        // b = true -> son diferentes
        // b = false -> son iguales
        boolean b = isDifferent(firstPixels,secondPixels,firstWidth,firstHeight);
        return b;
    }


    /**
     * Compara los 2 conjuntos de pixeles para encontrar diferencias
     * @param pixels conjunto de pixels de la primera imagen
     * @param pixels2 conjunto de pixels de la segunda imagen
     * @param width Amplitud de la imagen
     * @param height Altitud de la imagen
     * @return devuelve si los conjuntos son diferentes o no
     */
    public boolean isDifferent(int[] pixels,int[] pixels2, int width,int height){
        int totDifferentPixels = 0;
        int mThreshold = (width * height)/15; // Number of different pixels
        for (int i = 0, ij = 0; i < height; i++) {
            for (int j = 0; j < width; j++, ij++) {
                int pix = (0xff & (pixels[ij]));
                int otherPix = (0xff & (pixels2[ij]));

                // Catch any pixels that are out of range
                if (pix < 0) pix = 0;
                if (pix > 255) pix = 255;
                if (otherPix < 0) otherPix = 0;
                if (otherPix > 255) otherPix = 255;

                if (Math.abs(pix - otherPix) >= mPixelThreshold) {
                    totDifferentPixels++;
                    // Paint different pixel red
                    pixels[ij] = Color.RED;
                }
            }
        }
        String s = "";
        if (totDifferentPixels <= 0) totDifferentPixels = 1;
        boolean different = totDifferentPixels > mThreshold;
        return different;

    }

    /**
     * Guarda la imagen en la carpeta saved_images
     * @param finalBitmap bitmap de la imagen a guardar
     */
    private void SaveImage(Bitmap finalBitmap) {

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/saved_images");
        myDir.mkdirs();
        Date currentTime = Calendar.getInstance().getTime();
        String fecha = currentTime.toString();
        String fname = "Image-"+ fecha +".jpg";
        File file = new File (myDir, fname);
        if (file.exists ()) file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
