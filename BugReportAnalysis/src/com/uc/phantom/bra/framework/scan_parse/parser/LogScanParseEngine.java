package com.uc.phantom.bra.framework.scan_parse.parser;

import com.uc.phantom.bra.framework.common.Consts;
import com.uc.phantom.bra.framework.common.ErrorCode;
import com.uc.phantom.bra.framework.scan_parse.parser.exception.IllegalParserStateException;
import com.uc.phantom.bra.framework.scan_parse.parser.handler.*;
import com.uc.phantom.bra.framework.utils.FileUtils;
import com.uc.phantom.bra.framework.utils.FileWrapper;
import com.uc.phantom.bra.framework.utils.Logger;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

import static com.uc.phantom.bra.framework.scan_parse.parser.BugReportParseEngine.LOAD_MODE_FULL;
import static com.uc.phantom.bra.framework.scan_parse.parser.BugReportParseEngine.LOAD_MODE_PART;

/**
 * Log Scan and Parse Engine.
 * @Author Tam.
 * Modifications :
 * 2017/4/26 : Created.
 */
public class LogScanParseEngine {

    public final static String TAG = "LogScanParseEngine";

    public final static int LOG_TYPE_APPLOCAT   = 1;
    public final static int LOG_TYPE_EVENT_LOG  = 2;
    public final static int LOG_TYPE_KERNEL_LOG = 3;
    public final static int LOG_TYPE_TRACES      = 4;

    public final static String LOG_FILENAME_APPLOGCAT  = "applogcat-log";
    public final static String LOG_FILENAME_EVENT_LOG  = "log_events";
    public final static String LOG_FILENAME_KERNEL_LOG = "kmsgcat-log";
    public final static String LOG_FILENAME_TRACES     = "traces.txt";


    private static int sLoadMode;
    private static String sFileEncode;

    private static Map<Integer, ModuleHandler> sModuleHandlers;

    /**
     * Entrance of Logcat Scan and Split Module.
     * @param logFiles AppLogcat 's path.
     * @param filter which used to filter the Applogcat Content.
     * @param pr Container to hold the scan and split result.
     * @param parseMode
     * @return Parse Result Status.
     * @throws IOException
     */
    public static int parseAppLocat(File[] logFiles, String filter, int parseMode, BugReportParseEngine.ParseResult pr) throws IOException {
        return parseLogInner(logFiles, filter, parseMode, StatisticInfo.ChapterMapping.CHAPTER_APPLOCAT, pr);
    }

    /**
     * Entrance of Logevent Scan and Split Module.
     * @param logFiles AppLogcat 's path.
     * @param filter which used to filter the Applogcat Content.
     * @param pr Container to hold the scan and split result.
     * @param parseMode
     * @return Parse Result Status.
     * @throws IOException
     */
    public static int parseLogEvent(File[] logFiles, String filter, int parseMode, BugReportParseEngine.ParseResult pr) throws IOException {
        return parseLogInner(logFiles, filter, parseMode, StatisticInfo.ChapterMapping.CHAPTER_EVENTS, pr);
    }


    /**
     * Entrance of Kernel log Scan and Split Module.
     * @param logFiles AppLogcat 's path.
     * @param filter which used to filter the Applogcat Content.
     * @param pr Container to hold the scan and split result.
     * @param parseMode
     * @return Parse Result Status.
     * @throws IOException
     */
    public static int parseKernelLog(File[] logFiles, String filter, int parseMode, BugReportParseEngine.ParseResult pr) throws IOException {
        return parseLogInner(logFiles, filter, parseMode, StatisticInfo.ChapterMapping.CHAPTER_KERNEL_LOGS, pr);
    }

    /**
     * Entrance of Traces log Scan and Split Module.
     * @param logFiles AppLogcat 's path.
     * @param filter which used to filter the Applogcat Content.
     * @param pr Container to hold the scan and split result.
     * @param parseMode
     * @return Parse Result Status.
     * @throws IOException
     */
    public static int parseTracesLog(File[] logFiles, String filter, int parseMode, BugReportParseEngine.ParseResult pr) throws IOException {
        return parseLogInner(logFiles, filter, parseMode, StatisticInfo.ChapterMapping.CHAPTER_TRACES, pr);
    }

