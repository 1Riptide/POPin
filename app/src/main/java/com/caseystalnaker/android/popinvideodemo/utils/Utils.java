package com.caseystalnaker.android.popinvideodemo.utils;

import android.Manifest;
import android.util.SparseIntArray;
import android.view.Surface;

/**
 * Created by Casey on 9/9/16.
 */
public class Utils {

    //Back camera id = 0, front camera id = 1;
    public static final int CAMERA_ID = 1;
    public static final int VIDEO_TIME_LIMIT = 30000;
    public static final int VIDEO_BIT_RATE = 10000000;
    public static final int VIDEO_FRAME_RATE = 30;
    public static final String COUNTDOWN_TIMER_FORMAT = "%02d:%02d:%02d";

    public static final SparseIntArray DEFAULT_ORIENTATIONS = new SparseIntArray();
    public static final SparseIntArray INVERSE_ORIENTATIONS = new SparseIntArray();

    public static final int SENSOR_ORIENTATION_DEFAULT_DEGREES = 90;
    public static final int SENSOR_ORIENTATION_INVERSE_DEGREES = 270;
    public static final int REQUEST_VIDEO_PERMISSIONS = 1;
    public static final String[] VIDEO_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
    };

    public static final String PREVIEW_VIDEO_PATH_INTENT_KEY = "video_path";
    public static final String REQUEST_VIDEO_PLAYBACK = "video.request.playback";
    public static final String PREVIEW_VIDEO_COMPLETE_INTENT = "video.recording.preview";
    static {
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_0, 90);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_90, 0);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_180, 270);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    static {
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_0, 270);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_90, 180);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_180, 90);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_270, 0);
    }

    public static final String VIDEO_GALLERY_PREFS = "video.gallery.prefs";
    /*
    Not needed.

    private static Utils mInstance;
    private Utils() {}


    public static Utils getInstance() {
        if(mInstance == null){
            return new Utils();
        }
        return mInstance;
    }
    */
}
