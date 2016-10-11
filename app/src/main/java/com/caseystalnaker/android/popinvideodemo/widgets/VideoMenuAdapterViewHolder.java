package com.caseystalnaker.android.popinvideodemo.widgets;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.caseystalnaker.android.popinvideodemo.R;
import com.caseystalnaker.android.popinvideodemo.VideoPlaybackActivity;
import com.caseystalnaker.android.popinvideodemo.utils.Utils;

public class VideoMenuAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    private static String LOGTAG = VideoMenuAdapterViewHolder.class.getSimpleName();
    public CardView mCardView;
    public TextView mTextView;
    public ImageView mImageView;

    public VideoMenuAdapterViewHolder(final LinearLayout v) {
        super(v);

        mCardView = (CardView) v.findViewById(R.id.card_view);
        mCardView.setCardElevation(2);
        mTextView = (TextView) mCardView.findViewById(R.id.info_txt);
        mImageView = (ImageView) mCardView.findViewById(R.id.video_thumbnail);
        mCardView.setOnClickListener(this);
    }

    @Override
    public void onClick(final View view) {
        Intent i = new Intent(view.getContext().getApplicationContext(), VideoPlaybackActivity.class);
        i.setAction(Utils.REQUEST_VIDEO_PLAYBACK);
        i.putExtra(Utils.PREVIEW_VIDEO_PATH_INTENT_KEY, (String) view.getTag());

        Log.d(LOGTAG, "loading preview video: " + view.getTag());
        //relay to parent activity to prevent activity scope issues
        LocalBroadcastManager.getInstance(view.getContext().getApplicationContext()).sendBroadcast(i);
    }
}