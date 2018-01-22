package tr.org.lotoSorgula;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.graphics.BitmapCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.nio.ByteBuffer;

public class LotoSorgula extends AppCompatActivity implements SurfaceHolder.Callback{

    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 100;
    private static final int CAMERA_REQUEST = 1;
    private static final int RESULT_LOAD_IMAGE = 2;

//    private ImageView imageView;
    private Button btnCapture;
    private Button btnGallery;
    private String sonuc;
    private EditText width;
    private EditText height;

    private FrameLayout progressBarHolder;
    private AlphaAnimation inAnimation;
    private AlphaAnimation outAnimation;

    private android.hardware.Camera camera;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;

    private Camera.PictureCallback rawCallback;
    private Camera.ShutterCallback shutterCallback;
    private Camera.PictureCallback jpegCallback;

    private boolean previewRunning;

    public AlphaAnimation getInAnimation() {
        return inAnimation;
    }

    public void setInAnimation(AlphaAnimation inAnimation) {
        this.inAnimation = inAnimation;
    }

    public AlphaAnimation getOutAnimation() {
        return outAnimation;
    }

    public void setOutAnimation(AlphaAnimation outAnimation) {
        this.outAnimation = outAnimation;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loto_sorgula);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_HOME_AS_UP);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setLogo(R.drawable.baslik);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                MY_PERMISSIONS_REQUEST_CAMERA);

//        imageView = (ImageView)findViewById(R.id.image);

        surfaceView = (SurfaceView) findViewById(R.id.surfaceCamera);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        rawCallback = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, android.hardware.Camera camera) {
                Log.d("Log", "onPictureTaken - raw");
            }
        };

        shutterCallback = new Camera.ShutterCallback() {
            public void onShutter() {
                Log.i("Log", "onShutter'd");
            }
        };

        jpegCallback = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, android.hardware.Camera camera) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                byte[] array = null;
                byte[] tempArray = null;
                try {
                    if(width.getText() != null && !width.getText().toString().isEmpty() && height.getText() != null && !height.getText().toString().isEmpty()) {
                        bitmap = Bitmap.createScaledBitmap(bitmap, new Integer(width.getText().toString()), new Integer(height.getText().toString()), true);
                    }
                    bitmap = toGrayscale(bitmap);
//            int size = bitmap.getByteCount();
                    ByteBuffer buffer = ByteBuffer.allocate(BitmapCompat.getAllocationByteCount(bitmap));
                    bitmap.copyPixelsToBuffer(buffer);
                    array = buffer.array();
                    tempArray = new byte[array.length/4];
                    int count = 0;
                    for(int i = 0 ; i < array.length ; i ++){
                        if(i % 4 == 0){
                            tempArray[count] = array[i];
                            count++;
                        }
                    }
//            imageView.setImageBitmap(bitmap);
                } catch (Exception e){
                    Log.d("Ex : ", e.getMessage());
                }
                sendToService(tempArray);
//                FileOutputStream outStream = null;
//                try {
//                    outStream = new FileOutputStream(String.format(
//                            "/sdcard/%d.jpg", System.currentTimeMillis()));
//                    outStream.write(data);
//                    outStream.close();
//                    Log.d("Log", "onPictureTaken - wrote bytes: " + data.length);
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } finally {
//                }
//                Log.d("Log", "onPictureTaken - jpeg");
            }
        };

        width = (EditText)findViewById(R.id.width);
        height = (EditText)findViewById(R.id.height);

        btnCapture = (Button) findViewById(R.id.btnCapture);
        btnCapture.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                captureImage();
//                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });

        btnGallery = (Button) findViewById(R.id.btnGallery);
        btnGallery.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,
                        "Select Picture"), RESULT_LOAD_IMAGE);
            }
        });

        progressBarHolder = (FrameLayout) findViewById(R.id.progressBarHolder);
        progressBarHolder.setVisibility(View.GONE);

        // Example of a call to a native method
//        TextView tv = (TextView) findViewById(R.id.sample_text);
//        tv.setText(stringFromJNI());
    }

    private void captureImage() {
        camera.takePicture(shutterCallback, rawCallback, jpegCallback);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            sendToService(imageToByteArray(data));
        }
        else if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            sendToService(imageToByteArray(data));
        }
    }

    public void sendToService(byte[] array){
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        inAnimation = new AlphaAnimation(0f, 1f);
        inAnimation.setDuration(200);
        progressBarHolder.setAnimation(inAnimation);
        progressBarHolder.setVisibility(View.VISIBLE);

        SendPostRequest spq = new SendPostRequest();
        spq.setClass(this);
        spq.setData(array);
        spq.execute();
    }

    public  byte[] imageToByteArray(Intent data){
        byte[] array = null;
        byte[] tempArray = null;
        try {
            Uri imageUri = data.getData();
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            if(width.getText() != null && !width.getText().toString().isEmpty() && height.getText() != null && !height.getText().toString().isEmpty()) {
                bitmap = Bitmap.createScaledBitmap(bitmap, new Integer(width.getText().toString()), new Integer(height.getText().toString()), true);
            }
            bitmap = toGrayscale(bitmap);
            ByteBuffer buffer = ByteBuffer.allocate(BitmapCompat.getAllocationByteCount(bitmap));
            bitmap.copyPixelsToBuffer(buffer);
            array = buffer.array();
            tempArray = new byte[array.length/4];
            int count = 0;
            for(int i = 0 ; i < array.length ; i ++){
                if(i % 4 == 0){
                    tempArray[count] = array[i];
                    count++;
                }
            }
//            imageView.setImageBitmap(bitmap);
        } catch (Exception e){
            Log.d("Ex : ", e.getMessage());
        }
        return tempArray;
    }

    public Bitmap toGrayscale(Bitmap bmpOriginal) {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        camera = camera.open();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (previewRunning) {
            camera.stopPreview();
        }
        android.hardware.Camera.Parameters p = camera.getParameters();
        p.setPreviewSize(width, height);
        camera.setParameters(p);

        try {
            camera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }

        camera.startPreview();
        previewRunning = true;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.stopPreview();
        previewRunning = false;
        camera.release();
        camera = null;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
