package com.uc.phantom.bra.framework.scan_parse.parser;

import com.uc.phantom.bra.framework.common.Consts;
import com.uc.phantom.bra.framework.common.ErrorCode;
import com.uc.phantom.bra.framework.scan_parse.parser.exception.IllegalParserStateException;
import com.uc.phantom.bra.framework.scan_parse.parser.handler.*;
import com.uc.phantom.bra.framework.utils.FileUtils;
import com.uc.phantom.bra.framework.utils.FileWrapper;
import com.uc.phantom.bra.framework.utils.Logger;
import com.uc.phantom.bra.ui.swing.utils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * BugReport Parse Engine.
 * @Author Tam.
 * Modifications :
 * 2017/4/17 : Created.
 */
public class BugReportParseEngine {

    public final static String TAG = "BugReportParseEngine";

    public final static int FLAG_FILTER_NONE               = 0;
    public final static int FLAG_ONLY_FILTER_TEXT         = 1;
    public final static int FLAG_ONLY_FILTER_PROBLEM      = 2;
    public final static int FLAG_FILTER_TEXT_AND_PROBLEM = 3;

    public final static int LOAD_MODE_FULL = 0;
    public final static int LOAD_MODE_PART = 1;

    public final static String PARSE_ENGINE_STATE_START   = "Start";
    public final static String PARSE_ENGINE_STATE_ONLINE  = "Online";
    public final static String PARSE_ENGINE_STATE_OFFLINE = "Offline";
    public final static String PARSE_ENGINE_STATE_ONLINE_NORMAL  = "Online Normal";
    public final static String PARSE_ENGINE_STATE_OFFLINE_NORMAL = "Offline Normal";
    public final static String PARSE_ENGINE_STATE_PROBLEM_START  = "Problem Start";
    public final static String PARSE_ENGINE_STATE_PROBLEM_END     = "Problem End";
    public final static String PARSE_ENGINE_STATE_POTENTIAL_PROBLEM_DETECTED  = "Potential Problem Detected";
    public final static String PARSE_ENGINE_STATE_PROBLEM_DETECTED             = "Problem Detected";
    public final static String PARSE_ENGINE_STATE_REPORT_PROBLEM  = "Report Problem";
    public final static String PARSE_ENGINE_STATE_MACHINE_EXPERT  = "Machine Expert";
    public final static String PARSE_ENGINE_STATE_EXCEPTION  = "Exception";
    public final static String PARSE_ENGINE_STATE_BUILDPRINT = "Buildprint";
    public final static String PARSE_ENGINE_STATE_FATAL  = "Fatal";
    public final static String PARSE_ENGINE_STATE_ANR    = "ANR";
    public final static String PARSE_ENGINE_STATE_STOP   = "Stop";

    private static int sLoadMode;
    private static String sState;

    private static Map<String, ModuleHandler> sModuleHandlers;

    private static String sFileEncode;

