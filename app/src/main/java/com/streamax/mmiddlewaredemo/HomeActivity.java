package com.streamax.mmiddlewaredemo;

import android.content.Intent;
import android.os.Environment;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.streamax.common.STDownLoadType;
import com.streamax.common.STEnumType;
import com.streamax.common.STLogUtils;
import com.streamax.common.STQueryStatusType;
import com.streamax.common.STResponseData;
import com.streamax.common.STRestoreType;
import com.streamax.common.STSearchDiskType;
import com.streamax.decode.NativeSurfaceView;
import com.streamax.manager.STManager;
import com.streamax.netdevice.ChnSigInfo;
import com.streamax.netdevice.STNetDevice;
import com.streamax.netdevice.devtype.STAHDOprType;
import com.streamax.netdevice.devtype.STExternDevType;
import com.streamax.netdevice.devtype.STNetCtrlCmdType;
import com.streamax.netdevice.devtype.STNetDevBackupType;
import com.streamax.netdevice.devtype.STNetDevDiskType;
import com.streamax.netdevice.devtype.STNetDevExportFileType;
import com.streamax.netdevice.devtype.STNetDevFormatType;
import com.streamax.netdevice.devtype.STNetDevHWCtrlType;
import com.streamax.netdevice.devtype.STNetDevImportFileType;
import com.streamax.netdevice.devtype.STNetDevSMSType;
import com.streamax.netdevice.devtype.STNetDevSendFileType;
import com.streamax.netdevice.devtype.STNetDevUpgradeFileType;
import com.streamax.netdevice.devtype.STNetRecordVideoType;
import com.streamax.netdevice.devtype.STRemoteParamFileType;
import com.streamax.netdevice.devtype.STRmoteUpgradeType;
import com.streamax.netdevice.devtype.STStorageType;
import com.streamax.netdownloadfile.STHardDiskVersionType;
import com.streamax.netdownloadfile.STNetDownloadFile;
import com.streamax.netdownloadrecordfile.STNetDownloadRecord;
import java.util.HashMap;


