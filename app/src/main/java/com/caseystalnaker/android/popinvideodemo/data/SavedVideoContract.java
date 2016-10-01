package com.caseystalnaker.android.popinvideodemo.data;

import android.provider.BaseColumns;

/**
 * Created by Casey on 10/1/16.
 */

public class SavedVideoContract {

    private SavedVideoContract(){}

    public static class VideoEntry implements BaseColumns {
        public static final String TABLE_NAME = "videos";
        public static final String COLUMN_NAME_VIDEO_NAME = "video_name";
        public static final String COLUMN_NAME_VIDEO_PATH = "video_path";
        public static final String COLUMN_NAME_THUMBNAIL_NAME = "thumb_name";
        public static final String COLUMN_NAME_THUMBNAIL_PATH = "thumb_path";
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
    }
}