    /**
     * Must call before parseBugReport.
     */
    public static void startBugReportEngine() {
        sState = PARSE_ENGINE_STATE_START;

        if (sModuleHandlers == null) {
            sModuleHandlers = new HashMap<String, ModuleHandler>();
        } else {
            sModuleHandlers.clear();
        }

        // DUMPSYS MEMINFO Block Start.
        sModuleHandlers.put(Consts.BUGREPORT_CHAPTER_DUMPSYS_MEMINFO_START, new DumpSysMemInfoHandler(null, null, -1));
        // DUMPSYS CPUINFO Block Start.
        sModuleHandlers.put(Consts.BUGREPORT_CHAPTER_DUMPSYS_CPUINFO_START, new DumpSysCpuInfoHandler(null, null, -1));
        // UPTIME Block Start.
        sModuleHandlers.put(Consts.BUGREPORT_CHAPTER_UPTIME_START, new UptimeHandler(null, null, -1));
        // Memory Block Start.
        sModuleHandlers.put(Consts.BUGREPORT_CHAPTER_MEMORY_INFO_START, new MemoryInfoHandler(null, null, -1));
        // Cpu Info Block Start.
        sModuleHandlers.put(Consts.BUGREPORT_CHAPTER_CPU_INFO_START, new CpuInfoHandler(null, null, -1));
        // Processes and Threads Block Start.
        sModuleHandlers.put(Consts.BUGREPORT_CHAPTER_PROCESSES_AND_THREADS_START, new ProcessThreadHandler(null, null, -1));
        // Smaps of All Processes Block Start.
        sModuleHandlers.put(Consts.BUGREPORT_CHAPTER_SMAPS_OF_ALL_PROCESSES_START, new SmapsHandler(null, null, -1));
        // Blocked Process Wait Channels Block Start.
        sModuleHandlers.put(Consts.BUGREPORT_CHAPTER_BLOCKED_PROCESS_WAIT_CHANNELS_START, new BlockedProcessesWaitChannelHandler(null, null, -1));
        // Process Times Block Start.
        sModuleHandlers.put(Consts.BUGREPORT_CHAPTER_PROCESS_TIMES_START, new ProcessTimeHandler(null, null, -1));
        // VM Traces Block Start.
        sModuleHandlers.put(Consts.BUGREPORT_CHAPTER_VM_TRACES_JUST_NOW_START, new VMTracesHandler(null, null, -1));
        // Checkin UsageStats Block Start.
        sModuleHandlers.put(Consts.BUGREPORT_CHAPTER_CHECKIN_USAGESTATS_START, new UsageStatHandler(null, null, -1));
        // Checkin Package Block Start.
        sModuleHandlers.put(Consts.BUGREPORT_CHAPTER_CHECKIN_PACKAGE_START, new CheckinPackageHandler(null, null, -1));
        // App Activities Block Start.
        sModuleHandlers.put(Consts.BUGREPORT_CHAPTER_APP_ACTIVITIES_START, new AppActivitiesHandler(null, null, -1));
        // App Providers Block Start.
        sModuleHandlers.put(Consts.BUGREPORT_CHAPTER_APP_PROVIDERS_START, new AppProvidersHandler(null, null, -1));
        // App Services Block Start.
        sModuleHandlers.put(Consts.BUGREPORT_CHAPTER_APP_SERVICES_START, new AppServicesHandler(null, null, -1));
        // App Logcat Block Start.
        sModuleHandlers.put(Consts.BUGREPORT_CHAPTER_LOGCAT_START, new LogcatHandler(null, null, -1));
    }

    /**
     * Call before destruct.
     */
    public static void stopBugReportEngine() {
        sState = PARSE_ENGINE_STATE_STOP;
    }

    /**
     * Entrance of BugReport Scan and Split Module.
     * @param bugReportFile BugReport file's path.
     * @param filter which used to filter the BugReport Content.
     * @param pr Container to hold the scan and split result.
     * @param parseMode
     * @return Parse Result Status.
     * @throws IllegalParserStateException
     */
    public static int parseBugReport(File bugReportFile, String filter, ParseResult pr, int parseMode) throws IllegalParserStateException {
        if (!sState.equals(PARSE_ENGINE_STATE_START)) {
            throw new IllegalParserStateException("BugReportParserEngine is not in correct state(curState=" +sState + ", expState=" + PARSE_ENGINE_STATE_START + ").");
        }

        Logger.getLogger(null).log(TAG, Logger.LOG_LEVEL_INFO, "parseBugReport start.");

        try {
            sState = PARSE_ENGINE_STATE_OFFLINE;
            if (bugReportFile == null || !bugReportFile.exists())
                return ErrorCode.PARSER_RESULT_BUGREPORT_NOT_EXISTS;

            if (parseMode == FLAG_FILTER_NONE)
                return ErrorCode.PARSER_RESULT_BUGREPORT_NONE_CONTENT_NEED_PARSED;

            sState = PARSE_ENGINE_STATE_OFFLINE_NORMAL;

            return parseBugReportInner(bugReportFile, filter, parseMode, pr);
        } catch (IOException ioex) {
            ioex.printStackTrace();
            Logger.getLogger(null).log(TAG, ioex.getMessage(), ioex);

            return ErrorCode.PARSER_RESULT_FAILED;
        } finally {
            sState = PARSE_ENGINE_STATE_STOP;
            Logger.getLogger(null).log(TAG, Logger.LOG_LEVEL_INFO, "parseBugReport finished.");
        }
    }

