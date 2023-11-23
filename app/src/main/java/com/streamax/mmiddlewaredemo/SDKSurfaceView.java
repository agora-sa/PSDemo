package com.streamax.mmiddlewaredemo;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;

import com.streamax.decode.NativeSurfaceView;

public class SDKSurfaceView extends NativeSurfaceView {
    private int mChannel = 0;
    private SurfaceLifeCycle mSurfaceLifeCycle = null;
    private String TAG = "SDKSurfaceView";
    public SDKSurfaceView(Context context) {
        super(context);
    }

    public SDKSurfaceView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public SDKSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        super.surfaceCreated(holder);
        if(null != mSurfaceLifeCycle) {
            mSurfaceLifeCycle.surfaceCreated(mChannel);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        super.surfaceChanged(holder, format, width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        super.surfaceDestroyed(surfaceHolder);
    }

    public void setSurfaceOnLifeCycle(SurfaceLifeCycle surface, int channel) {
        this.mSurfaceLifeCycle = surface;
        this.mChannel = channel;
    }

    public interface SurfaceLifeCycle {
        void surfaceCreated(int channel);
    }
}
