package android.caseystalnaker.com.popinvideodemo;

import android.hardware.camera2.CaptureRequest;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;

public class VideoCaptureActivity extends VideoTextureBaseActivity {

    private static final String LOGTAG = VideoCaptureActivity.class.getSimpleName();

    private SquareLayout mVideoWrapper;
    private ToggleButton mVideoToggleButton;

    private boolean mIsRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getContentView());

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
                        startVideoCapture();

                    } else {
                        mVideoToggleButton.setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.record_btn));
                        stopVideoCapture();
                    }
                } else {
                    Log.e(LOGTAG, "Check Camera Manager. You should have never arrived here.");
                }
            }
        });
    }

    @Override
    public int getContentView() {
        return R.layout.activity_main;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOGTAG, "onResume()");

        //Need to check if camera is supported.
        if (mDeviceHasCameraFlag) {
            //Need to check for permission to access camera
            if (checkCameraPermissions()) {
                mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
            }
        } else {
            //No camera? alert user they are wasting their time.
            Toast.makeText(mContext, getResources().getText(R.string.camera_device_unavailable), Toast.LENGTH_LONG).show();
        }
    }

    public void onPause() {
        super.onPause();

        if (mCamera != null) {
            mCamera.close();
        }
    }
}
