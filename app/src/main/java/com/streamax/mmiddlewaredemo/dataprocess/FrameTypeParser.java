package com.streamax.mmiddlewaredemo.dataprocess;

public class FrameTypeParser {

    public static void parseFrameType(byte[] data) {
        // 查找H.264 NAL单元的起始码
        int nalStartIndex = findNALStartCode(data);

        if (nalStartIndex != -1) {
            // 解析NAL单元类型
            int nalUnitType = data[nalStartIndex + 4] & 0x1F;

            // 判断帧类型
            if (isVideoFrame(nalUnitType)) {
                System.out.println("parseFrameType---Frame Type: Video Frame");
            } else if (isAudioFrame(nalUnitType)) {
                System.out.println("parseFrameType---Frame Type: Audio Frame");
            } else {
                System.out.println("parseFrameType---Frame Type: Unknown");
            }

            // 解析时间戳（假设时间戳占据4个字节）
            int timestampIndex = nalStartIndex + 5;
            long timestamp = ((long) (data[timestampIndex] & 0xFF) << 24) |
                    ((data[timestampIndex + 1] & 0xFF) << 16) |
                    ((data[timestampIndex + 2] & 0xFF) << 8) |
                    (data[timestampIndex + 3] & 0xFF);
            System.out.println("parseFrameType---Timestamp: " + timestamp);
        } else {
            System.out.println("Invalid H.264 data");
        }
    }

    private static int findNALStartCode(byte[] data) {
        for (int i = 0; i < data.length - 4; i++) {
            if (data[i] == 0x00 && data[i + 1] == 0x00 && data[i + 2] == 0x00 && data[i + 3] == 0x01) {
                return i;
            }
        }
        return -1;
    }

    private static boolean isVideoFrame(int nalUnitType) {
        // 视频帧类型可以根据具体的编码标准来定义
        // 例如，H.264中，5表示关键帧，1表示非关键帧
        return nalUnitType == 5 || nalUnitType == 1;
    }

    private static boolean isAudioFrame(int nalUnitType) {
        // 这里只是一个示例，实际情况需要根据具体的音频编码标准来定义
        // 对于AAC，可以查阅AAC编码标准文档，根据AAC帧头信息判断
        return false;
    }

    private static boolean isADTSPacket(byte[] data) {
        // ADTS头的前4个字节是固定的，可以根据AAC标准检查这些字节来判断
        return (data[0] & 0xFF) == 0xFF && (data[1] & 0xF0) == 0xF0;
    }
}

