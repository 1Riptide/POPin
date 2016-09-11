package com.caseystalnaker.android.popinvideodemo.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.caseystalnaker.android.popinvideodemo.R;

import java.util.ArrayList;

/**
 * Created by Casey on 9/10/16.
 */

public class VideoMenuAdapter extends RecyclerView.Adapter<VideoMenuAdapter.ViewHolder> {
    private static final String LOGTAG = VideoMenuAdapter.class.getSimpleName();
    private static Context mContext;
    private String[] mVideoTitles;
    private ArrayList<Bitmap> mVideoThumbnails;
    //keep a reference to your Toast. So you don't eat too much.
    private static Toast mToast = null;

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public CardView mCardView;
        public TextView mTextView;
        public ImageView mImageView;

        public ViewHolder(final LinearLayout v) {
            super(v);

            mCardView = (CardView) v.findViewById(R.id.card_view);
            mCardView.setCardElevation(2);
            mTextView = (TextView) mCardView.findViewById(R.id.info_txt);
            mImageView = (ImageView) mCardView.findViewById(R.id.video_thumbnail);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(final View view) {
            if(mToast != null) mToast.cancel();
            mToast = Toast.makeText(mContext, "Not implemented. Menu is for show.", Toast.LENGTH_SHORT);
            mToast.show();
        }
    }

    public VideoMenuAdapter(final Context context, final String[] myVideoTitles, final ArrayList<Bitmap> myVideoThumbnails) {
        mContext = context;
        mVideoTitles = myVideoTitles;
        mVideoThumbnails = myVideoThumbnails;
    }

    @Override
    public VideoMenuAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent,
                                                  final int viewType) {
        final LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.video_menu_layout_item, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.mTextView.setText(mVideoTitles[position]);

        //instead of simply passing in drawable, I went ahead and passed bitmaps instead, to prep
        //for how video still shots will arrive. Here we are converting a bitmaps to a drawables.
        final Bitmap thumbnail = mVideoThumbnails.get(position);
        holder.mImageView.setBackground(new BitmapDrawable(mContext.getResources(), thumbnail));
    }

    @Override
    public int getItemCount() {
        return mVideoTitles.length;
    }

}

