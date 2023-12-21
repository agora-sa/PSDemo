package com.streamax.mmiddlewaredemo.dataprocess;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FileUtils {
    private static final String TAG = "FileUtils";

    public static void writeToFile(final byte[] buffer, final String folder,
                                   final String fileName, final boolean append, final boolean autoLine) {
        if(buffer==null||buffer.length < 1){
            Log.e(TAG, "---> buffer error buffer = "+ buffer+ " , len = "+ (buffer==null? 0:buffer.length));
            return;
        }

        File fileDir = new File(folder);
        if (!fileDir.exists()) {
            if (!fileDir.mkdirs()) {
                Log.e(TAG, "---> mkdirs() failed");
                return;
            }
        }

        // Log.e(TAG, "---> fileName = "+ fileName);

        File file = new File(fileDir, fileName);
        RandomAccessFile raf = null;
        FileOutputStream out = null;
        try {
            if (append) {
                //如果为追加则在原来的基础上继续写文件
                raf = new RandomAccessFile(file, "rw");
                raf.seek(file.length());
                raf.write(buffer);
                if (autoLine) {
                    raf.write("\n".getBytes());
                }
            } else {
                //重写文件，覆盖掉原来的数据
                out = new FileOutputStream(file);
                out.write(buffer);
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (raf != null) {
                    raf.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void writeToPcm() {

    }

    private boolean checkUDisk(){
        File uDir = new File("/mnt/usb_storage");
        return uDir.exists();
    }
}
