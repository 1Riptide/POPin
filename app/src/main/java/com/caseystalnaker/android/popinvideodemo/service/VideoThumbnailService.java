package com.caseystalnaker.android.popinvideodemo.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.util.Random;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */

public class VideoThumbnailService extends IntentService {

    public static final String LOGTAG = VideoThumbnailService.class.getSimpleName();

    public static final String ACTION_MAKE_THUMBNAIL = "com.caseystalnaker.android.popinvideodemo.service.action.MAKE_THUMB";
    public static final String ACTION_THUMBNAIL_COMPLETE = "com.caseystalnaker.android.popinvideodemo.service.action.THUMB_COMPLETE";
    public static final String EXTRA_PATH_TO_VIDEO = "com.caseystalnaker.android.popinvideodemo.service.extra.PATH_TO_VIDEO";
    public static final String EXTRA_PATH_TO_THUMBNAIL = "com.caseystalnaker.android.popinvideodemo.service.extra.PATH_TO_THUMBNAIL";
    //private static final String THUMBNAIL_DIRECTORY = "/storage/emulated/0/Android/data/com.caseystalnaker.android.popinvideodemo/files/thumbnail_images/";
    private static String mThumbnailDirectory;
    private static final String THUMBNAIL_FOLDER = "thumbnails/";
    private static final String THUMBNAIL_PREFIX = "Thumbnail-";
    private static final int mThumbnailwidth = 200;

    public VideoThumbnailService() {
        super("VideoThumbnailService");
    }

    public static Bitmap startActionMakeThumbnail(Context context, String pathToVideo) {
        Log.d(LOGTAG, "startActionMakeThumbnail() pathToVideo : " + pathToVideo);
        Bitmap bitmap = null;
        FileDescriptor fd = new FileDescriptor();
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            if (pathToVideo != null) {
                retriever.setDataSource(pathToVideo);
            } else {
                retriever.setDataSource(fd);
            }
            bitmap = retriever.getFrameAtTime(-1);
        } catch (IllegalArgumentException ex) {
            // Assume this is a corrupt video file
        } catch (RuntimeException ex) {
            // Assume this is a corrupt video file.
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                // Ignore failures while cleaning up.
            }
        }
        if (bitmap == null) return null;
        // Scale down the bitmap if it is bigger than we need.
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width > mThumbnailwidth) {
            float scale = (float) mThumbnailwidth / width;
            int w = Math.round(scale * width);
            int h = Math.round(scale * height);
            bitmap = Bitmap.createScaledBitmap(bitmap, w, h, true);
        }
        return bitmap;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            Log.d(LOGTAG, "onHandleIntent.getAction = " + action);
            if (ACTION_MAKE_THUMBNAIL.equals(action)) {

                final String pathToVideo = intent.getStringExtra(EXTRA_PATH_TO_VIDEO);
                mThumbnailDirectory = pathToVideo.substring(0, pathToVideo.lastIndexOf("/")+1);
                mThumbnailDirectory += THUMBNAIL_FOLDER;
                Log.d(LOGTAG, "thumbnail will be stored here: " + mThumbnailDirectory);

                Bitmap thumbnail = startActionMakeThumbnail(this, pathToVideo);
                if (thumbnail != null) {
                    Log.d(LOGTAG, "thumbnail complete");
                    String pathToThumb = saveImage(thumbnail);

                    Intent thumbnailIntent = new Intent();
                    thumbnailIntent.setAction(ACTION_THUMBNAIL_COMPLETE);
                    thumbnailIntent.putExtra(EXTRA_PATH_TO_VIDEO, pathToVideo);
                    thumbnailIntent.putExtra(EXTRA_PATH_TO_THUMBNAIL, pathToThumb);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(thumbnailIntent);

                } else {
                    Log.d(LOGTAG, "thumbnail not saved");
                }
            }
        }
    }

    private String saveImage(Bitmap finalBitmap) {

        String pathToThumb = null;

        File myDir = new File(mThumbnailDirectory);
        myDir.mkdirs();

        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);

        String fname = THUMBNAIL_PREFIX + n + ".jpg";

        File file = new File(myDir, fname);
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            pathToThumb = mThumbnailDirectory + fname;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pathToThumb;
    }
}
