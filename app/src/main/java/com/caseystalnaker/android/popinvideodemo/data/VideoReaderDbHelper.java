package com.caseystalnaker.android.popinvideodemo.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.caseystalnaker.android.popinvideodemo.data.SavedVideoContract;

/**
 * Created by Casey on 10/1/16.
 */

public class VideoReaderDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "PopinVideos.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + SavedVideoContract.VideoEntry.TABLE_NAME + " (" +
                    SavedVideoContract.VideoEntry._ID + " INTEGER PRIMARY KEY," +
                    SavedVideoContract.VideoEntry.COLUMN_NAME_VIDEO_NAME + TEXT_TYPE + COMMA_SEP +
                    SavedVideoContract.VideoEntry.COLUMN_NAME_VIDEO_PATH + TEXT_TYPE + COMMA_SEP +
                    SavedVideoContract.VideoEntry.COLUMN_NAME_THUMBNAIL_NAME + TEXT_TYPE + COMMA_SEP +
                    SavedVideoContract.VideoEntry.COLUMN_NAME_THUMBNAIL_PATH + TEXT_TYPE + COMMA_SEP +
                    SavedVideoContract.VideoEntry.COLUMN_NAME_TIMESTAMP + " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + SavedVideoContract.VideoEntry.TABLE_NAME;

    public VideoReaderDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

}
