package com.streamax.mmiddlewaredemo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.streamax.common.STEnumType;
import com.streamax.netplayback.STNetPlayback;
import com.streamax.netstream.STNetStream;

public class FullVideoActivity extends AppCompatActivity implements SDKSurfaceView.SurfaceLifeCycle {
    private static final boolean AUTO_HIDE = true;
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;

    private static final String REAL_PLAY_MODE = "REALPLAY";
    private static final String PLAYBACK_MODE = "PLAYBACK";

    private String TAG = "FullVideoActivity";
    private SDKSurfaceView mDisplayView;
    private STNetStream mNetStreamCH1;
    private STNetPlayback mNetPlayback;
    private String mPlayMode;
    private int mChannel;

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_full_video);
        Intent intent = getIntent();
        mPlayMode = intent.getStringExtra("PLAYMODE");
        mChannel = intent.getIntExtra("CHANNEL", 0);
        if(mPlayMode.equals(REAL_PLAY_MODE)){
            mNetStreamCH1 = SDKManager.getInstance().getRealStream(mChannel);
        } else if(mPlayMode.equals(PLAYBACK_MODE)) {
            mNetPlayback = SDKManager.getInstance().getNetPlayback();
        }
        mDisplayView = findViewById(R.id.sdk_surface_view);
        mDisplayView.setSurfaceOnLifeCycle(this, mChannel);
        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);

        mContentView.setOnTouchListener(new OnDoubleClickListener(new OnDoubleClickListener.DoubleClickCallback() {
            @Override
            public void onDoubleClick() {
                finish();
            }
        }));
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    @Override
    public void surfaceCreated(int channel) {
        Log.d(TAG, "surface view create callback, channel = " + channel);
        if(null != mNetStreamCH1 && mPlayMode.equals(REAL_PLAY_MODE)) {
            mNetStreamCH1.switchSurface(mDisplayView);
            mNetStreamCH1.switchStream(STEnumType.STStreamType.SUB);
        } else if(null != mNetPlayback && mPlayMode.equals(PLAYBACK_MODE)) {
            mNetPlayback.switchSurface(mChannel, mDisplayView, STEnumType.STRotateType.NOMAL, false);
        }
    }
}
