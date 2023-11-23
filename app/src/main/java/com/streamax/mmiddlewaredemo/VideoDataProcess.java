package com.streamax.mmiddlewaredemo;

import android.graphics.Matrix;
import android.util.Log;

import com.streamax.mmiddlewaredemo.dataprocess.YuvFboProgram;

import java.nio.ByteBuffer;
import java.util.concurrent.Callable;

import io.agora.base.JavaI420Buffer;
import io.agora.base.NV12Buffer;
import io.agora.base.NV21Buffer;
import io.agora.base.TextureBufferHelper;
import io.agora.base.VideoFrame;
import io.agora.base.internal.video.YuvHelper;
import io.agora.rtc2.gl.EglBaseProvider;

public class VideoDataProcess {

    private static final String TAG = "VideoDataProcess";

    private String selectedItem;
    private YuvFboProgram yuvFboProgram;
    private TextureBufferHelper textureBufferHelper;

    public VideoDataProcess(String selectItem) {
        selectedItem = selectItem;
    }

    public VideoFrame.Buffer buildVideoFrame(byte[] data, int width, int height) {
        VideoFrame.Buffer frameBuffer;
        if ("NV21".equals(selectedItem)) {
            int srcStrideY = width;
            int srcHeightY = height;
            int srcSizeY = srcStrideY * srcHeightY;
            ByteBuffer srcY = ByteBuffer.allocateDirect(srcSizeY);
            srcY.put(data, 0, srcSizeY);

            int srcStrideU = width / 2;
            int srcHeightU = height / 2;
            int srcSizeU = srcStrideU * srcHeightU;
            ByteBuffer srcU = ByteBuffer.allocateDirect(srcSizeU);
            srcU.put(data, srcSizeY, srcSizeU);

            int srcStrideV = width / 2;
            int srcHeightV = height / 2;
            int srcSizeV = srcStrideV * srcHeightV;
            ByteBuffer srcV = ByteBuffer.allocateDirect(srcSizeV);
            srcV.put(data, srcSizeY + srcSizeU, srcSizeV);

            int desSize = srcSizeY + srcSizeU + srcSizeV;
            ByteBuffer des = ByteBuffer.allocateDirect(desSize);
            YuvHelper.I420ToNV12(srcY, srcStrideY, srcV, srcStrideV, srcU, srcStrideU, des, width, height);

            byte[] nv21 = new byte[desSize];
            des.position(0);
            des.get(nv21);

            frameBuffer = new NV21Buffer(nv21, width, height, null);
        } else if ("NV12".equals(selectedItem)) {
            int srcStrideY = width;
            int srcHeightY = height;
            int srcSizeY = srcStrideY * srcHeightY;
            ByteBuffer srcY = ByteBuffer.allocateDirect(srcSizeY);
            srcY.put(data, 0, srcSizeY);

            int srcStrideU = width / 2;
            int srcHeightU = height / 2;
            int srcSizeU = srcStrideU * srcHeightU;
            ByteBuffer srcU = ByteBuffer.allocateDirect(srcSizeU);
            srcU.put(data, srcSizeY, srcSizeU);

            int srcStrideV = width / 2;
            int srcHeightV = height / 2;
            int srcSizeV = srcStrideV * srcHeightV;
            ByteBuffer srcV = ByteBuffer.allocateDirect(srcSizeV);
            srcV.put(data, srcSizeY + srcSizeU, srcSizeV);

            int desSize = srcSizeY + srcSizeU + srcSizeV;
            ByteBuffer des = ByteBuffer.allocateDirect(desSize);
            YuvHelper.I420ToNV12(srcY, srcStrideY, srcU, srcStrideU, srcV, srcStrideV, des, width, height);

            frameBuffer = new NV12Buffer(width, height, width, height, des, null);
        } else if ("Texture2D".equals(selectedItem)) {
            if (textureBufferHelper == null) {
                textureBufferHelper = TextureBufferHelper.create("PushExternalVideoYUV", EglBaseProvider.instance().getRootEglBase().getEglBaseContext());
            }
            if (yuvFboProgram == null) {
                textureBufferHelper.invoke((Callable<Void>) () -> {
                    yuvFboProgram = new YuvFboProgram();
                    return null;
                });
            }
            Integer textureId = textureBufferHelper.invoke(() -> yuvFboProgram.drawYuv(data, width, height));
            frameBuffer = textureBufferHelper.wrapTextureBuffer(width, height, VideoFrame.TextureBuffer.Type.RGB, textureId, new Matrix());
        } else {
            // I420 type default
            JavaI420Buffer i420Buffer = JavaI420Buffer.allocate(width, height);
            i420Buffer.getDataY().put(data, 0, i420Buffer.getDataY().limit());
            i420Buffer.getDataU().put(data, i420Buffer.getDataY().limit(), i420Buffer.getDataU().limit());
            i420Buffer.getDataV().put(data, i420Buffer.getDataY().limit() + i420Buffer.getDataU().limit(), i420Buffer.getDataV().limit());
            frameBuffer = i420Buffer;
        }

        return frameBuffer;
    }
}
