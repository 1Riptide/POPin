package com.caseystalnaker.android.popinvideodemo.adapters;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.caseystalnaker.android.popinvideodemo.R;
import com.caseystalnaker.android.popinvideodemo.data.SavedVideoContract;
import com.caseystalnaker.android.popinvideodemo.widgets.VideoMenuAdapterViewHolder;
import com.squareup.picasso.Picasso;

/**
 * Created by Casey on 9/10/16.
 */

public class VideoMenuCursorAdapter extends CursorRecyclerAdapter {
    private static final String LOGTAG = VideoMenuCursorAdapter.class.getSimpleName();

    public VideoMenuCursorAdapter(Cursor cursor) {
        super(cursor);
    }

    public void onBindViewHolderCursor(RecyclerView.ViewHolder holder, Cursor cursor) {
        VideoMenuAdapterViewHolder vh = (VideoMenuAdapterViewHolder)holder;
        final String thumbnailPath = cursor.getString(cursor.getColumnIndex(SavedVideoContract.VideoEntry.COLUMN_NAME_THUMBNAIL_PATH));
        final String videoPath = cursor.getString(cursor.getColumnIndex(SavedVideoContract.VideoEntry.COLUMN_NAME_VIDEO_PATH));

        vh.mTextView.setText("Video # "+(cursor.getPosition()+1));
        //storing path to video in tag
        vh.mCardView.setTag(videoPath);
        Log.d(LOGTAG, "KEY : " + thumbnailPath);
        Picasso.with(vh.mImageView.getContext()).load("file:"+thumbnailPath).resize(100, 100).centerCrop().into(vh.mImageView);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.video_menu_layout_item, parent, false);

        return new VideoMenuAdapterViewHolder(v);
    }
}