    /**
     * Another Entrance of BugReport Scan and Split Module.
     * @param bugReportPath
     * @param filter
     * @param pr
     * @param flags
     * @return
     * @throws IllegalParserStateException
     */
    public static int parseBugReport(String bugReportPath, String filter, ParseResult pr, int flags) throws IllegalParserStateException {
        return parseBugReport(new File(bugReportPath), filter, pr, flags);
    }


    private static int parseBugReportInner(File brFile, String filter, int parseMode, ParseResult pr) throws IOException {
        try {
            sFileEncode = FileUtils.getFileEncode(brFile);
        } catch (Exception ex) {
            Logger.getLogger(null).log(TAG,  "Get " + brFile.getAbsolutePath() + " encode failed.", ex);
            Logger.getLogger(null).log(TAG, Logger.LOG_LEVEL_INFO, "Using " + sFileEncode + " instead.");

            sFileEncode = Consts.DEFAULT_FILE_ENCODE;
        }

        RandomAccessFile raf = new RandomAccessFile(brFile,"r");

        long fileLen = raf.length();
        long maxHeapMemory = Runtime.getRuntime().maxMemory();

        double occupancyRate = fileLen * 1.0 / maxHeapMemory;
        if (occupancyRate <= 0.75) {// BugReport file size not exceed 3/4 of current max heap size.
            sLoadMode = LOAD_MODE_FULL;
        } else {
            sLoadMode = LOAD_MODE_PART;
        }

        raf.close();
        Logger.getLogger(null).log(TAG, Logger.LOG_LEVEL_INFO, String.format("BugReport size=%d, maxHeapMemory=%d", fileLen, maxHeapMemory))
                          .log(TAG, Logger.LOG_LEVEL_INFO, String.format("occupancyRate=%.2f, loadMode=%s", occupancyRate, sLoadMode == LOAD_MODE_FULL ? "full load" : "part load"));


        if (sLoadMode == LOAD_MODE_FULL) {
            Logger.getLogger(null).log(TAG, Logger.LOG_LEVEL_INFO, "aa");
            List<String> fileContentList = org.apache.commons.io.FileUtils.readLines(brFile, "utf-8");
            Logger.getLogger(null).log(TAG, Logger.LOG_LEVEL_INFO, "bb");
            FileWrapper fileWrapper = new FileWrapper(brFile, fileContentList);

            return parseBugReportFullLoad(fileWrapper, filter, parseMode, pr);
        } else {
            return parseBugReportPartLoad(raf, filter, parseMode, pr);
        }
    }

    private static int parseBugReportFullLoad(FileWrapper fileWrapper, String filter, int parseMode, ParseResult pr) throws IOException {
        String line = null;
        while ((line = readLine(fileWrapper, filter, parseMode, pr)) != null) { // LINE Driver Parse.
            dealLine(fileWrapper, line, filter, parseMode, pr);
        }

        fileWrapper.release();

        return ErrorCode.PARSER_RESULT_OK;
    }

    private static int dealLine(FileWrapper fileWrapper, String line, String filter, int parseMode, ParseResult pr) throws IOException {
        ModuleHandler moduleHandler = null;
        Set<String> keySet = sModuleHandlers.keySet();
        for (String key : keySet) {
            if (StringUtils.kmp_indexOf(line, key) > -1) {
                moduleHandler = sModuleHandlers.get(key);
                moduleHandler.setFilter(filter);

                sModuleHandlers.remove(key);
                break;
            }
        }

        if (moduleHandler == null) {
            return ErrorCode.PARSER_RESULT_NO_PROPER_HANDLER;
        }

        moduleHandler.onModuleStart(line, fileWrapper, pr);

        return ErrorCode.PARSER_RESULT_OK;
    }

