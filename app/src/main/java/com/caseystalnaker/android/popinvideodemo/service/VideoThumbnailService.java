package com.caseystalnaker.android.popinvideodemo.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.caseystalnaker.android.popinvideodemo.data.SavedVideoContract;
import com.caseystalnaker.android.popinvideodemo.data.VideoReaderDbHelper;

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
    private static final String THUMBNAIL_FOLDER = "thumbnails/";
    private static final String THUMBNAIL_PREFIX = "Thumbnail-";
    private static final int mThumbnailwidth = 200;

    public VideoThumbnailService() {
        super("VideoThumbnailService");
    }

    public static Bitmap startActionMakeThumbnail(final String pathToVideo) {
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
        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();
        if (width > mThumbnailwidth) {
            final float scale = (float) mThumbnailwidth / width;
            final int w = Math.round(scale * width);
            final int h = Math.round(scale * height);
            bitmap = Bitmap.createScaledBitmap(bitmap, w, h, true);
        }
        return bitmap;
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            Log.d(LOGTAG, "onHandleIntent.getAction = " + action);
            if (ACTION_MAKE_THUMBNAIL.equals(action)) {

                final String pathToVideo = intent.getStringExtra(EXTRA_PATH_TO_VIDEO);
                String thumbnailDirectory = pathToVideo.substring(0, pathToVideo.lastIndexOf("/") + 1);
                thumbnailDirectory += THUMBNAIL_FOLDER;
                Log.d(LOGTAG, "thumbnail will be stored here: " + thumbnailDirectory);

                final Bitmap thumbnail = startActionMakeThumbnail(pathToVideo);
                if (thumbnail != null) {
                    Log.d(LOGTAG, "thumbnail complete");
                    final String pathToThumb = saveImage(thumbnailDirectory, thumbnail);

                    String nameOfThumb = pathToThumb.substring(pathToThumb.lastIndexOf("/") + 1, pathToThumb.length());
                    String nameOfVideo = pathToVideo.substring(pathToVideo.lastIndexOf("/") + 1, pathToVideo.length());

                    Log.d(LOGTAG, "nameOfThumb = " + nameOfThumb);
                    Log.d(LOGTAG, "nameOfVideo = " + nameOfVideo);
                    long recordId = saveRecord(nameOfVideo, pathToVideo, nameOfThumb, pathToThumb);
                    Log.d(LOGTAG, "NEW RECORD CREATED : id = " + recordId);

                    final Intent thumbnailIntent = new Intent();
                    thumbnailIntent.setAction(ACTION_THUMBNAIL_COMPLETE);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(thumbnailIntent);

                } else {
                    Log.d(LOGTAG, "thumbnail not saved");
                }
            }
        }
    }

    private String saveImage(final String thumbnailDirectory, final Bitmap finalBitmap) {

        String pathToThumb = null;

        final File myDir = new File(thumbnailDirectory);
        myDir.mkdirs();

        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);

        final String fname = THUMBNAIL_PREFIX + n + ".jpg";

        final File file = new File(myDir, fname);
        if (file.exists()) file.delete();
        try {
            final FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            pathToThumb = thumbnailDirectory + fname;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pathToThumb;
    }

    private long saveRecord(final String nameOfVideo, final String pathToVideo, final String nameOfThumb, final String pathToThumb) {
        final VideoReaderDbHelper dbHelper = new VideoReaderDbHelper(getApplicationContext());
        final SQLiteDatabase db = dbHelper.getWritableDatabase();

        //create map of values to write
        ContentValues values = new ContentValues();
        values.put(SavedVideoContract.VideoEntry.COLUMN_NAME_VIDEO_NAME, nameOfVideo);
        values.put(SavedVideoContract.VideoEntry.COLUMN_NAME_VIDEO_PATH, pathToVideo);
        values.put(SavedVideoContract.VideoEntry.COLUMN_NAME_THUMBNAIL_NAME, nameOfThumb);
        values.put(SavedVideoContract.VideoEntry.COLUMN_NAME_THUMBNAIL_PATH, pathToThumb);

        return db.insert(SavedVideoContract.VideoEntry.TABLE_NAME, null, values);


    }
}
