package com.bitlove.fetlife.util;

import com.bitlove.fetlife.BuildConfig;
import com.bitlove.fetlife.FetLifeApplication;
import com.crashlytics.android.Crashlytics;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class LogUtil {

    private static final int MAX_LOG_LENGTH = 10000;

    public static synchronized void writeLog(String message) {

        if (!BuildConfig.DEBUG) {
            return;
        }

        String currentLog = readLogs();

        try {
            if (!currentLog.isEmpty()) {
                currentLog = currentLog.substring(0,Math.min(currentLog.length(), MAX_LOG_LENGTH));
            }
            File file = new File(FetLifeApplication.getInstance().getExternalFilesDir(null),"extra.log");
            if (!file.exists()) file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file,false);
            String log = DateUtil.toServerString(System.currentTimeMillis()) + " - " + message + "\n";
            fos.write((log + "\n" + currentLog).getBytes());
            fos.close();
        } catch (Throwable t) {
            Crashlytics.logException(new Exception("Extra log exception"));
        }
    }

    public static String readLogs() {

        if (!BuildConfig.DEBUG) {
            return "No Logs Available in Production Mode";
        }

        FileInputStream fis = null;
        try {
            String result = "";
            File file = new File(FetLifeApplication.getInstance().getExternalFilesDir(null),"extra.log");
            if (!file.exists()) return "No logs available";
            fis = new FileInputStream(file);
            while (fis.available() > 0) {
                result = result + String.valueOf((char) fis.read());
            }
            fis.close();
            return result;
        } catch (Throwable t) {
            return "Log read failed with exception";
        }


    }
}