    public static String readLine(FileWrapper fileWrapper, String filter, int parseMode, ParseResult pr) throws IOException {
        String line;
        if (pr.isSkipNextRead()) {
            pr.setSkipNextRead(false);
            return pr.getLastLine();
        }

        line = fileWrapper.readLine();
        if (line == null)
            return null;

        pr.setLastLine(line);
        // Statistic parsing line.
        StatisticInfo.CacheLinesStatistic cacheLinesStatistic = pr.getCacheLinesStatistic();
        cacheLinesStatistic.addNewLine(line, sFileEncode);

        return line;
    }

    private static int parseBugReportPartLoad(RandomAccessFile raf, String filter, int parseMode, ParseResult pr) {
        return ErrorCode.PARSER_RESULT_NOT_SUPPORT_METHOD;
    }

    /**
     * Store Parse Result for a pass.
     */
    public static class ParseResult {
        private Map<Integer, StatisticInfo.ChapterMapping.ChapterFilterLineAtInfo> mChapterFilterLineAtInfo;
        private StatisticInfo.CacheLinesStatistic mCacheLinesStatistic;
        private StatisticInfo.PlainProblems mPlainProblems;
        private StatisticInfo.PlainProblems mFilterProblems;

        private boolean mSkipNextRead;

        private boolean mValid;

        private String mLastLine;

        public ParseResult() {
            init();
        }

        public Map<Integer, StatisticInfo.ChapterMapping.ChapterFilterLineAtInfo> getChapterFilterLineAtInfo() {
            return mChapterFilterLineAtInfo;
        }

        public void setChapterFilterLineAtInfo(Map<Integer, StatisticInfo.ChapterMapping.ChapterFilterLineAtInfo> chapterFilterLineAtInfo) {
            mChapterFilterLineAtInfo = chapterFilterLineAtInfo;
        }

        public StatisticInfo.CacheLinesStatistic getCacheLinesStatistic() {
            return mCacheLinesStatistic;
        }

        public void setsCacheLinesStatistic(StatisticInfo.CacheLinesStatistic cacheLinesStatistic) {
            mCacheLinesStatistic = cacheLinesStatistic;
        }

        public StatisticInfo.PlainProblems getPlainProblems() {
            return mPlainProblems;
        }

        public StatisticInfo.PlainProblems getFilterProblems() {
            return mFilterProblems;
        }

        public void setPlainProblems(StatisticInfo.PlainProblems plainProblems) {
            mPlainProblems = plainProblems;
        }

        public boolean isValid() {
            return mValid;
        }

        public void setValid(boolean valid) {
            mValid = valid;
        }

        public boolean isSkipNextRead() {
            return mSkipNextRead;
        }

        public void setSkipNextRead(boolean skipNextRead) {
            mSkipNextRead = skipNextRead;
        }

        public String getLastLine() {
            return mLastLine;
        }

        public void setLastLine(String lastLine) {
            mLastLine = lastLine;
        }

        public void release() {
            mValid = false;
            mCacheLinesStatistic = null;

            if (mChapterFilterLineAtInfo != null) {
                mChapterFilterLineAtInfo.clear();
                mChapterFilterLineAtInfo = null;
            }

            if (mPlainProblems != null) {
                mPlainProblems.release();
                mPlainProblems = null;
            }

            if (mFilterProblems != null) {
                mFilterProblems.release();
                mFilterProblems = null;
            }
        }

        public void init() {
            mCacheLinesStatistic = new StatisticInfo.CacheLinesStatistic();
            mChapterFilterLineAtInfo = new HashMap<Integer, StatisticInfo.ChapterMapping.ChapterFilterLineAtInfo>();
            mPlainProblems = new StatisticInfo.PlainProblems();
            mFilterProblems = new StatisticInfo.PlainProblems();
        }

        @Override
        protected void finalize() throws Throwable {
            super.finalize();
            release();
        }
    }

}