public class HomeActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "HomeActivity";
    private Button mRealPlayBtn;
    private Button mPlayBackBtn;
    private Button mRunCommandBtn;
    private Spinner mCommondSpinner;
    private TextView mMsgShowTV;

    private STNetDevice mNetDevice = null;
    private STNetDownloadRecord mDownloadRecord = null;
    private STNetDownloadFile mDownloadFile = null;

    private String[] mCommondArr = null;
    private String mCommond = null;
    String stTime = "20181226112528";
    String endTime = "20181226235959";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mRealPlayBtn = findViewById(R.id.btn_realplay);
        mPlayBackBtn = findViewById(R.id.btn_playback);
        mRunCommandBtn = findViewById(R.id.btn_run_command);
        mCommondSpinner = findViewById(R.id.commond_spinner);
        mMsgShowTV = findViewById(R.id.tv_info_title);
        mRealPlayBtn.setOnClickListener(this);
        mPlayBackBtn.setOnClickListener(this);
        mRunCommandBtn.setOnClickListener(this);

        mNetDevice = SDKManager.getInstance().getNetDevice();
        mDownloadRecord = SDKManager.getInstance().getDownloadRecord();
        mDownloadFile = SDKManager.getInstance().getDownloadFile();
        mCommondArr = getResources().getStringArray(R.array.commond);
        mCommond = mCommondArr[0];

        mCommondSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int item, long l) {
                if (item < mCommondArr.length) {
                    mCommond = mCommondArr[item];
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        STLogUtils.d(TAG, "onDestroy--->>>STManager.uninitMiddleWare()");
        STManager.uninitMiddleWare();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_realplay:
                Intent realPlayIntent = new Intent(HomeActivity.this, RealPlayActivity.class);
                startActivity(realPlayIntent);
                break;
            case R.id.btn_playback:
                Intent playBackIntent = new Intent(HomeActivity.this, PlayBackActivity.class);
                startActivity(playBackIntent);
                break;
            case R.id.btn_run_command:
                runCommond(mCommond);
                break;
            default:
                break;
        }
    }

    public int runCommond(String comm) {
        STResponseData ret = null;
        int errorCode = -1;
        if (comm.equals(mCommondArr[0])) {
            //获取设备信息
            ret = mNetDevice.getDevInfoEX();
            STLogUtils.e(TAG, "RealPlayStopRecord ret = " + ret.getError() + "   msg = " + ret.getResponseStr());
        } else if (comm.equals(mCommondArr[1])) {
            //设置图像参数
//            int[] bright = {32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32};
//            int[] chorma = {32, 31, 31, 31, 31, 31, 31, 36, 31, 63, 31, 41, 32, 32, 32, 32};
//            int[] contrast = {32, 31, 31, 31, 31, 31, 31, 36, 31, 63, 31, 41, 32, 32, 32, 32};
//            int[] satura = {32, 31, 31, 31, 31, 31, 31, 36, 31, 63, 31, 41, 32, 32, 32, 32};
//            return mNetDevice.setVideoParam(65535, bright, chorma, contrast, satura);//(1<<16) -1
            int[] bright = {32};
            int[] chorma = {32};
            int[] contrast = {32};
            int[] satura = {32};
            errorCode = mNetDevice.setVideoParam(1, bright, chorma, contrast, satura);//(1<<16) -1
        } else if (comm.equals(mCommondArr[2])) {
            //设备配置参数获取
            ret = mNetDevice.getMDVRParam(STEnumType.STNetDevParamType.MDVR_GSP_TM);
        } else if (comm.equals(mCommondArr[3])) {
            //获取温度单位
            ret = mNetDevice.getMDVRParam(STEnumType.STNetDevParamType.MDVR_PCA);
            STLogUtils.e(TAG, "getMDVRParam ret = " + ret.getError() + "   ret = " + ret.getResponseStr());
        } else if (comm.equals(mCommondArr[4])) {
            //通用获取参数
            HashMap<String, Object> keyMap = new HashMap<>();
            HashMap<String, Object> paramMap = new HashMap<>();
            HashMap<String, Object> mdvrMap = new HashMap<>();
            paramMap.put("RIP", "?");
            mdvrMap.put("MDVR", paramMap);
            keyMap.put("PARAMETER", mdvrMap);
            Gson gson = new Gson();
            String strCMD = gson.toJson(keyMap);
            ret = mNetDevice.getDevParamInfo(strCMD);
            STLogUtils.e(TAG, "getDevParamInfo ret = " + ret);
        } else if (comm.equals(mCommondArr[6])) {
            //远程抓图
            long time = System.currentTimeMillis();
            final String path = Environment.getExternalStorageDirectory().getPath() + "/" + time + ".png";
            errorCode = mNetDevice.netSnapShot(1, path).getError();
        } else if (comm.equals(mCommondArr[7])) {
            //设备实时状态
            mNetDevice.setUploadStateInfo(true);
        } else if (comm.equals(mCommondArr[8])) {
            //获取IO状态
            ret = mNetDevice.getIOStatus();
            STLogUtils.e(TAG, "getIOStatus ret = " + ret.getError() + "   ret = " + ret.getResponseStr());
        } else if (comm.equals(mCommondArr[9])) {
            //获取设备UTC时间
            ret = mNetDevice.getDeviceUTCTime();
            STLogUtils.e(TAG, "getDeviceUTCTime ret = " + ret.getError() + "   ret = " + ret.getResponseStr());
        } else if (comm.equals(mCommondArr[10])) {
            //获取设备版本信息
            ret = mNetDevice.getDeviceVersionInfo();
            Log.e(TAG, ret.getError() + "   " + ret.getResponseStr());
        } else if (comm.equals(mCommondArr[11])) {
            //获取用户权限
            ret = mNetDevice.getUserRight();
            Log.e(TAG, ret.getError() + "   " + ret.getResponseStr());
        } else if (comm.equals(mCommondArr[12])) {
            //升级文件信息获取
            ret = mNetDevice.getUpgradeFileSwInfo();
            Log.e(TAG, ret.getError() + "   " + ret.getResponseStr());
        } else if (comm.equals(mCommondArr[13])) {
            //恢复出厂设置
            errorCode = mNetDevice.requestSetDefault(STRestoreType.DEVICE,0).getError();
            Log.e(TAG, "setDefaultConfig ret = " + ret);
        } else if (comm.equals(mCommondArr[14])) {
            //获取存储信息
            ret = mNetDevice.getDiskInfo();
            Log.e(TAG, ret.getError() + "   " + ret.getResponseStr());
        } else if (comm.equals(mCommondArr[15])) {
            //获取运维状态
            ret = mNetDevice.requestStatusData();
            Log.e(TAG, ret.getError() + "   " + ret.getResponseStr());
        } else if (comm.equals(mCommondArr[16])) {
            //故障日报表
            ret = mNetDevice.requestFaultReportData();
            Log.e(TAG, ret.getError() + "   " + ret.getResponseStr());
        } else if (comm.equals(mCommondArr[17])) {
            //历史故障日报表
            ret = mNetDevice.requestFaultReportDataHistory();
            Log.e(TAG, ret.getError() + "   " + ret.getResponseStr());
        } else if (comm.equals(mCommondArr[18])) {
            //运维验证
            ret = mNetDevice.getYunweiCheckInfoWithStartTime("20180317072344", "20180317072044");
            STLogUtils.e(TAG, "ret = " + ret.getError() + "   ret = " + ret.getResponseStr());
        } else if (comm.equals(mCommondArr[19])) {
            //获取硬件配置表
            ret = mNetDevice.getHWConfigTableInfo();
            Log.e(TAG, ret.getError() + "   " + ret.getResponseStr());
        } else if (comm.equals(mCommondArr[20])) {
            //控制硬件配置表
//            //登录
//            ret = mNetDevice.controlHWConfigTableInfo(STNetDevHWCtrlType.LOGIN, "122233", "", "");
//            //生成硬件配置表
//            ret = mNetDevice.controlHWConfigTableInfo(STNetDevHWCtrlType.CREATE, "122233", "", "");
//            STLogUtils.e(TAG, "ret = " + ret.getError() + "   ret = " + ret.getResponseStr());
            //修改密码
            ret = mNetDevice.controlHWConfigTableInfo(STNetDevHWCtrlType.EDITPASS, "admin", "djiofnd", "123456");
            STLogUtils.e(TAG, "ret = " + ret.getError() + "   ret = " + ret.getResponseStr());
        } else if (comm.equals(mCommondArr[21])) {
            //获取文件大小
            //获取导出文件大小
            ret = mNetDevice.getFileTotalSize(STEnumType.STFileDataType.VIDEO, STEnumType.STStreamType.MAIN, stTime, endTime, STNetDevDiskType.HDD, 3);
            STLogUtils.e(TAG, "ret = " + ret.getError() + "   ret = " + ret.getResponseStr());
        } else if (comm.equals(mCommondArr[22])) {
            //视频备份
            //备份视频
            ret = mNetDevice.backupRecordFile(STNetDevBackupType.AVI, "20181029100000", "20181029110000"
                    , 1, STEnumType.STStreamType.MAIN, STNetDevDiskType.HDD, STNetRecordVideoType.ALL_TYPE_RECORD);
            STLogUtils.e(TAG, "backupRecordFile ret = " + ret.getError() + "   ret = " + ret.getResponseStr());
            //获取备份进度
            mNetDevice.getBackupRecordFileProcess();
        } else if (comm.equals(mCommondArr[23])) {
            //导出文件
            ret = mNetDevice.exportFile(STNetDevExportFileType.GPSSOURCE_FILE, STNetDevExportFileType.STNetDevExportAlarmType.ALL,
                    "20181123000000", "20181123235959", true, STStorageType.UDISK);
            STLogUtils.e(TAG, "exportFile ret = " + ret.getError() + "   ret = " + ret.getResponseStr());
            //获取导出文件进度
            for (int i = 0; i < 5; i++) {
                STResponseData ret1 = mNetDevice.getExportFileProcess(STNetDevExportFileType.GPSSOURCE_FILE);
                STLogUtils.e(TAG, "getExportFileProcess ret1 = " + ret1.getError() + "   ret = " + ret1.getResponseStr());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else if (comm.equals(mCommondArr[24])) {
            //导入文件
            ret = mNetDevice.importFile(STNetDevImportFileType.GDSCONFIG_FILE);
            STLogUtils.e(TAG, "importFile ret = " + ret.getError() + "   ret = " + ret.getResponseStr());
            //获取导入文件进度
            for (int i = 0; i < 10; i++) {
                STResponseData ret1 = mNetDevice.getImportFileProcess();
                STLogUtils.e(TAG, "getExportFileProcess ret1 = " + ret1.getError() + "   ret = " + ret1.getResponseStr());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else if (comm.equals(mCommondArr[25])) {
            //主机设备升级
            ret = mNetDevice.upgradeFile(STNetDevUpgradeFileType.UPGRADE_IPC, 0);
            STLogUtils.e(TAG, "upgradeFile IPC ret = " + ret.getError() + "   ret = " + ret.getResponseStr());
            //查询升级进度
            STResponseData ret1 = mNetDevice.getUpgradeProcess(STNetDevUpgradeFileType.UPGRADE_IPC, 0);
            STLogUtils.e(TAG, "getUpgradeProcess ret1 = " + ret1.getError() + "   ret = " + ret1.getResponseStr());
        } else if (comm.equals(mCommondArr[26])) {
            //P2升级
            //P2中门升级
            ret = mNetDevice.upgradeP2File(STNetDevUpgradeFileType.UPGRADE_P2, STNetDevUpgradeFileType.STNetDeviceP2Type.DOOR_FRONT);
            STLogUtils.e(TAG, "upgradeP2File ret = " + ret.getError() + "   ret = " + ret.getResponseStr());
            //查询P2升级进度
            for (int i = 0; i < 40; i++) {
                STResponseData ret1 = mNetDevice.getP2UpgradeProcess(STNetDevUpgradeFileType.UPGRADE_P2, STNetDevUpgradeFileType.STNetDeviceP2Type.DOOR_FRONT);
                STLogUtils.e(TAG, "getP2UpgradeProcess ret1 = " + ret1.getError() + "   ret = " + ret1.getResponseStr());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else if (comm.equals(mCommondArr[27])) {
            //升级外设RWatch
            ret = mNetDevice.upgradeFile(STNetDevUpgradeFileType.UPGRADE_RWATCH);
            STLogUtils.e(TAG, "upgradeP2File ret = " + ret.getError() + "   ret = " + ret.getResponseStr());
            //RWatch升级进度
            STResponseData ret1 = mNetDevice.getUpgradeProcess(STNetDevUpgradeFileType.UPGRADE_RWATCH);
            STLogUtils.e(TAG, "getP2UpgradeProcess ret1 = " + ret1.getError() + "   ret = " + ret1.getResponseStr());
        } else if (comm.equals(mCommondArr[28])) {
            //升级外设GDS
            ret = mNetDevice.remoteUpgradeDevice("/storage/emulated/0/sss/GDS-M01-STM32-MCU-T631281.CRC", STRmoteUpgradeType.GDS, 0, 0);
            STLogUtils.e(TAG, "remoteUpgradeDevice ret1 = " + ret.getError() + "   ret = " + ret.getResponseStr());
            for (int i = 0; i < 20; i++) {
                STResponseData ret1 = mNetDevice.getRemoteUpgradeProcess(0);
                STLogUtils.e(TAG, "getRemoteUpgradeProcess ret1 = " + ret1.getError() + "   ret = " + ret1.getResponseStr());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else if (comm.equals(mCommondArr[29])) {
            //格式化硬盘
            ret = mNetDevice.formatDisk(1, STNetDevFormatType.FAT32);
            STLogUtils.e(TAG, "formatDisk ret = " + ret.getError() + "  return data = " + ret.getResponseStr());
        } else if (comm.equals(mCommondArr[30])) {
            //获取IPC版本号
            ret = mNetDevice.getIPCVersionsIndex(-1);
            STLogUtils.e(TAG, "getIPCVersionsIndex ret = " + ret.getError() + "   ret = " + ret.getResponseStr());
        } else if (comm.equals(mCommondArr[31])) {
            //获取更新文件版本信息
            ret = mNetDevice.getUpgradefileInfo(STNetDevUpgradeFileType.UPGRADE_CP4);
            STLogUtils.e(TAG, "getUpgradefileInfo ret = " + ret.getError() + "   ret = " + ret.getResponseStr());
        } else if (comm.equals(mCommondArr[32])) {
            //视频文件锁定和解锁
            String stTime[] = {"20180412000000", "20180412000000"};
            String edTime[] = {"20180412000819", "20180412000819"};
            ret = mNetDevice.setRecordLock(false, 3, STEnumType.STStreamType.MAIN, stTime, edTime);
            STLogUtils.e(TAG, "setRecordLock ret = " + ret.getError() + "   ret = " + ret.getResponseStr());
        } else if (comm.equals(mCommondArr[33])) {
            //下发维护文件完成到设备
            mNetDevice.sendFileToDevice("/storage/emulated/0/MaintainLogFile/1539755746.ML", STNetDevSendFileType.SendFileType.MAINTAIN, STNetDevSendFileType.SendFileLocationType.SDCARDORUDISK, 12);
        } else if (comm.equals(mCommondArr[34])) {
            //获取服务器连接信息
            ret = mNetDevice.getServerStateAndInfo();
            STLogUtils.e(TAG, "ret = " + ret.getError() + "   ret = " + ret.getResponseStr());
        } else if (comm.equals(mCommondArr[35])) {
            //导出参数文件
            ret = mNetDevice.remoteExportParamFile(STRemoteParamFileType.GDS, "/storage/emulated/0/config.gdp");
            STLogUtils.e(TAG, "remoteExportParamFile ret = " + ret.getError() + "   msg=" + ret.getResponseStr());
        } else if (comm.equals(mCommondArr[36])) {
            //导入参数文件
            ret = mNetDevice.remoteImportParamFile(STRemoteParamFileType.GDS, "/storage/emulated/0/config.gdp");
            STLogUtils.e(TAG, "remoteImportParamFile ret = " + ret.getError() + "   msg=" + ret.getResponseStr());
        } else if (comm.equals(mCommondArr[37])) {
            //六轴数据校准
            errorCode = mNetDevice.adjustSixAxis();
            STLogUtils.e(TAG, "adjustSixAxis ret = " + errorCode);
        } else if (comm.equals(mCommondArr[38])) {
            //外设六轴传感器校准
            errorCode = mNetDevice.adjustSixAxisWithExtern(STExternDevType.GDS);
            STLogUtils.e(TAG, "adjustSixAxisWithExtern ret = " + errorCode);
        } else if (comm.equals(mCommondArr[39])) {
            //设备语音对讲测试
            ret = mNetDevice.setUploadAUTest(true);
            STLogUtils.e(TAG, "setUploadAUTest ret = " + ret.getError() + "   ret = " + ret.getResponseStr());
        } else if (comm.equals(mCommondArr[40])) {
            //获取设备功能状态
            ret = mNetDevice.getCHNEnableAndLiveStatusInfo(15);
            STLogUtils.e(TAG, "getCHNEnableAndLiveStatusInfo ret = " + ret.getResponseStr());
        } else if (comm.equals(mCommondArr[41])) {
            //AHD摄像头操作控制
            mNetDevice.controlAHDCameraChannel(0, STAHDOprType.MENU_OK, 20);
        } else if (comm.equals(mCommondArr[42])) {
            //设置通道参数
            errorCode = mNetDevice.setCameraParamInfo("{\"MDVR\":{\"PCA\":[{\"F\":0,\"ISM\":0,\"RT\":1,\"W\":0},{\"F\":0,\"ISM\":0,\"RT\":0,\"W\":0},{\"F\":0,\"ISM\":0,\"RT\":0,\"W\":0},{\"F\":0,\"ISM\":0,\"RT\":0,\"W\":0},{\"F\":0,\"ISM\":0,\"RT\":0,\"W\":0},{\"F\":0,\"ISM\":0,\"RT\":0,\"W\":0},{\"F\":0,\"ISM\":0,\"RT\":0,\"W\":0},{\"F\":0,\"ISM\":0,\"RT\":0,\"W\":0},{\"F\":0,\"ISM\":0,\"RT\":0,\"W\":2},{\"F\":0,\"ISM\":0,\"RT\":0,\"W\":2},{\"F\":0,\"ISM\":0,\"RT\":0,\"W\":2},{\"F\":0,\"ISM\":0,\"RT\":0,\"W\":2},{\"F\":0,\"ISM\":0,\"RT\":0,\"W\":2},{\"F\":0,\"ISM\":0,\"RT\":0,\"W\":2},{\"F\":0,\"ISM\":0,\"RT\":0,\"W\":2},{\"F\":0,\"ISM\":0,\"RT\":0,\"W\":2}]}}");
            STLogUtils.e(TAG, "setCameraParamInfo ret = " + errorCode);
        } else if(comm.equals(mCommondArr[43])) {
            //车辆锁定
            errorCode = mNetDevice.setCarLockState(true);
            STLogUtils.e(TAG, "setCarLockState ret = " + errorCode);
        } else if(comm.equals(mCommondArr[44])){
            //获取模块信息
            ret = mNetDevice.GetDevGenralStatusInfo(STQueryStatusType.ALL);
            STLogUtils.e(TAG, "setCarLockState ret = " + ret.getResponseStr());
        } else if(comm.equals(mCommondArr[45])) {
            //设备管理
            errorCode = mNetDevice.SetControlDevCmd(STNetCtrlCmdType.REBOOT);
            STLogUtils.e(TAG, "setCarLockState ret = " + errorCode);
        } else if(comm.equals(mCommondArr[46])) {
            //通道信号源设置
            ChnSigInfo chn = new ChnSigInfo();
            chn.chn = 0;
            chn.sig = 1;
            ChnSigInfo aArr[] = {chn};
            int ss = mNetDevice.setGeneralDevCtrl(aArr);
            STLogUtils.e(TAG, "setGeneralDevCtrl ret = " + ss);
        } else if(comm.equals(mCommondArr[47])) {
            //通道信号源获取
            mNetDevice.getGeneralDevCtrl();
            STLogUtils.e(TAG, "getDevParamInfo ret = " + ret.getResponseStr());
        } else if (comm.equals(mCommondArr[48])) {
            //文件下载-黑匣子GPS
            if (null == mDownloadFile) {
                mDownloadFile = new STNetDownloadFile(mNetDevice);
            }
            mDownloadFile.startDownloadBlackBoxFileStart("20180604000000", "20180604235959", "/mnt/sdcard/AA264file/gps.txt", STHardDiskVersionType.VERSION1_0);
        } else if (comm.equals(mCommondArr[49])) {
            //文件下载-黑匣子维护记录
            if (null == mDownloadFile) {
                mDownloadFile = new STNetDownloadFile(mNetDevice);
            }
            mDownloadFile.startDownloadOperationLogStart(2, "20181010000000", "20181010235959", "/storage/emulated/0/Operations/Operate_Record.txt");
        } else if (comm.equals(mCommondArr[50])) {
            //视频下载-下载视频到本地
            if (null == mDownloadRecord) {
                mDownloadRecord = new STNetDownloadRecord(mNetDevice);
            }
            String devFileName = "0-270-0";
            String saveFileName = "/mnt/usb_storage/T111111/2018-05-29/record/0000000000000000-20181113-062608-064458-01p010000000.264";
            String s = "20181113062608";
            String e = "20181113064458";
            errorCode = mDownloadRecord.startDownloadRecordData(devFileName, saveFileName, s, e);
            STLogUtils.e(TAG, "startDownloadRecordData ret = " + ret);
        } else if (comm.equals(mCommondArr[51]) && null != mDownloadRecord) {
            //视频下载-获取下载进度
            ret = mDownloadRecord.getDownloadRecordDataProcess();
            STLogUtils.e(TAG, "getDownloadRecordDataProcess ret = " + ret.getResponseStr());
        }
        String msgStr = getString(R.string.errorcode) + errorCode;
        if(null != ret) {
            errorCode = ret.getError();
            msgStr = getString(R.string.errorcode) + errorCode + "\n";
            msgStr += getString(R.string.response) + ret.getResponseStr();
        }
        mMsgShowTV.setText(msgStr);
        return 0;
    }
}
