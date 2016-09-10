package android.caseystalnaker.com.popinvideodemo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private static final String LOGTAG = MainActivity.class.getSimpleName();
    private ToggleButton mVideoToggleButton;
    private boolean mIsRecording = false;
    private PackageManager mPackageManager;
    private boolean mDeviceHasCameraFlag;

    private SquareLayout mVideoWrapper;

    private CameraManager mCameraManager;
    private CameraDevice mCamera;
    private TextureView mTextureView; //The view which will display our preview.
    private Surface previewSurface;  //The surface to which the preview will be drawn.
    private Size[] mSizes; //The sizes supported by the Camera. 1280x720, 1024x768, etc.  This must be set.
    private CaptureRequest.Builder mRequestBuilder;  //Builder to create a request for a camera capture.

    //TODO:Move to utils
    static final String mCameraId = "1";
    private static final int CAMERA_REQUEST = 1888;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        mPackageManager = getPackageManager();
        mDeviceHasCameraFlag = mPackageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA);

        setContentView(R.layout.activity_main);

        mVideoWrapper = (SquareLayout) findViewById(R.id.video_wrapper);
        mTextureView = (TextureView) findViewById(R.id.camera_preview);
        mVideoToggleButton = (ToggleButton) findViewById(R.id.video_toggle_button);

        mVideoToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (!mCameraManager.equals(null)) {
                    mIsRecording = !mIsRecording;
                    if (mIsRecording) {
                        //switch play button to stop button
                        mVideoToggleButton.setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.stop_btn));
                        //startVideoCapture();

                    } else {
                        mVideoToggleButton.setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.record_btn));
                        //stopVideoCapture();
                    }
                } else {
                   Log.e(LOGTAG, "Check Camera Manager. You should have never arrived here.");
                }
            }
        });
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.d(LOGTAG, "onResume()");

        //Need to check if camera is supported.
        if (mDeviceHasCameraFlag) {
            //Need to check for permission to access camera
            if (checkCameraPermissions()) {
                mTextureView.setSurfaceTextureListener(surfaceTextureListener);
            }
        }else{
            //No camera? alert user they are wasting their time.
            Toast.makeText(mContext, " You do not have a camera. Time for a new device.", Toast.LENGTH_LONG).show();
        }
    }

    private boolean checkCameraPermissions(){
        boolean isCameraAvailable = false;
        //check if permissions to access Camera are established already
        if(ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_REQUEST);
        }else{
            mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            isCameraAvailable =true;
        }
        Log.d(LOGTAG, "checkCameraPermissions() isCameraAvailable? " + isCameraAvailable);
        return isCameraAvailable;
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String permissions[], @NonNull final int[] grantResults) {
        Log.d(LOGTAG, "onRequestPermissionsResult()");
        switch (requestCode) {
            case CAMERA_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(LOGTAG, " Camera Permissions --> Granted. Setting surfaceTextureListener.");
                    // permission was granted
                    mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
                    /*
                     IMPORTANT - without this you will never get onSurfaceTextureAvailable()
                     callback if it is already available. Come on Google!!!
                     */
                    mTextureView.setSurfaceTextureListener(surfaceTextureListener);
                    if (mTextureView.isAvailable()) {
                        surfaceTextureListener.onSurfaceTextureAvailable(mTextureView.getSurfaceTexture(), mTextureView.getWidth(), mTextureView.getHeight());
                    }

                } else {
                    Toast.makeText(mContext, "Need permission to continue.", Toast.LENGTH_LONG).show();
                    // permission denied
                }
            }
        }
    }

    private void startVideoCapture(){


    }

    private void stopVideoCapture(){

    }

    /*
    Source: https://sites.google.com/site/averagelosercom/android/android-camera-api-v2-preview
     */
    private TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {

        /*The surface texture is available, so this is where we will create and open the camera, as
        well as create the request to start the camera preview.
         */
        @Override
        public void onSurfaceTextureAvailable(final SurfaceTexture surface, final int width, final int height) {
            previewSurface = new Surface(surface);

            mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            Log.d(LOGTAG, "onSurfaceTextureAvailable()");
            try {
                //The capabilities of the specified camera. On my Nexus 5, 1 is back camera.
                CameraCharacteristics characteristics =
                        mCameraManager.getCameraCharacteristics(mCameraId);

                /*
                A map that contains all the supported sizes and other information for the camera.
                Check the documentation for more information on what is available.
                 */
                StreamConfigurationMap streamConfigurationMap = characteristics.get(
                        CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                mSizes = streamConfigurationMap.getOutputSizes(SurfaceTexture.class);

                Log.d(LOGTAG, "onSurfaceTextureAvailable() --> Opening Camera.");
                /*
                Request that the manager open and create a camera object.
                cameraDeviceCallback.onOpened() is called now to do this.
                 */
                 mCameraManager.openCamera(mCameraId, cameraDeviceCallback, null);

            } catch (CameraAccessException e) {
                e.printStackTrace();
            } catch (SecurityException e){
                e.printStackTrace();
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            Log.d(LOGTAG, "onSurfaceTextureSizeChanged()");
            //updateTextureViewSize(mTextureView.getWidth(), mTextureView.getHeight());
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            Log.d(LOGTAG, "onSurfaceTextureDestroyed()");
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            //Log.d(LOGTAG, "onSurfaceTextureUpdated()");
        }
    };

    /**
     * Callbacks to notify us of the status of the Camera device.
     */
    CameraDevice.StateCallback cameraDeviceCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(final CameraDevice camera) {
            Log.d(LOGTAG, "CameraDevice.onOpened()");
            /*
            This where we create our capture session.  Our Camera is ready to go.
             */
            mCamera = camera;

            try {
                //Used to create the surface for the preview.
                SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();

                /*VERY IMPORTANT.  THIS MUST BE SET FOR THE APP TO WORK.  THE CAMERA NEEDS TO KNOW ITS PREVIEW SIZE.*/
                surfaceTexture.setDefaultBufferSize(mSizes[2].getWidth(), mSizes[2].getHeight());

                /*A list of surfaces to which we would like to receive the preview.  We can specify
                more than one.*/
                List<Surface> surfaces = new ArrayList<>();
                surfaces.add(previewSurface);

                /*We humbly forward a request for the camera.  We are telling it here the type of
                capture we would like to do.  In this case, a live preview.  I could just as well
                have been CameraDevice.TEMPLATE_STILL_CAPTURE to take a singe picture.  See the CameraDevice
                docs.*/
                mRequestBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                mRequestBuilder.addTarget(previewSurface);

                //A capture session is now created. The capture session is where the preview will start.
                camera.createCaptureSession(surfaces, cameraCaptureSessionStateCallback, new Handler());

            } catch (CameraAccessException e) {
                Log.e("Camera Exception", e.getMessage());
            }
        }

        @Override
        public void onDisconnected(final CameraDevice camera) {
            Log.d(LOGTAG, "CameraDevice.onDisconnected()");
        }

        @Override
        public void onError(final CameraDevice camera, final int error) {
            Log.d(LOGTAG, "CameraDevice.onError()");
        }
    };

    /**
     * The CameraCaptureSession.StateCallback class  This is where the preview request is set and started.
     */
    CameraCaptureSession.StateCallback cameraCaptureSessionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(final CameraCaptureSession session) {
            try {
                Log.d(LOGTAG, "CameraCaptureSession -- > Attempting Preview...");
                /* We humbly set a repeating request for images.  i.e. a preview. */
                session.setRepeatingRequest(mRequestBuilder.build(), cameraCaptureSessionCallback, new Handler());
            } catch (CameraAccessException e) {
                Log.e("Camera Exception", e.getMessage());
            }
        }

        @Override
        public void onConfigureFailed(final CameraCaptureSession session) {
            Log.d(LOGTAG, "CameraCaptureSession.onConfigureFailed() No preview for you!");
        }
    };

    private CameraCaptureSession.CaptureCallback cameraCaptureSessionCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureStarted(final CameraCaptureSession session, final CaptureRequest request, final long timestamp, final long frameNumber) {
            //Log.d(LOGTAG, "CameraCaptureSession.onCaptureStarted()");
            super.onCaptureStarted(session, request, timestamp, frameNumber);
        }

        @Override
        public void onCaptureCompleted(final CameraCaptureSession session, final CaptureRequest request, final TotalCaptureResult result) {
            //Log.d(LOGTAG, "CameraCaptureSession.onCaptureCompleted()");
            super.onCaptureCompleted(session, request, result);
        }

        @Override
        public void onCaptureFailed(final CameraCaptureSession session, final CaptureRequest request, final CaptureFailure failure) {
            Log.d(LOGTAG, "CameraCaptureSession.onCaptureFailed()");
            super.onCaptureFailed(session, request, failure);
        }
    };

    public void onPause() {
        super.onPause();

        if (mCamera != null) {
            mCamera.close();
        }
    }

    private void updateTextureViewSize(int viewWidth, int viewHeight) {
        mTextureView.setLayoutParams(new SquareLayout.LayoutParams(viewWidth, viewHeight));
    }
}
