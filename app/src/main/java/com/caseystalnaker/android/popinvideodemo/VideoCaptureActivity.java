package com.caseystalnaker.android.popinvideodemo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.caseystalnaker.android.popinvideodemo.adapters.VideoMenuAdapter;
import com.caseystalnaker.android.popinvideodemo.fragments.Camera2VideoFragment;
import com.caseystalnaker.android.popinvideodemo.service.VideoThumbnailService;
import com.caseystalnaker.android.popinvideodemo.utils.Utils;


public class VideoCaptureActivity extends Activity {

    private static final String LOGTAG = VideoCaptureActivity.class.getSimpleName();

    private Context mContext;
    private RecyclerView mVideoCaptureMenu;
    private BroadcastReceiver mPreviewSavedVideoReceiver;
    private BroadcastReceiver mThumbnailSavedReceiver;
    private BroadcastReceiver mRequestVideoPlaybackReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOGTAG, "onCreate()");
        mContext = getBaseContext();
        setContentView(R.layout.video_capture_activity);

        //receiver for showing previews of saved videos
        mPreviewSavedVideoReceiver = new PreviewSavedVideoReceiver();
        //receiver for handling thumbnail creation
        mThumbnailSavedReceiver = new ThumbnailSavedReceiver();
        //receiver for handling video playback
        mRequestVideoPlaybackReceiver = new RequestVideoPlaybackReceiver();

        mVideoCaptureMenu = (RecyclerView) findViewById(R.id.video_capture_menu);
        mVideoCaptureMenu.setHasFixedSize(true);

        final RecyclerView.LayoutManager layoutMgr = new LinearLayoutManager(this);
        mVideoCaptureMenu.setLayoutManager(layoutMgr);

        final RecyclerView.Adapter videoMenuAdapter = new VideoMenuAdapter(mContext);
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

        LocalBroadcastManager.getInstance(this).registerReceiver(mThumbnailSavedReceiver,
                new IntentFilter(VideoThumbnailService.ACTION_THUMBNAIL_COMPLETE));

        LocalBroadcastManager.getInstance(this).registerReceiver(mRequestVideoPlaybackReceiver,
                new IntentFilter(Utils.REQUEST_VIDEO_PLAYBACK));

    }

    @Override
    public void onPause(){
        try {

            Log.d(LOGTAG, "onPause() unregistering for preview saved events.");
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mPreviewSavedVideoReceiver);
        }catch(IllegalArgumentException e){
            Log.d(LOGTAG, "Receiver mPreviewSavedVideoReceiver not registered");
        }

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mThumbnailSavedReceiver);
        }catch(IllegalArgumentException e){
            Log.d(LOGTAG, "Receiver mThumbnailSavedReceiver not registered");
        }

        super.onPause();
    }

    SharedPreferences.OnSharedPreferenceChangeListener mPrefsChangedListener = new
            SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                              String key) {
            Log.d(LOGTAG, "Prefs changed!!! + key " + key);
        }
    };

    public class PreviewSavedVideoReceiver extends BroadcastReceiver {
        private final String LOGTAG = PreviewSavedVideoReceiver.class.getSimpleName();

        public PreviewSavedVideoReceiver() {}

        @Override
        public void onReceive(final Context context, final Intent intent) {
            Log.d(LOGTAG, "onReceive");
            final String pathToVideo = intent.getStringExtra(Utils.PREVIEW_VIDEO_PATH_INTENT_KEY);

            //start IntentService to generate thumbnail of video
            Intent thumbnailIntent = new Intent(mContext, VideoThumbnailService.class);
            thumbnailIntent.setAction(VideoThumbnailService.ACTION_MAKE_THUMBNAIL);
            thumbnailIntent.putExtra(VideoThumbnailService.EXTRA_PATH_TO_VIDEO, pathToVideo);
            startService(thumbnailIntent);
        }
    }

    public class RequestVideoPlaybackReceiver extends BroadcastReceiver {

        public void onReceive(final Context context, final Intent intent) {

            Log.d(LOGTAG, "loading preview video: " + intent.getStringExtra(Utils.PREVIEW_VIDEO_PATH_INTENT_KEY));
            if(intent.getAction() == Utils.REQUEST_VIDEO_PLAYBACK) {
                Intent i = new Intent(getApplicationContext(), VideoPlaybackActivity.class);
                i.putExtra(Utils.PREVIEW_VIDEO_PATH_INTENT_KEY, intent.getStringExtra(Utils.PREVIEW_VIDEO_PATH_INTENT_KEY));
                startActivity(i);
            }
        }
    }

    public class ThumbnailSavedReceiver extends BroadcastReceiver {

        private final String LOGTAG = ThumbnailSavedReceiver.class.getSimpleName();

        @Override
        public void onReceive(final Context context, final Intent intent){
            Log.d(LOGTAG, "### thumbnail saved recvr");
            if(intent.getAction() == VideoThumbnailService.ACTION_THUMBNAIL_COMPLETE){

                String videoPath = intent.getStringExtra(VideoThumbnailService.EXTRA_PATH_TO_VIDEO);
                String thumbPath = intent.getStringExtra(VideoThumbnailService.EXTRA_PATH_TO_THUMBNAIL);

                if(videoPath!=null && thumbPath !=null){
                    //lets save these
                   // Toast.makeText(mContext, "Video saved to : " + videoPath + "\nThumbnailSaved to: " + thumbPath, Toast.LENGTH_LONG).show();
                    Log.d(LOGTAG, "Video path = " + videoPath + " thumbPath : " + thumbPath);
                    SharedPreferences prefs = getSharedPreferences(Utils.VIDEO_GALLERY_PREFS, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(videoPath, thumbPath);
                    editor.commit();

                    updateGallery();
                }
            }
        }
    }



    private void updateGallery(){
        ((VideoMenuAdapter)mVideoCaptureMenu.getAdapter()).swap();
    }
}
