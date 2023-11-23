package com.streamax.mmiddlewaredemo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.streamax.common.STEnumType;
import com.streamax.common.STErrorCode;
import com.streamax.common.STLogUtils;
import com.streamax.common.STResponseData;
import com.streamax.common.STVideoDecodeType;
import com.streamax.netdevice.STNetDevice;
import com.streamax.netdevice.STNetDeviceCallback;
import com.streamax.netdevice.devtype.STNetDevMsgType;
import com.streamax.netstream.STNetStream;
import com.streamax.netstream.STNetStreamCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class BackupRealPlayActivity extends AppCompatActivity implements STNetDeviceCallback, SDKSurfaceView.SurfaceLifeCycle {
    String TAG = "RealPlayActivity";
    private STNetDevice netDevice;
    private STNetStream netStreamCH1;
    private STNetStream netStreamCH2;
    private STNetStream netStreamCH3;
    private STNetStream netStreamCH4;

    private Button mSwitchBtn;
    private Button mVideoBtn;
    private Button mSoundBtn;
    private Button mPauseBtn;
    private Button mGrabPicBtn;
    private Button mSaveVideoBtn;
    private TextView mMsgTV;
    private ImageView mImageView;
    private SDKSurfaceView mNativeDisplayView;
    private SDKSurfaceView mNativeDisplayView1;
    private SDKSurfaceView mNativeDisplayView2;
    private SDKSurfaceView mNativeDisplayView3;
    private boolean bOpenVideo = false;

    private boolean bRealPlayPause = true;
    private STEnumType.STStreamType stream_type = STEnumType.STStreamType.SUB;

    private boolean bSwitch = false;
    private boolean bRecordVideo = false;

    public BackupRealPlayActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_backup);
        initView();
        initOpreation();
        STVideoDecodeType.setDecodeType(STVideoDecodeType.HARD);
    }

    private void initView() {
        mVideoBtn = findViewById(R.id.btn_video);
        mSwitchBtn = findViewById(R.id.btn_switch_stream);
        mSoundBtn = findViewById(R.id.btn_sound);
        mPauseBtn = findViewById(R.id.btn_pause);
        mGrabPicBtn = findViewById(R.id.btn_grab);
        mSaveVideoBtn = findViewById(R.id.btn_save_video);
        mMsgTV = findViewById(R.id.tv_msg_info);
        mImageView = findViewById(R.id.imageView);
        mNativeDisplayView = findViewById(R.id.nativesurfaceView);
        mNativeDisplayView1 = findViewById(R.id.nativesurfaceView1);
        mNativeDisplayView2 = findViewById(R.id.nativesurfaceView2);
        mNativeDisplayView3 = findViewById(R.id.nativesurfaceView3);

        mVideoBtn.setOnClickListener(mClickListener);
        mSwitchBtn.setOnClickListener(mClickListener);
        mSoundBtn.setOnClickListener(mClickListener);
        mPauseBtn.setOnClickListener(mClickListener);
        mGrabPicBtn.setOnClickListener(mClickListener);
        mSaveVideoBtn.setOnClickListener(mClickListener);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initOpreation() {
        mNativeDisplayView.setOnTouchListener(new OnDoubleClickListener(new OnDoubleClickListener.DoubleClickCallback() {
            @Override
            public void onDoubleClick() {
                Intent intent = new Intent(BackupRealPlayActivity.this, FullVideoActivity.class);
                intent.putExtra("CHANNEL", 2);
                intent.putExtra("PLAYMODE", "REALPLAY");
                startActivity(intent);
            }
        }));
        mNativeDisplayView1.setOnTouchListener(new OnDoubleClickListener(new OnDoubleClickListener.DoubleClickCallback() {
            @Override
            public void onDoubleClick() {
                Intent intent = new Intent(BackupRealPlayActivity.this, FullVideoActivity.class);
                intent.putExtra("CHANNEL", 1);
                intent.putExtra("PLAYMODE", "REALPLAY");
                startActivity(intent);
            }
        }));
    }

    @Override
    protected void onResume() {
        Log.e(TAG, "RealPlay Activity is onResume.");
        if (null == netStreamCH1) {
            Log.d(TAG, "surface view create callback, netStreamCH1");
            netStreamCH1 = SDKManager.getInstance().getRealStream(0);
        }
        if (null == netStreamCH2) {
            Log.d(TAG, "surface view create callback, netStreamCH2");
            netStreamCH2 = SDKManager.getInstance().getRealStream(1);
        }
        netStreamCH1.switchSurface(mNativeDisplayView);
        netStreamCH2.switchSurface(mNativeDisplayView1);
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

    public View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_video:
                    onOpenVideo();
                    break;
                case R.id.btn_switch_stream:
                    onSwitchStream();
                    break;
                case R.id.btn_sound:
                    onOpenSound();
                    break;
                case R.id.btn_pause:
                    onPausePlay();
                    break;
                case R.id.btn_grab:
                    onGrabPic();
                    break;
                case R.id.btn_save_video:
                    onSaveVideoStream();
                    break;
                default:
                    break;
            }
        }
    };

    private void onOpenVideo() {
        if (bOpenVideo) {
            netStreamCH1.closeStream();
            netStreamCH2.closeStream();
//            netStreamCH3.closeStream();
//            netStreamCH4.closeStream();
        } else {
            netStreamCH1 = SDKManager.getInstance().getRealStream(2);
            netStreamCH1.openStream(2, STEnumType.STStreamType.SUB, mNativeDisplayView, STEnumType.STRotateType.NOMAL, false); //STEnumType.STRotateType.ROTATE90
            mNativeDisplayView.setSurfaceOnLifeCycle(this, 2);
            netStreamCH2 = SDKManager.getInstance().getRealStream(1);
            netStreamCH2.openStream(1, STEnumType.STStreamType.SUB, mNativeDisplayView1, STEnumType.STRotateType.NOMAL, false);
            mNativeDisplayView1.setSurfaceOnLifeCycle(this, 1);

//            netStreamCH3 = SDKManager.getInstance().getRealStream(2);
//            netStreamCH3.openStream(2, STEnumType.STStreamType.MAIN, mNativeDisplayView2);
//            netStreamCH4 = SDKManager.getInstance().getRealStream(3);
//            netStreamCH4.openStream(3, STEnumType.STStreamType.MAIN, mNativeDisplayView3);

            if (null != netStreamCH1) {
                    netStreamCH1.openSound();
                netStreamCH1.registerPlaybackMsgCallback(new STNetStreamCallback() {
                    @Override
                    public void netstreamCallback(STNetDevMsgType msgType, byte[] data, int nLen, int param) {
                        if (null != data && data.length >= nLen) {
                            String str;
                            try {
                                str = new String(data, 0, nLen, "UTF-8");
                                STLogUtils.e(TAG, "stream Callback = " + str);
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        }
        if (bOpenVideo) {
            bOpenVideo = false;
            mVideoBtn.setText(R.string.button_openvideo);
        } else {
            bOpenVideo = true;
            mVideoBtn.setText(R.string.button_closevideo);
        }
    }

    private void onSwitchStream() {
        if (!bOpenVideo) {
            return;
        }
        netStreamCH1.switchStream(stream_type);
        netStreamCH2.switchStream(stream_type);
        if (STEnumType.STStreamType.SUB == stream_type) {
            stream_type = STEnumType.STStreamType.MAIN;
            mSwitchBtn.setText("主码流");
        } else {
            stream_type = STEnumType.STStreamType.SUB;
            mSwitchBtn.setText("子码流");
        }
    }

    private void onOpenSound() {
        if (!bOpenVideo) {
            return;
        }
        if (!bSwitch) {
            netStreamCH1.closeSound();
//            netStreamCH2.closeSound();
//            netStreamCH3.closeSound();
//            netStreamCH4.closeSound();
            bSwitch = true;
            mSoundBtn.setText("开声音");
        } else {
            netStreamCH1.openSound();
//            netStreamCH2.closeSound();
//            netStreamCH4.closeSound();
//            netStreamCH3.closeSound();
            bSwitch = false;
            mSoundBtn.setText("关声音");
        }
    }

    private void onPausePlay() {
        if (!bOpenVideo) {
            return;
        }
        netStreamCH1.pauseStream(bRealPlayPause);
        netStreamCH2.pauseStream(bRealPlayPause);
//            int ret = netStreamCH3.pauseStream(bRealPlayPause);
//            int ret = netStreamCH4.pauseStream(bRealPlayPause);
        mPauseBtn.setText(bRealPlayPause ? "播放" : "暂停");
        bRealPlayPause = !bRealPlayPause;
    }

    private void onGrabPic() {
        if (!bOpenVideo) {
            return;
        }
        STResponseData data = netStreamCH1.grabLocalPicture("/mnt/sdcard/");
        mMsgTV.setText(data.getResponseStr());
        if (STErrorCode.ERROR_SUCCESS == data.getError()) {
            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(data.getResponseStr());
                JSONObject param = jsonObject.getJSONObject("PARAM");
                Bitmap bitmap = BitmapFactory.decodeFile(param.getString("PICNAME"));
                mImageView.setImageBitmap(bitmap);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void onSaveVideoStream() {
        if (!bOpenVideo) {
            return;
        }
        if (!bRecordVideo) {
            if (STErrorCode.ERROR_SUCCESS == netStreamCH1.startRecord("/mnt/sdcard/AAAA.h264")) {
                bRecordVideo = true;
                mSaveVideoBtn.setText("停止");
            }
        } else {
            netStreamCH1.stopRecord();
            bRecordVideo = false;
            mSaveVideoBtn.setText("录制");
        }
    }

    public void onBackPressed() {
        if (bRecordVideo) {
            netStreamCH1.stopRecord();
        }
        if (bOpenVideo) {
            netStreamCH1.closeStream();
            netStreamCH2.closeStream();
        }
        finish();
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
        Log.e(TAG, "surfaceview create callback channel = " + channel);
        if (2 == channel && null != netStreamCH1) {
            Log.d(TAG, "surface view create callback, netStreamCH1 ch = " + channel);
            netStreamCH1.switchSurface(mNativeDisplayView);
            netStreamCH1.switchStream(STEnumType.STStreamType.SUB);
        } else if (1 == channel && null != netStreamCH2) {
            Log.d(TAG, "surface view create callback, netStreamCH2 ch = " + channel);
            netStreamCH2.switchSurface(mNativeDisplayView1);
            netStreamCH2.switchStream(STEnumType.STStreamType.SUB);
        }
    }
}
