package com.caseystalnaker.android.popinvideodemo;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.caseystalnaker.android.popinvideodemo.utils.Utils;

/*
* This is a simple activity to play back video that was previously been recorded
* on this device.
*/
public class VideoPlaybackActivity extends Activity implements View.OnClickListener{

    private VideoView mVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_video_playback);
        findViewById(R.id.close_button).setOnClickListener(this);

        final Bundle extras = getIntent().getExtras();
        if(extras != null){
            final String path = extras.getString(Utils.PREVIEW_VIDEO_PATH_INTENT_KEY);
            Uri videoUri = Uri.parse(path);
            if(videoUri != null){
                mVideoView = (VideoView) findViewById(R.id.video_preview);
                mVideoView.setVideoURI(videoUri);
                mVideoView.setMediaController(new MediaController(this));
                mVideoView.requestFocus();
                mVideoView.start();
                //show a toast with the path the stored video.
                Toast.makeText(this, getResources().getString(R.string.video_saved_confirmation) + path,
                        Toast.LENGTH_LONG).show();
            }else{
                //no video Uri
                Toast.makeText(this, getResources().getText(R.string.video_not_found), Toast.LENGTH_SHORT).show();
                delayedExit();
            }
        }else {
            //no bundle data
            Toast.makeText(this, getResources().getText(R.string.no_video_data), Toast.LENGTH_SHORT).show();
            delayedExit();
        }
    }

    private void delayedExit(){
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //close after 2 seconds.
                finish();
            }
        }, 2000);
    }

    @Override
    public void onClick(View view) {
        finish();
    }

    @Override
    public void onStop(){
        if(mVideoView!=null && mVideoView.isPlaying()){
            mVideoView.suspend();
            mVideoView.stopPlayback();
        }
        super.onStop();
    }
}
