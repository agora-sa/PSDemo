package com.streamax.mmiddlewaredemo;

import android.annotation.SuppressLint;

import com.streamax.localStream.LocalStream;
import com.streamax.netdevice.STNetDevice;
import com.streamax.netdownloadfile.STNetDownloadFile;
import com.streamax.netdownloadrecordfile.STNetDownloadRecord;
import com.streamax.netplayback.STNetPlayback;
import com.streamax.netsearch.STNetSearch;
import com.streamax.netstream.STNetStream;

import java.util.HashMap;
import java.util.Map;

public class SDKManager {
    private static SDKManager instance = new SDKManager();

    @SuppressLint("UseSparseArrays")
    private Map<Integer, STNetStream> mRealPlayStreamMap = new HashMap<>();

    private STNetDevice mNetDevice;
    private STNetSearch mSTNetSearch;
    private STNetPlayback mNetPlayback;
    private STNetDownloadRecord mDownloadRecord = null;
    private STNetDownloadFile mDownloadFile = null;
    private LocalStream mLocalStream;

    public STNetDownloadRecord getDownloadRecord() {
        return mDownloadRecord;
    }

    public STNetDownloadFile getDownloadFile() {
        return mDownloadFile;
    }

    private SDKManager() {
        mNetDevice = new STNetDevice();
        mSTNetSearch = new STNetSearch(mNetDevice);
        mNetPlayback = new STNetPlayback(mNetDevice);
        mDownloadRecord = new STNetDownloadRecord(mNetDevice);
        mDownloadFile = new STNetDownloadFile(mNetDevice);
        mLocalStream = new LocalStream();
    }

    public static SDKManager getInstance() {
        return instance;
    }

    public STNetDevice getNetDevice() {
        return mNetDevice;
    }

    public STNetPlayback getNetPlayback() {
        return mNetPlayback;
    }

    public STNetSearch getSTNetSearch() {
        return mSTNetSearch;
    }

    public LocalStream getLocalStream() {
        return mLocalStream;
    }

    public STNetStream getRealStream(int nChannel) {
        STNetStream stream = mRealPlayStreamMap.get(nChannel);
        if(null == stream) {
            stream = new STNetStream(mNetDevice);
            mRealPlayStreamMap.put(nChannel, stream);
        }
        return stream;
    }
}