    private static int parseLogInner(File[] logFiles, String filter, int parseMode, Integer logType, BugReportParseEngine.ParseResult pr) throws IOException {
        boolean patiallySuccess = false;
        int errCode;

        initModules();

        for (File logFile : logFiles) {
            try {
                Logger.getLogger(null).log(TAG, Logger.LOG_LEVEL_INFO, "parseLogInner: " + logFile.getAbsolutePath());
                sFileEncode = FileUtils.getFileEncode(logFile);
            } catch (Exception ex) {
                Logger.getLogger(null).log(TAG,  "Get " + logFile.getAbsolutePath() + " encode failed.", ex);
                Logger.getLogger(null).log(TAG, Logger.LOG_LEVEL_INFO, "Using " + sFileEncode + " instead.");

                sFileEncode = Consts.DEFAULT_FILE_ENCODE;
            }

            RandomAccessFile raf = new RandomAccessFile(logFile,"r");

            long fileLen = raf.length();
            long maxHeapMemory = Runtime.getRuntime().maxMemory();

            double occupancyRate = fileLen * 1.0 / maxHeapMemory;
            if (occupancyRate <= 0.75) {// BugReport file size not exceed 3/4 of current max heap size.
                sLoadMode = LOAD_MODE_FULL;
            } else {
                sLoadMode = LOAD_MODE_PART;
            }

            raf.close();

            Logger.getLogger(null).log(TAG, Logger.LOG_LEVEL_INFO, String.format(logFile.getAbsolutePath() + " size=%d, maxHeapMemory=%d", fileLen, maxHeapMemory))
                                   .log(TAG, Logger.LOG_LEVEL_INFO, String.format("occupancyRate=%.2f, loadMode=%s", occupancyRate, sLoadMode == LOAD_MODE_FULL ? "full load" : "part load"));

            List<String> fileContentList = org.apache.commons.io.FileUtils.readLines(logFile, "utf-8");
            FileWrapper fileWrapper = new FileWrapper(logFile, fileContentList);

            if (sLoadMode == LOAD_MODE_FULL) {
                errCode = parseLogFullLoad(fileWrapper, filter, parseMode, logType, pr);
            } else {

                errCode = parseLogPartLoad(fileWrapper, filter, parseMode, logType, pr);
            }

            if (errCode != ErrorCode.PARSER_RESULT_OK && !patiallySuccess) {
                patiallySuccess = true;
            }
        }

        if (patiallySuccess) {
            return ErrorCode.PARSER_RESULT_PARTIAL_OK;
        } else {
            return ErrorCode.PARSER_RESULT_OK;
        }
    }

    private static void initModules() {
        if (sModuleHandlers == null) {
            sModuleHandlers = new HashMap<Integer, ModuleHandler>();
        } else {
            sModuleHandlers.clear();
        }

        // AppLogcat Block.
        sModuleHandlers.put(StatisticInfo.ChapterMapping.CHAPTER_APPLOCAT, new AppLogcatHandler(null, null, -1));
        // Events Block.
        sModuleHandlers.put(StatisticInfo.ChapterMapping.CHAPTER_EVENTS, new LogEventHandler(null, null, -1));
        // Kernel Block.
        sModuleHandlers.put(StatisticInfo.ChapterMapping.CHAPTER_KERNEL_LOGS, new KernelLogHandler(null, null, -1));
        // Traces Block.
        sModuleHandlers.put(StatisticInfo.ChapterMapping.CHAPTER_TRACES, new TracesLogHandler(null, null, -1));
    }

    private static int parseLogFullLoad(FileWrapper fileWrapper, String filter, int parseMode, Integer logType, BugReportParseEngine.ParseResult pr) throws IOException {
        String line = null;
        while ((line = readLine(fileWrapper, filter, parseMode, pr)) != null) { // LINE Driver Parse.
            dealLine(fileWrapper, line, filter, parseMode, logType, pr);
        }

        fileWrapper.release();

        return ErrorCode.PARSER_RESULT_OK;
    }

    private static int parseLogPartLoad(FileWrapper fileWrapper, String filter, int parseMode, Integer logType, BugReportParseEngine.ParseResult pr) {
        return ErrorCode.PARSER_RESULT_NOT_SUPPORT_METHOD;
    }

    private static int dealLine(FileWrapper fileWrapper, String line, String filter, int parseMode, Integer logType, BugReportParseEngine.ParseResult pr) throws IOException {
        Set<Integer> setKey = sModuleHandlers.keySet();
        if (setKey.contains(logType)) {
            ModuleHandler moduleHandler = sModuleHandlers.get(logType);
            moduleHandler.onModuleStart(line, fileWrapper, pr);

            sModuleHandlers.remove(logType);

            return ErrorCode.PARSER_RESULT_OK;
        } else {
            return ErrorCode.PARSER_RESULT_IGNORE;
        }
    }

    public static String readLine(FileWrapper fileWrapper, String filter, int parseMode, BugReportParseEngine.ParseResult pr) throws IOException {
        String line;
        if (pr.isSkipNextRead()) {
            pr.setSkipNextRead(false);
            return pr.getLastLine();
        }

        line = fileWrapper.readLine();
        if (line == null) {
            Logger.getLogger(null).log(TAG, Logger.LOG_LEVEL_INFO, "readLine: Reading end of " + fileWrapper);
            return null;
        }

        pr.setLastLine(line);
        // Statistic parsing line.
        StatisticInfo.CacheLinesStatistic cacheLinesStatistic = pr.getCacheLinesStatistic();
        cacheLinesStatistic.addNewLine(line, sFileEncode);

        return line;

    }
}
