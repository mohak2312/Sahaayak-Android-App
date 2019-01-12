//unique package
package obd.edu.pdx.mohak.ece558.object_detection;

//built in library
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;

//library for camera2 api
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.view.Surface;
import android.view.TextureView;
import android.os.Bundle;

//library for handler, thread
import android.os.Handler;
import android.os.HandlerThread;

//library for speech synthesis
import android.speech.tts.TextToSpeech;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;

//library for touch event
import android.view.MotionEvent;

import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetector;
import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetectorOptions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Camera preview and object detection
 * label of detected object is displayed on camera preview
 * on screen touch it will speech out the label name
 */
public class CameraActivity extends AppCompatActivity {


    //variable
    private TextureView textureView;
    private CameraManager cameraManager;
    private static final String TAG="Mark";
    private Size  previewSize;
    private String cameraId;
    private Size [] outputsize;
    private FirebaseVisionImage image;

    //variable for handler
    private Handler backgroundHandler;
    private HandlerThread backgroundThread;
    int i=0;
    private Handler mHandler = new Handler();

    //variable for camera
    private GraphicOverlay mGraphicOverlay;
    private CameraDevice cameraDevice;
    private CaptureRequest.Builder captureRequestBuilder;
    private CaptureRequest captureRequest;
    private TextureView.SurfaceTextureListener surfaceTextureListener;
    private CameraCaptureSession cameraCaptureSession;

    //variable for text to speech
    TextToSpeech t1;
    final List <String> toplables = new ArrayList<String>();

    /**
     * camera listener from background thread
     */
    CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {

        /**
         * Create camera preview
         * @param cameraDevice
         */
        @Override
        public void onOpened(CameraDevice cameraDevice) {
            CameraActivity.this.cameraDevice = cameraDevice;
            createPreviewSession();
        }

        /**
         * close the camera preview
         * @param cameraDevice
         */
        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            cameraDevice.close();
            CameraActivity.this.cameraDevice = null;
        }

