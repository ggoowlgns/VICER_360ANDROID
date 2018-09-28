package com.samsung.android.sdk.gear360.sample.service;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.samsung.android.sdk.gear360.ResponseListener;
import com.samsung.android.sdk.gear360.device.VideoPlayer;
import com.samsung.android.sdk.gear360.sample.R;
import com.samsung.android.sdk.gear360.sample.SampleApplication;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Activity for render preview.
 */

public class VideoPlayerActivity extends Activity {
    private final String TAG = VideoPlayerActivity.class.getSimpleName();
    private VideoPlayer mVideoPlayer;
    private View mProgressLayout;
    private boolean mIsPlaying;
    private String mRequestedId;
    private SurfaceHolder.Callback surfaceHolderCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            Log.d(TAG, "surfaceHolder.surfaceCreated");
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Log.d(TAG, "surfaceHolder.surfaceChanged");
            if (mVideoPlayer != null) {
                mIsPlaying = true;
                mVideoPlayer.play(new ResponseListener<Long>() {
                    @Override
                    public void onSucceed(final Long result) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (result != 0) {
                                    Log.d(TAG, "STARTED");
                                    mProgressLayout.setVisibility(View.GONE);
                                } else {
                                    Log.d(TAG, "FINISHED");
                                    mIsPlaying = false;
                                    finish();
                                }
                            }
                        });
                    }

                    @Override
                    public void onFailed(ErrorCode code, final String message) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Fail to start!!! - " + (message != null ? message : ""), Toast.LENGTH_LONG).show();
                                finish();
                            }
                        });
                    }
                }, mRequestedId, holder.getSurface(), true);
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.d(TAG, "surfaceHolder.surfaceDestroyed");
            stopPlayingVideo();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        if (getIntent() == null) {
            Log.e(TAG, "Intent is null");
            finish();
            return;
        }

        mRequestedId = getIntent().getStringExtra("ID");
        ((TextView) findViewById(R.id.view_video_name)).setText(getIntent().getStringExtra("NAME"));
        String duration = getIntent().getIntExtra("DURATION", 0) + " sec";
        ((TextView) findViewById(R.id.view_video_duration)).setText(duration);
        ((TextView) findViewById(R.id.view_video_resolution)).setText(getIntent().getStringExtra("RESOLUTION"));
        ((TextView) findViewById(R.id.view_video_frame_rate)).setText(getIntent().getStringExtra("FRAME_RATE"));
        String date = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault())
                .format(new Date(getIntent().getLongExtra("TIME", 0)));
        ((TextView) findViewById(R.id.view_video_time)).setText(date);

        mProgressLayout = findViewById(R.id.lay_preview_waiting);
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.suf_videoPreview);

        mVideoPlayer = ((SampleApplication) getApplication()).getDevice().getVideoPlayer();
        final SurfaceHolder surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(surfaceHolderCallback);
    }

    public void pauseButtonClicked(View view) {
        Log.d(TAG, "pauseButtonClicked");
        Button pauseButton = (Button) findViewById(R.id.btn_pause_preview);
        if (mIsPlaying) {
            pauseButton.setText(R.string.camera_button_record_video_resume);
            mVideoPlayer.pause(new ResponseListener<Void>() {
                @Override
                public void onSucceed(Void result) {
                    mIsPlaying = false;
                    Log.i(TAG, "onSucceed : " + result);
                }

                @Override
                public void onFailed(ErrorCode code, String message) {
                    Log.e(TAG, "onFailed - pause : code : " + code.toString() + ", message : " + message);
                }
            });
        } else {
            pauseButton.setText(R.string.camera_button_record_video_pause);
            mVideoPlayer.resume(new ResponseListener<Void>() {
                @Override
                public void onSucceed(Void result) {
                    mIsPlaying = true;
                    Log.i(TAG, "onSucceed : " + result);
                }

                @Override
                public void onFailed(ErrorCode code, String message) {
                    Log.e(TAG, "onFailed - resume : code : " + code.toString() + ", message : " + message);
                }
            });
        }
    }

    public void stopButtonClicked(View view) {
        Log.d(TAG, "stopButtonClicked");
        stopPlayingVideo();
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d(TAG, "onBackPressed");
        stopPlayingVideo();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        stopPlayingVideo();
    }

    private void stopPlayingVideo() {
        Log.d(TAG, "stopPlayingVideo");
        if (mVideoPlayer != null) {
            mVideoPlayer.stop(new ResponseListener<Void>() {
                @Override
                public void onSucceed(Void result) {
                    mIsPlaying = false;
                    Log.i(TAG, "onSucceed : " + result);
                }

                @Override
                public void onFailed(ErrorCode code, String message) {
                    Log.e(TAG, "onFailed - Stop : code : " + code.toString() + ", message : " + message);
                }
            });
        }
    }
}