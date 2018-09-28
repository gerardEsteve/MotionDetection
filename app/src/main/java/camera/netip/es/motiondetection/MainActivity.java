package camera.netip.es.motiondetection;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;

import camera.netip.es.motiondetection.config.CameraFacing;
import camera.netip.es.motiondetection.config.CameraImageFormat;
import camera.netip.es.motiondetection.config.CameraResolution;
import camera.netip.es.motiondetection.config.CameraRotation;

public class MainActivity extends HiddenCameraActivity {

    // configuracion de la camara
    private static SurfaceView preview = null;
    private static Camera camera = null;
    private CameraConfig mCameraConfig;
    // booleans para detectar las 2 primeras imagenes
    boolean primero,segundo;
    Button but1,but2,but3,but4; //PETA

    public int firstHeight, firstWidth, secondHeight, secondWidth;
    public int[] firstPixels, secondPixels;

    // codigos para el Intent
    public static final int PICK_IMAGE = 1;
    public static final int PICK_IMAGE2 = 2;
    // bitmaps para almacenar las imagenes a comparar
    Bitmap first,second;

    MainService mainService;

    TextView textView; //PETA
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      //  setContentView(R.layout.activity_main);
        Intent i= new Intent(MainActivity.this, MainService.class);
        MainActivity.this.startService(i);
        finish();
        MainActivity.this.bindService(i, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MainService.MyBinder myBinder = (MainService.MyBinder) service;
                mainService = myBinder.getService();
                mainService.passContext(MainActivity.this);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        }, Context.BIND_AUTO_CREATE);

     //   startService(new Intent(MainActivity.this, MainService.class));
/*
        but1 = (Button)findViewById(R.id.but1);
        but2 = (Button)findViewById(R.id.but2);
        but3 = (Button)findViewById(R.id.but3);
        but4 = (Button)findViewById(R.id.but4);
        textView = (TextView)findViewById(R.id.text);

        //Setting camera configuration
        mCameraConfig = new CameraConfig()
                .getBuilder(this)
                .setCameraFacing(CameraFacing.REAR_FACING_CAMERA)
                .setCameraResolution(CameraResolution.HIGH_RESOLUTION)
                .setImageFormat(CameraImageFormat.FORMAT_JPEG)
                .setImageRotation(CameraRotation.ROTATION_90)
                .build();
        //Check for the camera permission for the runtime
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            //Start camera preview
            startCamera(mCameraConfig);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 101);
        }

        findViewById(R.id.but4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePicture();
            }
        });
*/

       // primero = segundo = false;
    /*      primero = segundo = true;
          final Handler handler = new Handler();
          handler.postDelayed(new Runnable() {
              @Override
              public void run() {
                  //Do something after 100ms
                  takePicture();
                  handler.postDelayed(this,1000);
              }
           }, 1000);*/

    /*     but1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
            }
        });
        but2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE2);
            }
        });

        but3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ( first!= null && second != null){
                    int width = first.getWidth();
                    int heigth = first.getHeight();
                    int width2 = second.getWidth();
                    int heigth2 = second.getHeight();

                    if (width != width2 || heigth != heigth2){
                        second = Bitmap.createScaledBitmap(second,width,heigth,false);
                    }
                    int[] pixels = new int[width*heigth];
                    first.getPixels(pixels,0,width,0,0,width,heigth);

                    width2 = second.getWidth();
                    heigth2 = second.getHeight();
                    int[] pixels2 = new int[width2*heigth2];
                    second.getPixels(pixels2,0,width2,0,0,width2,heigth2);

                    // b = true -> son diferentes
                    // b = false -> son iguales
                    boolean b = isDifferent(pixels,pixels2,width,heigth);
                    if (b ) textView.setText("Imagenes diferentes");
                    else textView.setText("Imagenes iguales");

                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //Do something after 100ms
                            textView.setText("Sin comparar");
                        }
                    }, 3000);

                    String s = "";
                    first = second = null;
                }
            }
        });*/

    }

    // Specific settings
    private static final int mPixelThreshold = 50; // Difference in pixel (RGB)

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
/*
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == PICK_IMAGE) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                first = BitmapFactory.decodeStream(imageStream);
                String s = "";
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Toast.makeText(this,"picked first image!",Toast.LENGTH_SHORT).show();
        }
        else if (requestCode == PICK_IMAGE2){
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                second = BitmapFactory.decodeStream(imageStream);
                String s = "";
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Toast.makeText(this,"picked second image!",Toast.LENGTH_SHORT).show();
        }
    }
*/
    /**
     * Callback de cuando se captura una imagen
     * @param imageFile imagen capturada
     */
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
            but1.setBackgroundColor(Color.DKGRAY);
            but2.setBackgroundColor(Color.DKGRAY);
            but3.setBackgroundColor(Color.DKGRAY);
            but4.setBackgroundColor(Color.DKGRAY);
            textView.setText("Imagenes diferentes");
            //TODO añadir accion de movimiento detectado

        }
        else{
            // Las 2 imagenes comparadas son iguales, con lo que
            // no ha habido movimientos detectados por la camara
            but1.setBackgroundColor(Color.BLUE);
            but2.setBackgroundColor(Color.BLUE);
            but3.setBackgroundColor(Color.BLUE);
            but4.setBackgroundColor(Color.BLUE);
            textView.setText("Imagenes iguales");
            tmp = "son iguales";
            // TODO añadir accion de imagenes iguales

        }
        Log.i("Image Captured3", "comparacion : " + tmp);

        //PETA esto sobra
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                textView.setText("Sin comparar");
            }
        }, 3000);

        // Guardamos la imagen en la carpeta saved_images
        SaveImage(bitmap);

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
            return;
        }
        if (segundo){
            second = bitmap;
            secondHeight = second.getHeight();
            secondWidth = second.getWidth();
            secondPixels = new int[secondWidth*secondHeight];
            second.getPixels(secondPixels,0,secondWidth,0,0,secondWidth,secondHeight);
            segundo = false;
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
     * Función para gestionar si hay algun error con la camara
     * @param errorCode codigo del error producido por la camara
     */
    @Override
    public void onCameraError(int errorCode) {
        switch (errorCode) {
            case CameraError.ERROR_CAMERA_OPEN_FAILED:
                //Camera open failed. Probably because another application
                //is using the camera
                Toast.makeText(this, R.string.error_cannot_open, Toast.LENGTH_LONG).show();
                break;
            case CameraError.ERROR_IMAGE_WRITE_FAILED:
                //Image write failed. Please check if you have provided WRITE_EXTERNAL_STORAGE permission
                Toast.makeText(this, R.string.error_cannot_write, Toast.LENGTH_LONG).show();
                break;
            case CameraError.ERROR_CAMERA_PERMISSION_NOT_AVAILABLE:
                //camera permission is not available
                //Ask for the camera permission before initializing it.
                Toast.makeText(this, R.string.error_cannot_get_permission, Toast.LENGTH_LONG).show();
                break;
            case CameraError.ERROR_DOES_NOT_HAVE_OVERDRAW_PERMISSION:
                //Display information dialog to the user with steps to grant "Draw over other app"
                //permission for the app.
                HiddenCameraUtils.openDrawOverPermissionSetting(this);
                break;
            case CameraError.ERROR_DOES_NOT_HAVE_FRONT_CAMERA:
                Toast.makeText(this, R.string.error_not_having_camera, Toast.LENGTH_LONG).show();
                break;
        }
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
