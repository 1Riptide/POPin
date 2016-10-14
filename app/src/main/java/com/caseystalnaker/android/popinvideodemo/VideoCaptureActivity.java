package com.caseystalnaker.android.popinvideodemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.caseystalnaker.android.popinvideodemo.adapters.VideoMenuCursorAdapter;
import com.caseystalnaker.android.popinvideodemo.data.SavedVideosContentProvider;
import com.caseystalnaker.android.popinvideodemo.fragments.Camera2VideoFragment;
import com.caseystalnaker.android.popinvideodemo.service.VideoThumbnailService;
import com.caseystalnaker.android.popinvideodemo.utils.Utils;

import java.util.Objects;


public class VideoCaptureActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor>  {

    private static final String LOGTAG = VideoCaptureActivity.class.getSimpleName();

    private Context mContext;
    private RecyclerView mVideoCaptureMenu;
    private BroadcastReceiver mPreviewSavedVideoReceiver;
    private BroadcastReceiver mThumbnailSavedReceiver;
    private BroadcastReceiver mRequestVideoPlaybackReceiver;
    private VideoMenuCursorAdapter mVideoMenuAdapter;

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


        mVideoMenuAdapter = new VideoMenuCursorAdapter(null);
        mVideoCaptureMenu.setAdapter(mVideoMenuAdapter);

        //Loader to retrieve all videos - when finished this will populate the mVideoCaptureMenu
        getSupportLoaderManager().initLoader(SavedVideosContentProvider.VIDEOS, null, this);

        //Need to check if camera is supported.
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            //do this only once
            if (null == savedInstanceState) {

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

        try {
            Log.d(LOGTAG, "onPause() unregistering for mRequestVideoPlaybackReceiver events.");
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mRequestVideoPlaybackReceiver);
        }catch(IllegalArgumentException e){
            Log.d(LOGTAG, "Receiver mRequestVideoPlaybackReceiver not registered");
        }
        super.onPause();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {
        /*
         * Takes action based on the ID of the Loader that's being created
        */
        Log.d(LOGTAG, "onCreateLoader() loaderId = " + loaderID);

        switch (loaderID) {
            case SavedVideosContentProvider.VIDEOS:
                String[] projection = new String[] { "*" };

                // Returns a new CursorLoader
                return new CursorLoader(
                        this,   // Parent activity context
                        SavedVideosContentProvider.ALL_VIDEOS_URI,        // Table to query
                        projection,     // Projection to return
                        null,           // No selection clause
                        null,           // No selection arguments
                        null            // Default sort order
                );
            default:
                // An invalid id was passed in
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        mVideoMenuAdapter.swapCursor(cursor);
        mVideoCaptureMenu.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                mVideoCaptureMenu.scrollToPosition(mVideoMenuAdapter.getItemCount() - 1);
            }
        });
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mVideoMenuAdapter.swapCursor(null);
    }

    public class PreviewSavedVideoReceiver extends BroadcastReceiver {
        private final String LOGTAG = PreviewSavedVideoReceiver.class.getSimpleName();

        public PreviewSavedVideoReceiver() {}

        @Override
        public void onReceive(final Context context, final Intent intent) {
            Log.d(LOGTAG, "onReceive");
            final String pathToVideo = intent.getStringExtra(Utils.PREVIEW_VIDEO_PATH_INTENT_KEY);

            //start IntentService to generate thumbnail of video and save references to db.
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
            if(Objects.equals(intent.getAction(), VideoThumbnailService.ACTION_THUMBNAIL_COMPLETE)){

                //Loader to retrieve all videos - when finished this will populate the mVideoCaptureMenu
                getSupportLoaderManager().restartLoader(SavedVideosContentProvider.VIDEOS, null, VideoCaptureActivity.this);
            }
        }
    }

}
