package com.caseystalnaker.android.popinvideodemo.adapters;

import com.caseystalnaker.android.popinvideodemo.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;

/**
 * Created by Casey on 9/10/16.
 */

public class VideoMenuAdapter extends RecyclerView.Adapter<VideoMenuAdapter.ViewHolder> {
    private static final String LOGTAG = VideoMenuAdapter.class.getSimpleName();
    private Context mContext;
    private String[] mVideoTitles;
    private ArrayList<Bitmap> mVideoThumbnails;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public CardView mCardView;
        public TextView mTextView;
        public ImageView mImageView;

        public ViewHolder(final LinearLayout v) {
            super(v);

            mCardView = (CardView) v.findViewById(R.id.card_view);
            mCardView.setCardElevation(2);
            mTextView = (TextView) mCardView.findViewById(R.id.info_txt);
            mImageView = (ImageView) mCardView.findViewById(R.id.video_thumbnail);
        }
    }

    public VideoMenuAdapter(final Context context, final String[] myVideoTitles, final ArrayList<Bitmap> myVideoThumbnails) {
        Log.d(LOGTAG, "setting data:  " + myVideoTitles.toString());
        mContext = context;
        mVideoTitles = myVideoTitles;
        mVideoThumbnails = myVideoThumbnails;
    }

    @Override
    public VideoMenuAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent,
                                                  final  int viewType) {
        // create a new view
        final LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.video_menu_layout_item, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.mTextView.setText(mVideoTitles[position]);

        Bitmap thumbnail = mVideoThumbnails.get(position);
        holder.mImageView.setBackground(new BitmapDrawable(mContext.getResources(), thumbnail));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mVideoTitles.length;
    }
}

