package android.caseystalnaker.com.popinvideodemo;

import android.support.annotation.NonNull;
import android.Manifest;
import android.app.Activity;
import android.caseystalnaker.com.popinvideodemo.util.Util;
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
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public abstract class VideoTextureBaseActivity extends Activity {

    private final String LOGTAG = VideoTextureBaseActivity.class.getSimpleName();

    protected Context mContext;
    private PackageManager mPackageManager;
    protected boolean mDeviceHasCameraFlag;
    protected CameraManager mCameraManager;
    protected CameraDevice mCamera;
    protected TextureView mTextureView; //The view which will display our preview.
    protected Surface mPreviewSurface;  //The surface to which the preview will be drawn.
    protected Size[] mSizes; //The sizes supported by the Camera. 1280x720, 1024x768, etc.  This must be set.
    protected CaptureRequest.Builder mRequestBuilder;  //Builder to create a request for a camera capture.
    protected Util mUtils;

    //default
    private int mCameraActionRequestType = CameraDevice.TEMPLATE_PREVIEW;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(getContentView());
        mContext = getApplicationContext();
        mPackageManager = getPackageManager();
        mDeviceHasCameraFlag = mPackageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA);
        mUtils = Util.getInstance();

    }

    protected void startVideoCapture() {
        mCameraActionRequestType = CameraDevice.TEMPLATE_RECORD;
        if(mCameraManager!=null){
            //to prevent conflicts
            mCamera.close();
            try {
                mCameraManager.openCamera(mUtils.mCameraId, mCameraDeviceCallback, new Handler());
            }catch(SecurityException e){
                e.printStackTrace();
            }catch(CameraAccessException e){
                e.printStackTrace();
            }
        }
    }

    protected void stopVideoCapture() {
        mCamera.close();
        mCameraActionRequestType = CameraDevice.TEMPLATE_PREVIEW;
        mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        if (mTextureView.isAvailable()) {
            mSurfaceTextureListener.onSurfaceTextureAvailable(mTextureView.getSurfaceTexture(), mTextureView.getWidth(), mTextureView.getHeight());
        }
        //prompt to save or discard video
    }

    protected void takeVideoStillShot(CaptureRequest captureReq) {
        mCameraActionRequestType = CameraDevice.TEMPLATE_STILL_CAPTURE;
        try {
            mRequestBuilder = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
        }catch(SecurityException e){
            e.printStackTrace();
        }catch(CameraAccessException e){
            e.printStackTrace();
        }
        //mRequestBuilder.addTarget(previewSurface);
    }

    protected boolean checkCameraPermissions() {
        boolean isCameraAvailable = false;
        //check if permissions to access Camera are established already
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            //if not, request access at runtime
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    Util.CAMERA_REQUEST_ID);
        } else {
            //if so, no need to request permissions again.
            mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            isCameraAvailable = true;
        }
        Log.d(LOGTAG, "checkCameraPermissions() isCameraAvailable? " + isCameraAvailable);
        return isCameraAvailable;
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String permissions[], @NonNull final int[] grantResults) {
        Log.d(LOGTAG, "onRequestPermissionsResult()");
        if (requestCode == mUtils.CAMERA_REQUEST_ID) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(LOGTAG, " Camera Permissions --> Granted. Setting surfaceTextureListener.");
                // permission was granted
                mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
                /*
                 IMPORTANT - without this hack you will never get onSurfaceTextureAvailable()
                 callback if it is already available. Come on Google!!!
                 */
                mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
                if (mTextureView.isAvailable()) {
                    mSurfaceTextureListener.onSurfaceTextureAvailable(mTextureView.getSurfaceTexture(), mTextureView.getWidth(), mTextureView.getHeight());
                }

            } else {
                // permission denied
                Toast.makeText(mContext, getResources().getText(R.string.need_permission_to_continue), Toast.LENGTH_LONG).show();
            }
        }
    }


    /*
    Source: https://sites.google.com/site/averagelosercom/android/android-camera-api-v2-preview
     */
    protected TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {

        /*The surface texture is available, so this is where we will create and open the camera, as
        well as create the request to start the camera preview.
         */
        @Override
        public void onSurfaceTextureAvailable(final SurfaceTexture surface, final int width, final int height) {
            mPreviewSurface = new Surface(surface);

            mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            final String cameraID = mUtils.mCameraId;

            Log.d(LOGTAG, "onSurfaceTextureAvailable()");
            try {
                //The capabilities of the specified camera. On my Nexus 5, 1 is back camera.
                CameraCharacteristics characteristics =
                        mCameraManager.getCameraCharacteristics(cameraID);

                /*
                A map that contains all the supported sizes and other information for the camera.
                Check the documentation for more information on what is available.
                 */
                final StreamConfigurationMap streamConfigurationMap = characteristics.get(
                        CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                mSizes = streamConfigurationMap.getOutputSizes(SurfaceTexture.class);

                Log.d(LOGTAG, "onSurfaceTextureAvailable() --> Opening Camera.");
                /*
                Request that the manager open and create a camera object.
                cameraDeviceCallback.onOpened() is called now to do this.
                 */
                mCameraManager.openCamera(cameraID, mCameraDeviceCallback, null);

            } catch (CameraAccessException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
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
    CameraDevice.StateCallback mCameraDeviceCallback = new CameraDevice.StateCallback() {
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
                surfaces.add(mPreviewSurface);

                //mRequestBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                mRequestBuilder = camera.createCaptureRequest(mCameraActionRequestType);
                mRequestBuilder.addTarget(mPreviewSurface);

                //A capture session is now created. The capture session is where the preview will start.
                camera.createCaptureSession(surfaces, mCameraCaptureSessionStateCallback, new Handler());

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
    CameraCaptureSession.StateCallback mCameraCaptureSessionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(final CameraCaptureSession session) {
            try {
                Log.d(LOGTAG, "CameraCaptureSession -- > Attempting Preview...");
                /* We humbly set a repeating request for images.  i.e. a preview. */
                session.setRepeatingRequest(mRequestBuilder.build(), mCameraCaptureSessionCallback, new Handler());
            } catch (CameraAccessException e) {
                Log.e("Camera Exception", e.getMessage());
            }
        }

        @Override
        public void onConfigureFailed(final CameraCaptureSession session) {
            Log.d(LOGTAG, "CameraCaptureSession.onConfigureFailed() No preview for you!");
        }
    };

    private CameraCaptureSession.CaptureCallback mCameraCaptureSessionCallback = new CameraCaptureSession.CaptureCallback() {
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


    public abstract int getContentView();
}
