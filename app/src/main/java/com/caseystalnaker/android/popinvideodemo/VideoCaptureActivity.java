package com.caseystalnaker.android.popinvideodemo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.caseystalnaker.android.popinvideodemo.adapters.VideoMenuAdapter;
import com.caseystalnaker.android.popinvideodemo.fragments.Camera2VideoFragment;
import com.caseystalnaker.android.popinvideodemo.utils.Utils;

import java.util.ArrayList;

public class VideoCaptureActivity extends Activity {

    private static final String LOGTAG = VideoCaptureActivity.class.getSimpleName();

    private Context mContext;
    private RecyclerView mVideoCaptureMenu;
    private BroadcastReceiver mPreviewSavedVideoReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOGTAG, "onCreate()");
        mContext = getBaseContext();
        setContentView(R.layout.video_capture_activity);

        //receiver for showing previews of saved videos
        mPreviewSavedVideoReceiver = new PreviewSavedVideoReceiver();


        mVideoCaptureMenu = (RecyclerView) findViewById(R.id.video_capture_menu);
        mVideoCaptureMenu.setHasFixedSize(true);

        final RecyclerView.LayoutManager layoutMgr = new LinearLayoutManager(this);
        mVideoCaptureMenu.setLayoutManager(layoutMgr);

        //fake it till you make it - this is some dummy data for use in the video gallery component.
        final String[] values = new String[]{"Video 1", "Video 2", "Video 3", "Video 4", "Video 5"};
        ArrayList<Bitmap> thumbnails = new ArrayList<>(values.length);

        //this will ultimatly be from from storage.
        final int thumbTitlesLength = values.length;
        for (int i = 0; i < thumbTitlesLength; i++) {
            /* I am purposely converting drawables into bitmaps here. I did this so that I could allow the
             * adapter to do the work of converting video thumbnails (bitmaps) back into drawables for use
             * in the RecyclerView. Consider it stub code, ready for real data.
             */
            thumbnails.add(BitmapFactory.decodeResource(mContext.getResources(),
                    R.drawable.video_thumb_placeholder));
        }

        final RecyclerView.Adapter videoMenuAdapter = new VideoMenuAdapter(mContext, values, thumbnails);
        mVideoCaptureMenu.setAdapter(videoMenuAdapter);

        //Need to check if camera is supported.
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            //do this only once
            if (null == savedInstanceState) {

                /*The Camera2VideoFragment is where most of the heavy lifting occurs.
                See https://github.com/googlesamples/android-Camera2Video

                This is google work which has its issues (which is to say, does not work right out of the box,
                but it was useful once I smoothed it out a bit.
                 */
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, Camera2VideoFragment.newInstance())
                        .commit();
            }
        } else {
            //No camera? alert user they are wasting their time.
            Toast.makeText(mContext, getResources().getText(R.string.camera_device_unavailable), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.d(LOGTAG, "onResume() registering for preview saved events.");
        LocalBroadcastManager.getInstance(this).registerReceiver(mPreviewSavedVideoReceiver,
                new IntentFilter(Utils.PREVIEW_VIDEO_COMPLETE_INTENT));
    }

    @Override
    public void onPause(){
        try {

            Log.d(LOGTAG, "onPause() unregistering for preview saved events.");
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mPreviewSavedVideoReceiver);
        }catch(IllegalArgumentException e){
            Log.d(LOGTAG, "Receiver mPreviewSavedVideoReceiver not registered");
        }
        super.onPause();
    }


    public class PreviewSavedVideoReceiver extends BroadcastReceiver {
        private final String LOGTAG = PreviewSavedVideoReceiver.class.getSimpleName();

        public PreviewSavedVideoReceiver() {}

        @Override
        public void onReceive(final Context context, final Intent intent) {
            Log.d(LOGTAG, "onReceive");
            final String pathToVideo = intent.getStringExtra(Utils.PREVIEW_VIDEO_PATH_INTENT_KEY);
            //relay intent data into video preview activity. Quick and dirty.
            Intent i = new Intent(getApplicationContext(), VideoPlaybackActivity.class);
            i.putExtra(Utils.PREVIEW_VIDEO_PATH_INTENT_KEY, pathToVideo);
            startActivity(i);
        }
    }

}
