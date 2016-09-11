package com.caseystalnaker.android.popinvideodemo.util;

/**
 * Created by Casey on 9/9/16.
 */
public class Util {
    private static Util mInstance;
    public static final int mVideoTimeLimit = 30000;
    public static final String mCameraId = "1";
    public static final int CAMERA_REQUEST_ID = 1888;

    private Util() {}

    public static Util getInstance() {
        if(mInstance == null){
            return new Util();
        }
        return mInstance;
    }
}
