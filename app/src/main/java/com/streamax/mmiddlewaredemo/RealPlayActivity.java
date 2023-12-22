package com.streamax.mmiddlewaredemo;

import static io.agora.rtc2.video.VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15;
import static io.agora.rtc2.video.VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE;
import static io.agora.rtc2.video.VideoEncoderConfiguration.STANDARD_BITRATE;
import static io.agora.rtc2.video.VideoEncoderConfiguration.VD_960x540;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;

import com.streamax.common.STEnumType;
import com.streamax.common.STVideoDecodeType;
import com.streamax.localStream.LocalStream;
import com.streamax.mmiddlewaredemo.dataprocess.FileUtils;
import com.streamax.mmiddlewaredemo.dataprocess.FrameTypeParser;
import com.streamax.netdevice.STNetDeviceCallback;
import com.streamax.netdevice.devtype.STNetDevMsgType;
import com.streamax.nxmapi.base.NXMSystemApi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.EncodedVideoTrackOptions;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.RtcEngineEx;
import io.agora.rtc2.audio.AudioTrackConfig;
import io.agora.rtc2.proxy.LocalAccessPointConfiguration;
import io.agora.rtc2.video.EncodedVideoFrameInfo;
import io.agora.rtc2.video.VideoEncoderConfiguration;

public class RealPlayActivity extends AppCompatActivity implements STNetDeviceCallback, SDKSurfaceView.SurfaceLifeCycle {
    String TAG = "RealPlayActivity";

//    private STNetStream netStreamCH1;
//    private STNetStream netStreamCH2;
//    private STNetStream netStreamCH3;
//    private STNetStream netStreamCH4;

    private AppCompatEditText mChannelNameText;
    private AppCompatButton mChannelBtn;
    private static FileOutputStream directSavePcmFos = null;

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


    private RtcEngineEx engine;
    private volatile boolean joined = false;
    private String mChannelId = "123456";
    private int myUid;
    // private VideoDataProcess videoDataProcess;
    private Handler mHandler;
    private boolean isAccessPoint = true;
    private LocalStream mLocalStream;
    private boolean isH264;
    private int videoTrackId;
    private int customAudioTrack = -1;

    public static final int SAMPLE_RATE = 16000;
    public static final int SAMPLE_NUM_OF_CHANNEL = 1;
    public static final int BITS_PER_SAMPLE = 16;
    private int pushVideoTimes;
    private int pushTimes;

    private final static String USB_DIR = "/sdcard/Android/data/io.agora.publicsecurity/files";
    private final static String FILE_H264_FORMAT = "channel_%d.h264";

    public RealPlayActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NXMSystemApi.initialize(this);
        NXMSystemApi.startService();

