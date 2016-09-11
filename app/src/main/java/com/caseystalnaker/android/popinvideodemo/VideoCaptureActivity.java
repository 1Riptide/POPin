package com.caseystalnaker.android.popinvideodemo;

import com.caseystalnaker.android.popinvideodemo.adapters.VideoMenuAdapter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;

public class VideoCaptureActivity extends VideoTextureBaseActivity {

    private static final String LOGTAG = VideoCaptureActivity.class.getSimpleName();

    private SquareLayout mVideoWrapper;
    private RecyclerView mVideoCaptureMenu;
    private ToggleButton mVideoToggleButton;

    private boolean mIsRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getContentView());

        mVideoWrapper = (SquareLayout) findViewById(R.id.video_wrapper);
        mTextureView = (TextureView) findViewById(R.id.camera_preview);
        mVideoToggleButton = (ToggleButton) findViewById(R.id.video_toggle_button);
        mVideoCaptureMenu = (RecyclerView) findViewById(R.id.video_capture_menu);
        mVideoCaptureMenu.setHasFixedSize(true);

        final RecyclerView.LayoutManager layoutMgr = new LinearLayoutManager(this);
        mVideoCaptureMenu.setLayoutManager(layoutMgr);

        //fake it till you make it
        String[] values = new String[] {"Video 1", "Video 2", "Video 3",
                "Video 4", "Video 5"};

        ArrayList<Bitmap> thumbnails = new ArrayList<>();

        //this will ultimatly be from from storage.
        final int thumbTitlesLength = values.length;
        for (int i = 0; i < thumbTitlesLength; i++) {
           thumbnails.add(BitmapFactory.decodeResource(mContext.getResources(),
                   R.drawable.video_thumb_placeholder));
        }

        final RecyclerView.Adapter videoMenuAdapter = new VideoMenuAdapter(mContext, values, thumbnails);
        mVideoCaptureMenu.setAdapter(videoMenuAdapter);

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
                        //switch back to record button
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
        return R.layout.video_capture_activity;
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
