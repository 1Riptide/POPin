package com.caseystalnaker.android.popinvideodemo.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.caseystalnaker.android.popinvideodemo.R;
import com.caseystalnaker.android.popinvideodemo.VideoPlaybackActivity;
import com.caseystalnaker.android.popinvideodemo.utils.Utils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Casey on 9/10/16.
 */

public class VideoMenuAdapter extends RecyclerView.Adapter<VideoMenuAdapter.ViewHolder> {
    private static final String LOGTAG = VideoMenuAdapter.class.getSimpleName();
    private static Context mContext;
    private ArrayList mKeys;
    private ArrayList mValues;

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
            mCardView.setOnClickListener(this);
        }

        @Override
        public void onClick(final View view) {
            Intent i = new Intent(mContext.getApplicationContext(), VideoPlaybackActivity.class);
            i.setAction(Utils.REQUEST_VIDEO_PLAYBACK);
            i.putExtra(Utils.PREVIEW_VIDEO_PATH_INTENT_KEY, (String) view.getTag());

            Log.d(LOGTAG, "loading preview video: " + view.getTag());
            //relay to parent activity to prevent activity scope issues
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(i);
        }
    }

    public VideoMenuAdapter(final Context context) {
        mContext = context;
        Log.d(LOGTAG, "VideMenuAdapter()");
        swap();
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

        holder.mTextView.setText("Video # "+(position+1));
        final String thumbnailPath = ((String)mValues.get(position));
        //storing path to video in tag
        holder.mCardView.setTag(mKeys.get(position));
        Log.d(LOGTAG, "KEY : " + thumbnailPath);
        Picasso.with(mContext).load("file:"+thumbnailPath).resize(100, 100).centerCrop().into(holder.mImageView);
    }

    @Override
    public int getItemCount() {
        return mKeys.size();
    }

    public void swap(){
        if(mKeys!=null)
            mKeys.clear();
        if(mValues!=null)
            mValues.clear();

        SharedPreferences prefs = mContext.getSharedPreferences(Utils.VIDEO_GALLERY_PREFS, Context.MODE_PRIVATE);
        Map map = prefs.getAll();
        Log.d(LOGTAG, "Swap() : " + map);
        mKeys = new ArrayList<>(map.keySet());
        mValues = new ArrayList<>(map.values());

        notifyDataSetChanged();
    }
}

