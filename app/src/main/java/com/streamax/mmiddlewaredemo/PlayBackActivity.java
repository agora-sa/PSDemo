package com.streamax.mmiddlewaredemo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.streamax.common.STEnumType;
import com.streamax.common.STErrorCode;
import com.streamax.common.STResponseData;
import com.streamax.common.STSearchDiskType;
import com.streamax.common.STVideoDecodeType;
import com.streamax.decode.NativeSurfaceView;
import com.streamax.netdevice.STNetDevice;
import com.streamax.netdevice.STNetDeviceCallback;
import com.streamax.netdevice.devtype.STNetDevMsgType;
import com.streamax.netplayback.STNetPlayback;
import com.streamax.netplayback.STNetPlaybackCallback;
import com.streamax.netplayback.STPlayBackRotateType;
import com.streamax.netsearch.STNetSearch;
import com.streamax.netstream.STNetStream;

import java.io.UnsupportedEncodingException;


public class PlayBackActivity extends AppCompatActivity implements STNetDeviceCallback, SDKSurfaceView.SurfaceLifeCycle {
    String TAG = "PlayBackActivity";
    private STNetDevice mNetDevice;
    private STNetStream netStreamCH1;
    private STNetStream netStreamCH2;
    private STNetStream netStreamCH3;
    private STNetStream netStreamCH4;
    private STNetSearch mNetSearch;
    private STNetPlayback mNetPlayback;

    private Button mSearchAll;
    private Button mSearchMonth;
    private Button mSearchDay;
    private Button mVideoBtn;
    private Button mSoundBtn;
    private Button mFrameBtn;
    private Button mPauseBtn;
    private Button mSeekBtn;
    private Button mSpeedBtn;
    private Button mGrabBtn;
    private Button mAddStreamBtn;
    private Button mRemoveStreamBtn;
    private TextView mMsgShowTV;
    private SDKSurfaceView mNativeDisplayView;
    private SDKSurfaceView mNativeDisplayView1;
    private SDKSurfaceView mNativeDisplayView2;
    private SDKSurfaceView mNativeDisplayView3;
    private boolean bOpenVideo = false;
    private boolean bPlaybackPause = true;

    private boolean bRealPlayPause = true;
    private STEnumType.STStreamType stream_type = STEnumType.STStreamType.SUB;

