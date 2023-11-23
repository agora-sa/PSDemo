# 概述
    1、本项目是为了集成并调试公安项目中摄像头厂商的设备，通过厂商的摄像头完成视频的采集
    然后将采集到的视频推送到声网的频道中，实现一个实时的流传输。

    2、因为厂商只能提供出编码后的视频数据（H264）,所以这里我们使用pushExternalEncodedVideoFrameEx的方式进行推送

    3、主要代码见 RealPlayActivity，其中push的关键代码使用注释表明，可以搜索 "// push key"查看

# 注意事项：
    1、本实例调试必须连接厂商的设备进行。
    2、必须在客户的私有化环境中进行。
    3、厂商回调回来的视频流，如果是主码流，延时会比较大，在4-6S之间
        如果需要降低延时，需要切换成子码流，延时大概在1-1.5S
    4、目前尝试采集回调回来的数据中只有视频数据，没有音频数据，厂商目前还没有实现。


