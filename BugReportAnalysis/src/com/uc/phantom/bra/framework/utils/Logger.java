package com.uc.phantom.bra.framework.utils;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Log Utility.
 * @Author Tam.
 * Modifications :
 * 2017/4/18 : Created.
 */
public class Logger {

    public final static String LOG_LEVEL_VERBOSE = "V";
    public final static String LOG_LEVEL_DEBUG   = "D";
    public final static String LOG_LEVEL_INFO    = "I";
    public final static String LOG_LEVEL_WARN    = "W";
    public final static String LOG_LEVEL_ERROR   = "E";
    public final static String LOG_LEVEL_FATAL   = "F";

    private static Logger mLogger;
    private LogInterface mLogInterface;

    SimpleDateFormat sdfDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");

    public static synchronized Logger getLogger(LogInterface logInterface) {
        if (mLogger == null) {
            mLogger = new Logger(logInterface);
        }

        return  mLogger;
    }

    public Logger(LogInterface logInterface) {
        mLogInterface = logInterface;
    }

    public String formatLogString(String TAG, String level, String msg) {
        return sdfDateTimeFormat.format(System.currentTimeMillis()) + " " +  level + " " + TAG + " " + msg;
    }

    public String formatLogString(String TAG, String msg, Throwable throwable) {
        StringBuilder sbLog = new StringBuilder();
        sbLog.append(sdfDateTimeFormat.format(System.currentTimeMillis()) + " " + LOG_LEVEL_ERROR + " " + TAG + " " + msg);

        StackTraceElement[] stacks = throwable.getStackTrace();
        sbLog.append("Message:" + throwable.getMessage());
        for (StackTraceElement ste : stacks) {
            sbLog.append(ste.toString());
        }

        return sbLog.toString();
    }

    public synchronized Logger log(String TAG, String level, String msg) {
        String logMsg = sdfDateTimeFormat.format(System.currentTimeMillis()) + " " + level + " " + TAG + " " + msg;
        if (mLogInterface != null) {
            mLogInterface.log(TAG, level, msg);
        } else {
            System.out.println(logMsg);
        }

        return this;
    }

    public synchronized Logger log(String TAG, String msg, Throwable throwable) {
        String logMsg = sdfDateTimeFormat.format(System.currentTimeMillis()) + " " + LOG_LEVEL_ERROR + " " + TAG + " " + msg;
        if (mLogInterface != null) {
            mLogInterface.log(TAG, msg, throwable);
        } else {
            System.out.println(logMsg);
            throwable.printStackTrace();
        }

        return this;
    }

    public synchronized void setLogInterface(LogInterface logInterface) {
        mLogInterface = logInterface;
    }

    public interface LogInterface {
        Logger log(String TAG, String level, String msg);
        Logger log(String TAG, String msg, Throwable throwable);
    }
}