    String stTime = "20181229082528";
    String endTime = "20181229235959";
    String mSeekTime = "20181229102528";
    private boolean bOpenSound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_back);
        initView();
        initOpreation();
        STVideoDecodeType.setDecodeType(STVideoDecodeType.HARD);
    }

    private void initView() {
        mSearchAll = findViewById(R.id.btn_search_all);
        mSearchMonth = findViewById(R.id.btn_search_month);
        mSearchDay = findViewById(R.id.btn_search_day);
        mVideoBtn = findViewById(R.id.btn_playback_video);
        mSoundBtn = findViewById(R.id.btn_playback_sound);
        mFrameBtn = findViewById(R.id.btn_playback_frame);
        mPauseBtn = findViewById(R.id.btn_playback_pause);
        mSeekBtn = findViewById(R.id.btn_playback_seek);
        mSpeedBtn = findViewById(R.id.btn_playback_speed);
        mGrabBtn = findViewById(R.id.btn_playback_grab);
        mAddStreamBtn = findViewById(R.id.btn_playback_add);
        mRemoveStreamBtn = findViewById(R.id.btn_playback_remove);
        mMsgShowTV = findViewById(R.id.tv_msg_info);

        mNativeDisplayView = findViewById(R.id.nativesurfaceView);
        mNativeDisplayView1 = findViewById(R.id.nativesurfaceView1);
        mNativeDisplayView2 = findViewById(R.id.nativesurfaceView2);
        mNativeDisplayView3 = findViewById(R.id.nativesurfaceView3);

        mSearchAll.setOnClickListener(myClickListener);
        mSearchMonth.setOnClickListener(myClickListener);
        mSearchDay.setOnClickListener(myClickListener);
        mVideoBtn.setOnClickListener(myClickListener);
        mSoundBtn.setOnClickListener(myClickListener);
        mFrameBtn.setOnClickListener(myClickListener);
        mPauseBtn.setOnClickListener(myClickListener);
        mSeekBtn.setOnClickListener(myClickListener);
        mSpeedBtn.setOnClickListener(myClickListener);
        mGrabBtn.setOnClickListener(myClickListener);
        mAddStreamBtn.setOnClickListener(myClickListener);
        mRemoveStreamBtn.setOnClickListener(myClickListener);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initOpreation() {
        mNativeDisplayView.setOnTouchListener(new OnDoubleClickListener(new OnDoubleClickListener.DoubleClickCallback() {
            @Override
            public void onDoubleClick() {
                Intent intent = new Intent(PlayBackActivity.this, FullVideoActivity.class);
                intent.putExtra("CHANNEL", 0);
                intent.putExtra("PLAYMODE", "PLAYBACK");
                startActivity(intent);
            }
        }));
        mNativeDisplayView1.setOnTouchListener(new OnDoubleClickListener(new OnDoubleClickListener.DoubleClickCallback() {
            @Override
            public void onDoubleClick() {
                Intent intent = new Intent(PlayBackActivity.this, FullVideoActivity.class);
                intent.putExtra("CHANNEL", 1);
                intent.putExtra("PLAYMODE", "PLAYBACK");
                startActivity(intent);
            }
        }));

        mNetDevice = SDKManager.getInstance().getNetDevice();
        mNetSearch = SDKManager.getInstance().getSTNetSearch();
        mNetPlayback = SDKManager.getInstance().getNetPlayback();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public void onBackPressed() {
        if (bOpenVideo && null != mNetPlayback) {
            mNetPlayback.stopRemotePlay();
        }
        finish();
    }

    public View.OnClickListener myClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_search_all:
                    onSearchAll();
                    break;
                case R.id.btn_search_month:
                    onSearchMonth();
                    break;
                case R.id.btn_search_day:
                    onSearchDay();
                    break;
                case R.id.btn_playback_video:
                    onPlayback();
                    break;
                case R.id.btn_playback_sound:
                    onOpenSound();
                    break;
                case R.id.btn_playback_frame:
                    onFramePlay();
                    break;
                case R.id.btn_playback_pause:
                    onPauseSet();
                    break;
                case R.id.btn_playback_seek:
                    onSeekSet();
                    break;
                case R.id.btn_playback_speed:
                    onSpeedSet();
                    break;
                case R.id.btn_playback_grab:
                    onGrabLocalPic();
                    break;
                case R.id.btn_playback_add:
                    onAddStream();
                    break;
                case R.id.btn_playback_remove:
                    onRemoveStream();
                    break;
                default:
                    break;
            }
        }
    };

    private void onSearchAll() {
        STResponseData ret = mNetSearch.searchAllCalendar(65535, STEnumType.STStreamType.MAIN);
        showRetMsg(ret.getError(), ret);
    }

    private void onSearchMonth() {
        STResponseData ret = mNetSearch.searchCalendarByMonthEx(15, STEnumType.STStreamType.MAIN, 2018, 12, STSearchDiskType.DISK);
        showRetMsg(ret.getError(), ret);
    }

    private void onSearchDay() {
        STResponseData ret = mNetSearch.searchFilelistByDayEx(2, STEnumType.STStreamType.MAIN, stTime, endTime, STSearchDiskType.DISK);
        showRetMsg(ret.getError(), ret);
    }

    private void onPlayback() {
        if(!bOpenVideo) {
            NativeSurfaceView[] surfaceViewsArr = {mNativeDisplayView};
            STPlayBackRotateType[] rotateArr = {new STPlayBackRotateType(STEnumType.STRotateType.NOMAL, true)};
            int ret = mNetPlayback.openPlayback(1, STEnumType.STStreamType.MAIN, surfaceViewsArr, stTime, endTime, STSearchDiskType.DISK, rotateArr);
            mNativeDisplayView.setSurfaceOnLifeCycle(this, 0);

            mNetPlayback.openSound(0);
            bOpenSound = (ret == STErrorCode.ERROR_SUCCESS);
            bOpenVideo = (ret == STErrorCode.ERROR_SUCCESS);
            mNetPlayback.registerPlaybackMsgCallback(new STNetPlaybackCallback() {
                @Override
                public void netPlaybackCallback(int msgType, String data, int nParam) {
//                STLogUtils.i(TAG, "Play back ret = " + data);
                }
            });
        } else {
            mNetPlayback.stopRemotePlay();
            bOpenVideo = false;
            bOpenSound = false;
        }
        mVideoBtn.setText(bOpenVideo ? "关闭" : "打开");
    }

    private void onOpenSound() {
        if (!bOpenVideo) {
            return;
        }
        int ret = bOpenSound ? mNetPlayback.closeSound() : mNetPlayback.openSound(0);
        showRetMsg(ret, null);
        bOpenSound = !bOpenSound;
        mSoundBtn.setText(bOpenSound ? "关声音" : "开声音");
    }

    private void onFramePlay() {
        if (bOpenVideo) {
            //每次帧放25帧
            int ret = mNetPlayback.remotePlayFrame(25);
            showRetMsg(ret, null);
        }
    }

    private void onPauseSet() {
        if (bOpenVideo) {
            int ret = mNetPlayback.remotePlayPause(bPlaybackPause);
            bPlaybackPause = !bPlaybackPause;
            mPauseBtn.setText(bPlaybackPause ? "暂停" : "播放");
            showRetMsg(ret, null);
        }
    }

    private void onSeekSet() {
        if (bOpenVideo) {
            int ret = mNetPlayback.remotePlaySeek(mSeekTime);
            showRetMsg(ret, null);
        }
    }

    private void onSpeedSet() {
        if (bOpenVideo) {
            int ret = mNetPlayback.remotePlayHighSpeed(16);
            showRetMsg(ret, null);
        }
    }

    private void onGrabLocalPic() {
        if (bOpenVideo) {
            //通道号为掩码方式
            STResponseData ret = mNetPlayback.grabLocalPlaybackPicture(3, "/mnt/sdcard/");
            showRetMsg(ret.getError(), ret);
        }
    }

    private void onAddStream() {
        if (bOpenVideo) {
            int ret = mNetPlayback.addStream(STEnumType.STStreamType.MAIN, mNativeDisplayView1, stTime, 1, STEnumType.STRotateType.NOMAL, false);
            mNativeDisplayView1.setSurfaceOnLifeCycle(this, 1);
            showRetMsg(ret, null);
        }
    }

    private void onRemoveStream() {
        if (bOpenVideo) {
            int ret = mNetPlayback.removeStream(STEnumType.STStreamType.MAIN, stTime, 1);
            showRetMsg(ret, null);
        }
    }

    private void showRetMsg(int errorCode, STResponseData ret) {
        String msgStr = "错误码:" + errorCode;
        if (null != ret) {
            errorCode = ret.getError();
            msgStr = "错误码:" + errorCode + "\n";
            msgStr += "返回信息:" + ret.getResponseStr();
        }
        mMsgShowTV.setText(msgStr);
    }

    @Override
    public void deviceMsgCallback(STNetDevMsgType msgType, byte[] data, int nLen, int param) {
        Log.e(TAG, "msgType = " + msgType.getValue() + "  msg len = " + nLen);
        if (null != data && data.length >= nLen) {
            String str = null;
            try {
                str = new String(data, 0, nLen, "UTF-8");
                Log.e(TAG, "device Msg Callback = " + str);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceCreated(int channel) {
        if (0 == channel && null != mNetPlayback) {
            Log.d(TAG, "surface view create callback, netStreamCH1 ch = " + channel);
            mNetPlayback.switchSurface(0, mNativeDisplayView, STEnumType.STRotateType.NOMAL, false);
        } else if (1 == channel && null != mNetPlayback) {
            Log.d(TAG, "surface view create callback, netStreamCH2 ch = " + channel);
            mNetPlayback.switchSurface(1, mNativeDisplayView1, STEnumType.STRotateType.NOMAL, false);
        }
    }
}