        intiPermission();
        initEngine();
        initView();
        initOpreation();
        STVideoDecodeType.setDecodeType(STVideoDecodeType.HARD);
        initDataProcess();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                onJoinChannel();
            }
        }, 200);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                onOpenVideo();
            }
        }, 300);
    }

    private void initEngine() {
        try {
            RtcEngineConfig config = new RtcEngineConfig();
            config.mContext = RealPlayActivity.this.getApplicationContext();
            config.mAppId = "d406cf8ebf784bc88fcd2cc399128b76";
            config.mChannelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING;
            config.mEventHandler = iRtcEngineEventHandler;
            config.mAudioScenario = Constants.AudioScenario.getValue(Constants.AudioScenario.DEFAULT);
            config.mAreaCode = RtcEngineConfig.AreaCode.AREA_CODE_CN;
            engine = (RtcEngineEx) RtcEngine.create(config);

            // push key:1、创建一个正确的trackId（createCustomEncodedVideoTrack）
            EncodedVideoTrackOptions opt = new EncodedVideoTrackOptions();
            opt.codecType = Constants.VIDEO_CODEC_H264;
            videoTrackId = engine.createCustomEncodedVideoTrack(opt);

            AudioTrackConfig config1 = new AudioTrackConfig();
            config1.enableLocalPlayback = false;
            customAudioTrack = engine.createCustomAudioTrack(Constants.AudioTrackType.AUDIO_TRACK_MIXABLE, config1);

            try {
                File directSavePcmFile = new File("/sdcard/Android/data/io.agora.publicsecurity/files/output_direct.pcm");
                directSavePcmFos = new FileOutputStream(directSavePcmFile, true); // 使用追加模式
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
            onBackPressed();
        }
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

        mChannelNameText = findViewById(R.id.et_channel);
        mChannelBtn = findViewById(R.id.btn_join);

        mVideoBtn.setOnClickListener(mClickListener);
        mSwitchBtn.setOnClickListener(mClickListener);
        mSoundBtn.setOnClickListener(mClickListener);
        mPauseBtn.setOnClickListener(mClickListener);
        mGrabPicBtn.setOnClickListener(mClickListener);
        mSaveVideoBtn.setOnClickListener(mClickListener);

        mChannelBtn.setOnClickListener(mClickListener);
    }

    private void initDataProcess() {
        mHandler = new Handler(Looper.getMainLooper());
        // videoDataProcess = new VideoDataProcess("NV21");
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initOpreation() {
        mNativeDisplayView.setOnTouchListener(new OnDoubleClickListener(new OnDoubleClickListener.DoubleClickCallback() {
            @Override
            public void onDoubleClick() {
                Intent intent = new Intent(RealPlayActivity.this, FullVideoActivity.class);
                intent.putExtra("CHANNEL", 2);
                intent.putExtra("PLAYMODE", "REALPLAY");
                startActivity(intent);
            }
        }));
        mNativeDisplayView1.setOnTouchListener(new OnDoubleClickListener(new OnDoubleClickListener.DoubleClickCallback() {
            @Override
            public void onDoubleClick() {
                Intent intent = new Intent(RealPlayActivity.this, FullVideoActivity.class);
                intent.putExtra("CHANNEL", 1);
                intent.putExtra("PLAYMODE", "REALPLAY");
                startActivity(intent);
            }
        }));
    }

    @Override
    protected void onResume() {
//        Log.e(TAG, "RealPlay Activity is onResume.");
//        if (null == netStreamCH1) {
//            Log.d(TAG, "surface view create callback, netStreamCH1");
//            netStreamCH1 = SDKManager.getInstance().getRealStream(0);
//        }
//        if (null == netStreamCH2) {
//            Log.d(TAG, "surface view create callback, netStreamCH2");
//            netStreamCH2 = SDKManager.getInstance().getRealStream(1);
//        }
//        netStreamCH1.switchSurface(mNativeDisplayView);
//        netStreamCH2.switchSurface(mNativeDisplayView1);
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (customAudioTrack != -1) {
            engine.destroyCustomAudioTrack(customAudioTrack);
            customAudioTrack = -1;
        }
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
                case R.id.btn_join:
                    onJoinChannel();
                    break;
                default:
                    break;
            }
        }
    };

    private void onOpenVideo() {
        if (bOpenVideo) {
//            netStreamCH1.closeStream();
//            netStreamCH2.closeStream();
//            netStreamCH3.closeStream();
//            netStreamCH4.closeStream();
            mLocalStream.close();
        } else {
//            netStreamCH1 = SDKManager.getInstance().getRealStream(2);
//            int ret1 = netStreamCH1.openStream(2, STEnumType.STStreamType.SUB, mNativeDisplayView, STEnumType.STRotateType.NOMAL, false); //STEnumType.STRotateType.ROTATE90
//            Log.d(TAG, "open stream ret = " + ret1);
//            mNativeDisplayView.setSurfaceOnLifeCycle(this, 2);
//            netStreamCH2 = SDKManager.getInstance().getRealStream(1);
//            int ret2 = netStreamCH2.openStream(1, STEnumType.STStreamType.SUBANDMAIN, mNativeDisplayView1, STEnumType.STRotateType.NOMAL, false);
//            Log.d(TAG, "open stream ret = " + ret2);
//            mNativeDisplayView1.setSurfaceOnLifeCycle(this, 1);

//            netStreamCH3 = SDKManager.getInstance().getRealStream(2);
//            netStreamCH3.openStream(2, STEnumType.STStreamType.MAIN, mNativeDisplayView2);
//            netStreamCH4 = SDKManager.getInstance().getRealStream(3);
//            netStreamCH4.openStream(3, STEnumType.STStreamType.MAIN, mNativeDisplayView3);

//            if (null != netStreamCH2) {
//                netStreamCH2.openSound();
//                netStreamCH2.registerPlaybackMsgCallback(new STNetStreamCallback() {
//                    @Override
//                    public void netstreamCallback(STNetDevMsgType msgType, byte[] data, int nLen, int param) {
//                        // 这里实现将自采集的视频流推送到声网RTC的频道中
//                        Log.d(TAG, "msgType=" + ((msgType == null) ? "null" : msgType.name()) + " , nLen=" + nLen + " , param=" + param);
//
//                    }
//                });
//            }

            mLocalStream = SDKManager.getInstance().getLocalStream();
            mLocalStream.openWithStream(1, LocalStream.RecvDataTypeH264_SUB, (buffer, length, frameType, frame_pts) -> {
                Log.d(TAG, "---> onStreamData chn = 1, length = " + length + ", frameType = " + frameType + ", frame_pts = " + frame_pts);
                // 将摄像头采集到的数据保存成H264文件
                // FileUtils.writeToFile(isH264?LocalStream.filterRMFrameHead(buffer):buffer,USB_DIR,String.format(FILE_H264_FORMAT,1),true,false);

                if (frameType == 0 || frameType == 1) {
                    // push key:4、push，但是必须要将数据转成Direct的
                    // 将摄像头采集到的数据转换成Direct的byteBuffer数据
                    byte[] newByte = LocalStream.filterRMFrameHead(buffer);
                    ByteBuffer byteBuffer = ByteBuffer.allocateDirect(newByte.length);
                    byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                    byteBuffer.put(newByte);
                    byteBuffer.flip();

                    // 将摄像头采集到的数据push到声网的频道中
                    int ret = engine.pushExternalEncodedVideoFrameEx(byteBuffer, buildEncodedVideoFrame(), videoTrackId);
                    Log.i(TAG, "pushExternalEncodedVideoFrameEx times:" + (++pushVideoTimes) + ", ret=" + ret);
                    if (ret != Constants.ERR_OK) {
                        Log.e(TAG, "pushExternalEncodedVideoFrame error: " + ret);
                    }
                } else if (frameType == 2) {
                    // appendToPcmFileForByteArray(buffer);
                    if(joined && engine != null && customAudioTrack != -1){
                        int ret = engine.pushExternalAudioFrame(buffer, frame_pts, SAMPLE_RATE, SAMPLE_NUM_OF_CHANNEL, Constants.BytesPerSample.TWO_BYTES_PER_SAMPLE, customAudioTrack);
                        Log.i(TAG, "pushExternalAudioFrame times:" + (++pushTimes) + ", ret=" + ret);
                    }
                }

            });
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
//        netStreamCH2.switchStream(stream_type);
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
//            netStreamCH1.closeSound();
//            netStreamCH2.closeSound();
//            netStreamCH3.closeSound();
//            netStreamCH4.closeSound();
            bSwitch = true;
            mSoundBtn.setText("开声音");
        } else {
//            netStreamCH1.openSound();
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
//        netStreamCH1.pauseStream(bRealPlayPause);
//        netStreamCH2.pauseStream(bRealPlayPause);
//            int ret = netStreamCH3.pauseStream(bRealPlayPause);
//            int ret = netStreamCH4.pauseStream(bRealPlayPause);
        mPauseBtn.setText(bRealPlayPause ? "播放" : "暂停");
        bRealPlayPause = !bRealPlayPause;
    }

    private void onGrabPic() {
        if (!bOpenVideo) {
            return;
        }
//        STResponseData data = netStreamCH1.grabLocalPicture("/mnt/sdcard/");
//        mMsgTV.setText(data.getResponseStr());
//        if (STErrorCode.ERROR_SUCCESS == data.getError()) {
//            JSONObject jsonObject;
//            try {
//                jsonObject = new JSONObject(data.getResponseStr());
//                JSONObject param = jsonObject.getJSONObject("PARAM");
//                Bitmap bitmap = BitmapFactory.decodeFile(param.getString("PICNAME"));
//                mImageView.setImageBitmap(bitmap);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
    }

    private void onSaveVideoStream() {
//        if (!bOpenVideo) {
//            return;
//        }
//        if (!bRecordVideo) {
//            if (STErrorCode.ERROR_SUCCESS == netStreamCH1.startRecord("/mnt/sdcard/AAAA.h264")) {
//                bRecordVideo = true;
//                mSaveVideoBtn.setText("停止");
//            }
//        } else {
//            netStreamCH1.stopRecord();
//            bRecordVideo = false;
//            mSaveVideoBtn.setText("录制");
//        }
    }

    private void onJoinChannel() {
        if (!joined) {
            mChannelId = mChannelNameText.getText().toString();
            hideInputBoard(RealPlayActivity.this, mChannelNameText);
            requestPermissions();
        } else {
            mVideoBtn.setEnabled(false);
            mSwitchBtn.setEnabled(false);
            mSoundBtn.setEnabled(false);
            mPauseBtn.setEnabled(false);
            mGrabPicBtn.setEnabled(false);
            mSaveVideoBtn.setEnabled(false);
            mChannelBtn.setEnabled(true);
            leaveChannel();
        }
    }

    public void onBackPressed() {
        if (bRecordVideo) {
//             netStreamCH1.stopRecord();
        }
        if (bOpenVideo) {
//            netStreamCH1.closeStream();
//            netStreamCH2.closeStream();
            mLocalStream.close();
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
//        if (2 == channel && null != netStreamCH1) {
//            Log.d(TAG, "surface view create callback, netStreamCH1 ch = " + channel);
//            netStreamCH1.switchSurface(mNativeDisplayView);
//            netStreamCH1.switchStream(STEnumType.STStreamType.SUB);
//        } else
//        if (1 == channel && null != netStreamCH2) {
//            Log.d(TAG, "surface view create callback, netStreamCH2 ch = " + channel);
//            netStreamCH2.switchSurface(mNativeDisplayView1);
//            netStreamCH2.switchStream(STEnumType.STStreamType.SUB);
//        }
    }


    // 是否同意了权限
    protected boolean isRequested = false;
    private ActivityResultLauncher<String[]> requestMultiplePermissionsLauncher;
    private final String[] permissionsToRequest = {
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    protected void requestPermissions() {
        // 检查未被授予的权限
        if (!isRequested) {
            // 请求未被授予的权限
            requestMultiplePermissionsLauncher.launch(permissionsToRequest);
        } else {
            joinChannel();
        }
    }

    private void intiPermission() {
        // 初始化 ActivityResultLauncher
        requestMultiplePermissionsLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                    if (Boolean.TRUE.equals(result.get("android.permission.CAMERA"))
                            && Boolean.TRUE.equals(result.get("android.permission.RECORD_AUDIO"))
                            && Boolean.TRUE.equals(result.get("android.permission.WRITE_EXTERNAL_STORAGE"))
                            && Boolean.TRUE.equals(result.get("android.permission.READ_EXTERNAL_STORAGE"))) {
                        // 获取到权限
                        joinChannel();
                        isRequested = true;
                    } else {
                        // 未获取到权限
                        isRequested = false;
                        Log.d("MainActivity", "使用demo需要 摄像头、录音、存储权限！");
                    }
                });
    }

    public void hideInputBoard(Activity activity, EditText editText) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    private void joinChannel() {
        if (isAccessPoint) {
            engine.setParameters("{\"rtc.enableMultipath\":true}");

            engine.setParameters("{\"rtc.local_domain\":\"ap.1226191.agora.local\"}");
            LocalAccessPointConfiguration config = new LocalAccessPointConfiguration();
            ArrayList<String> iplist = new ArrayList<>();
            iplist.add("20.1.124.137");
            config.ipList = iplist;
            config.mode = 1;
            config.verifyDomainName = "ap.1226191.agora.local";
            engine.setLocalAccessPoint(config);
            Log.d("SSSSS", "enable cus");
        } else {
            engine.setLocalAccessPoint(new LocalAccessPointConfiguration());
            Log.d("SSSSS", "disable cus");
        }

        engine.setDefaultAudioRoutetoSpeakerphone(true);
        engine.setClientRole(Constants.CLIENT_ROLE_BROADCASTER);
        // Enables the video module.
        engine.enableVideo();
        // Setup video encoding configs
        engine.setVideoEncoderConfiguration(new VideoEncoderConfiguration(
                VD_960x540,
                FRAME_RATE_FPS_15,
                STANDARD_BITRATE,
                ORIENTATION_MODE_ADAPTIVE
        ));
        // push key:2、设置一个正确的sourceType，这里是ENCODED_VIDEO_FRAME
        engine.setExternalVideoSource(true, false, Constants.ExternalVideoSourceType.ENCODED_VIDEO_FRAME);

        TokenUtils.gen(RealPlayActivity.this, mChannelId, 0, accessToken -> {
            ChannelMediaOptions option = new ChannelMediaOptions();
            option.autoSubscribeAudio = true;
            option.autoSubscribeVideo = true;
            option.publishCustomVideoTrack = true;
            // push key:3、不要忘记设置这个trackId，否则会返回-1
            option.customVideoTrackId = videoTrackId;

            option.publishCustomAudioTrack = true;
            option.publishCustomAudioTrackId = customAudioTrack;
            int res = engine.joinChannel(accessToken, "12345678", 0, option);
            Log.d(TAG, "join channel res = " + res);
            if (res != 0) {
                Toast.makeText(RealPlayActivity.this, "login failure=" + res, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void leaveChannel() {
        joined = false;
        mChannelBtn.setText("加入RTC房间");
        if (null != engine) {
            engine.leaveChannel();
            engine.stopScreenCapture();
            engine.stopPreview();
        }
    }

    private final IRtcEngineEventHandler iRtcEngineEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onLeaveChannel(RtcStats stats) {
            super.onLeaveChannel(stats);
            Log.d(TAG, String.format("local user %d leaveChannel!", myUid));
        }

        @Override
        public void onError(int err) {
            super.onError(err);
            Log.d(TAG, "occur error is : " + err);
        }

        @Override
        public void onConnectionStateChanged(int state, int reason) {
            super.onConnectionStateChanged(state, reason);
            Log.d(TAG, "state : " + state + " , reason : " + reason);
        }

        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            Log.d(TAG, String.format("onJoinChannelSuccess channel %s uid %d", channel, uid));
            myUid = uid;
            joined = true;
            mHandler.post(() -> {
                mVideoBtn.setEnabled(true);
                mSwitchBtn.setEnabled(true);
                mSoundBtn.setEnabled(true);
                mPauseBtn.setEnabled(true);
                mGrabPicBtn.setEnabled(true);
                mSaveVideoBtn.setEnabled(true);
                mChannelBtn.setText("离开RTC频道");
            });
        }

        @Override
        public void onUserJoined(int uid, int elapsed) {
            super.onUserJoined(uid, elapsed);
            Log.i(TAG, "onUserJoined->" + uid);
        }

        @Override
        public void onUserOffline(int uid, int reason) {
            Log.i(TAG, String.format("user %d offline! reason:%d", uid, reason));
        }
    };

    private EncodedVideoFrameInfo buildEncodedVideoFrame() {
        EncodedVideoFrameInfo info = new EncodedVideoFrameInfo();
        info.codecType = Constants.VIDEO_CODEC_H264;
        info.framesPerSecond = 15;
        info.frameType = 4;
        return info;
    }

    public static void appendToPcmFileForByteArray(byte[] pcmData) {
        try {
            directSavePcmFos.write(pcmData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