        /**
         * display the camera2 api error
         * @param cameraDevice
         * @param error
         */
        @Override
        public void onError(CameraDevice cameraDevice, int error) {
            cameraDevice.close();
            CameraActivity.this.cameraDevice = null;
        }
    };


    /**
     *  OnCreate is called when the quiz_activity
     *  is started.
     *  super.xxx method whenever we override a method
     * @param savedInstanceState
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // calls the super.onCreate() method and inflates
        // the layout with an ID of R.id.layout
        //Hide the system status bar
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera);

        //create an object
        // wiring the widget
        textureView=findViewById(R.id.texture_view);
        mGraphicOverlay = findViewById(R.id.graphic_overlay);
        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);

        //cameraFacing = CameraCharacteristics.LENS_FACING_BACK;
        Log.d(TAG, "------------------Camera Start--------------------- ");

        //listner for text to speech
        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {

            /**
             * check the status of text to speech
             * and set the language
             * @param status
             */
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.ENGLISH);
                }
            }
        });


        //listner for texureview
        surfaceTextureListener = new TextureView.SurfaceTextureListener() {

            /**
             * Setup up the camera2 api and oen the camera
             * @param surfaceTexture
             * @param width
             * @param height
             */
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
                setUpCamera();
                openCamera();
            }

            /**
             * detect the any changes in size of textureview
             * @param surfaceTexture
             * @param width
             * @param height
             */
            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {

            }

            /**
             * destroy the textureview surface
             * @param surfaceTexture
             * @return
             */
            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                return false;
            }

            /**
             * update textureview
             * @param surfaceTexture
             */
            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

            }
        };

        //toast when camera activity start
        // which is for user guideline
        // how to use speech feature
        Toast.makeText(CameraActivity.this,
                " Touch to speak out the label name",
                Toast.LENGTH_LONG).show();

        //wiring the layout
        // listner for any on touch event
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.layout);
        layout.setOnTouchListener(new View.OnTouchListener() {

            /**
             * if any on touch event occur
             * speak out the label name
             * @param v
             * @param event
             * @return
             */
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                speak();
                return false;
            }
        });

        //handler
        mHandler.post(mDrawRunnable);
    }

    /**
     * first get the detected object lablel
     * and set the pithch and speed
     * and speak out the label name
     */
    private void speak() {
        String toSpeak = toplables.get(0);
        t1.setPitch(1.3f);
        t1.setSpeechRate(0.8f);
        t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null,null);
    }

    /**
     * onPause method
     * to stop the text t speech
     */

    @Override
    public void onPause(){
        if(t1 !=null){
            t1.stop();
            t1.shutdown();
        }
        super.onPause();
    }

    /**
     * Runnable mathod for object detection
     * using firebase ML kit
     * and overlay the label name on camera preview
     */
    private Runnable mDrawRunnable = new Runnable() {

        @Override
        public void run() {
            if(textureView.isAvailable()) {
                List<String>label=new ArrayList<String>();
                List<String>label_1=new ArrayList<String>();

                //capture the bitmap image from textureview
                //and send it to firebasevision image
                // detect the object
                Bitmap bitmap = textureView.getBitmap();
                FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
                label_1=labelImages(image);
                if(!label_1.isEmpty()) {
                    label.add(label_1.get(0));

                    //overlay the detected object label
                    mGraphicOverlay.clear();
                    GraphicOverlay.Graphic labelGraphic = new LabelGraphic(mGraphicOverlay,label );
                    mGraphicOverlay.add(labelGraphic);

                }
            }
            //post delay for 100 miliseconds
            // so that it will run smoothly in real-time
            mHandler.postDelayed(mDrawRunnable,100);

        }

    };

    /**
     * Setup back camera for preview
     */
    private void setUpCamera() {
        try {
            for (String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics cameraCharacteristics =
                        cameraManager.getCameraCharacteristics(cameraId);
                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) ==
                        CameraCharacteristics.LENS_FACING_BACK) {
                    StreamConfigurationMap streamConfigurationMap = cameraCharacteristics.get(
                            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                    //get the default height and weight of mobile screen
                    // And optimize the camera preview accordingly
                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                    int height = displayMetrics.heightPixels;
                    int width = displayMetrics.widthPixels;
                    outputsize = streamConfigurationMap.getOutputSizes(SurfaceTexture.class);
                    previewSize= chooseOptimalSize(outputsize,width,height);
                    this.cameraId = cameraId;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * invoke the background thread
     * setup the camera nd open it
     */
    @Override
    protected void onResume() {
        super.onResume();
        openBackgroundThread();
        if (textureView.isAvailable()) {
            setUpCamera();
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(surfaceTextureListener);
        }
    }

    /**
     * Close the camera
     */
    @Override
    protected void onStop() {
        super.onStop();
        closeCamera();
        closeBackgroundThread();
    }

    /**
     * close the camera session if it is null
     */
    private void closeCamera() {
        if (cameraCaptureSession != null) {
            cameraCaptureSession.close();
            cameraCaptureSession = null;
        }

        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }

    /**
     * stop the background thread
     */
    private void closeBackgroundThread() {
        if (backgroundHandler != null) {
            backgroundThread.quitSafely();
            backgroundThread = null;
            backgroundHandler = null;
        }
    }

    /**
     * Create the camera preview seassion for realtime
     * object detection
     */
    private void createPreviewSession() {
        try {
            SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
            Surface previewSurface = new Surface(surfaceTexture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(previewSurface);

            cameraDevice.createCaptureSession(Collections.singletonList(previewSurface),
                    new CameraCaptureSession.StateCallback() {
                        /**
                         * capture camera session in background thread
                         * @param cameraCaptureSession
                         */

                        @Override
                        public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                            if (cameraDevice == null) {
                                return;
                            }

                            try {
                                captureRequest = captureRequestBuilder.build();
                                CameraActivity.this.cameraCaptureSession = cameraCaptureSession;
                                CameraActivity.this.cameraCaptureSession.setRepeatingRequest(captureRequest,
                                        null, backgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {

                        }
                    }, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * open camera2 api and check for the permisson
     */
    private void openCamera() {
            try {
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED) {
                    cameraManager.openCamera(cameraId, stateCallback, backgroundHandler);
                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

    /**
     * background thread handler
     */
    private void openBackgroundThread() {
            backgroundThread = new HandlerThread("camera_background_thread");
            backgroundThread.start();
            backgroundHandler = new Handler(backgroundThread.getLooper());
        }

    /**
     * optimize camera preview for realtime object detection
     * so that it will not get compressed
     * @param outputSizes
     * @param width
     * @param height
     * @return
     */
    private Size chooseOptimalSize(Size[] outputSizes, int width, int height) {
        double preferredRatio = height / (double) width;
        Size currentOptimalSize = outputSizes[0];
        double currentOptimalRatio = currentOptimalSize.getWidth() / (double) currentOptimalSize.getHeight();
        for (Size currentSize : outputSizes) {
            double currentRatio = currentSize.getWidth() / (double) currentSize.getHeight();
            if (Math.abs(preferredRatio - currentRatio) <
                    Math.abs(preferredRatio - currentOptimalRatio)) {
                currentOptimalSize = currentSize;
                currentOptimalRatio = currentRatio;
            }
        }
        return currentOptimalSize;
    }


    /**
     * object detection in image
     * and get the label for detected object
     * @param image
     * @return
     */
    private List<String> labelImages(FirebaseVisionImage image) {


        //create the FirebaseLabelDetector object for the label detection of the object
        // set confidence 0.8
        FirebaseVisionLabelDetectorOptions options =
                new FirebaseVisionLabelDetectorOptions.Builder()
                        .setConfidenceThreshold(0.80f)
                        .build();

        //we create the FirebaseVisionImage object to read the bitmap image
        FirebaseVisionLabelDetector detector = FirebaseVision.getInstance()
                .getVisionLabelDetector(options);

        //detectInImage method to get the label for object
        Task<List<FirebaseVisionLabel>> result =
                detector.detectInImage(image)
                        .addOnSuccessListener(
                                new OnSuccessListener<List<FirebaseVisionLabel>>() {
                                    @Override
                                    public void onSuccess(List<FirebaseVisionLabel> labels) {

                                        int j =0;
                                       for (FirebaseVisionLabel label: labels) {

                                            String text = label.getLabel();

                                            if(j<1) {

                                                toplables.clear();
                                                toplables.add(text);
                                                List<String>label_1=new ArrayList<String>();
                                                label_1.add(text);

                                                mGraphicOverlay.clear();
                                                GraphicOverlay.Graphic labelGraphic = new LabelGraphic(mGraphicOverlay, label_1);
                                                mGraphicOverlay.add(labelGraphic);
                                                j++;
                                           }
                                        }
                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        Toast.makeText(CameraActivity.this,
                                                " Fail to detect",
                                                Toast.LENGTH_SHORT).show();

                                    }
                                });
      return toplables;

    }

    /**
     * Go to login activity if user pressed the back button
     */

    @Override
    public void onBackPressed(){
        Intent i = new Intent(CameraActivity.this,loginActivity.class );
        startActivity(i);
    }

}
