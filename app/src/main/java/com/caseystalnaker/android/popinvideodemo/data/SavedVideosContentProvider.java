package com.caseystalnaker.android.popinvideodemo.data;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;

public class SavedVideosContentProvider extends ContentProvider {

    private static final String LOGTAG = SavedVideosContentProvider.class.getSimpleName();
    private VideoReaderDbHelper mSavedVideoDb;
    public static HashMap<String, String> ALL_VIDEOS_PROJECTION_MAP = new HashMap<>();

    private static final String AUTHORITY = "com.caseystalnaker.android.popinvideodemo.data";
    public static final int VIDEOS = 100;
    public static final int VIDEO_ID = 110;

    private static final String VIDEOS_BASE_PATH = SavedVideoContract.VideoEntry.TABLE_NAME;
    public static final Uri ALL_VIDEOS_URI = Uri.parse("content://" + AUTHORITY
            + "/" + VIDEOS_BASE_PATH);

    private static final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        mUriMatcher.addURI(AUTHORITY, VIDEOS_BASE_PATH, VIDEOS);
        mUriMatcher.addURI(AUTHORITY, VIDEOS_BASE_PATH + "/#", VIDEO_ID);
    }

    public SavedVideosContentProvider() {
        //populate allVideos projectionMap
        ALL_VIDEOS_PROJECTION_MAP.put(SavedVideoContract.VideoEntry._ID, SavedVideoContract.VideoEntry._ID);
        ALL_VIDEOS_PROJECTION_MAP.put(SavedVideoContract.VideoEntry.COLUMN_NAME_VIDEO_NAME, SavedVideoContract.VideoEntry.COLUMN_NAME_VIDEO_NAME);
        ALL_VIDEOS_PROJECTION_MAP.put(SavedVideoContract.VideoEntry.COLUMN_NAME_VIDEO_PATH, SavedVideoContract.VideoEntry.COLUMN_NAME_VIDEO_PATH);
        ALL_VIDEOS_PROJECTION_MAP.put(SavedVideoContract.VideoEntry.COLUMN_NAME_THUMBNAIL_NAME, SavedVideoContract.VideoEntry.COLUMN_NAME_THUMBNAIL_NAME);
        ALL_VIDEOS_PROJECTION_MAP.put(SavedVideoContract.VideoEntry.COLUMN_NAME_THUMBNAIL_PATH, SavedVideoContract.VideoEntry.COLUMN_NAME_THUMBNAIL_PATH);
        ALL_VIDEOS_PROJECTION_MAP.put(SavedVideoContract.VideoEntry.COLUMN_NAME_TIMESTAMP, SavedVideoContract.VideoEntry.COLUMN_NAME_TIMESTAMP);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean onCreate() {
        Log.d(LOGTAG, "onCreate()");
        mSavedVideoDb = new VideoReaderDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(SavedVideoContract.VideoEntry.TABLE_NAME);
        switch (mUriMatcher.match(uri)) {

            // If the incoming URI was for all of table3
            case VIDEOS:

                if (TextUtils.isEmpty(sortOrder)) sortOrder = "_ID ASC";
                break;

            // If the incoming URI was for a single row
            case VIDEO_ID:

                /*
                 * Because this URI was for a single row, the _ID value part is
                 * present. Get the last path segment from the URI; this is the _ID value.
                 * Then, append the value to the WHERE clause for the query
                 */
                selection = selection + "_ID = " + uri.getLastPathSegment();
                break;

            default:
                Log.d(LOGTAG, "Invalid URI. Skipping.");
                return null;
        }
        Cursor cursor = queryBuilder.query(mSavedVideoDb.getReadableDatabase(),
                projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
