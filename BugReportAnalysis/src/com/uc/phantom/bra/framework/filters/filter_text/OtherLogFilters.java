package com.uc.phantom.bra.framework.filters.filter_text;

import com.uc.phantom.bra.framework.common.ErrorCode;
import com.uc.phantom.bra.framework.scan_parse.parser.BugReportParseEngine;
import com.uc.phantom.bra.framework.scan_parse.parser.BugReportParseEngine.ParseResult;
import com.uc.phantom.bra.framework.scan_parse.parser.LogScanParseEngine;
import com.uc.phantom.bra.framework.scan_parse.parser.exception.IllegalParserStateException;
import com.uc.phantom.bra.framework.utils.Logger;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Filters of Other Log Files.
 * @Author Tam.
 * Modifications :
 * 2017/4/26 : Created.
 */
public class OtherLogFilters {

    private final static String TAG = "OtherLogFilters";

    /**
     * Entrance of Filter Module.
     * @param logDir
     * @param filter
     * @param fr
     * @return
     */
    public static int filterText4AppLogCat(File logDir, String filter, int parseMode, ParseResult fr) {
        return filterLogFile(logDir, LogScanParseEngine.LOG_TYPE_APPLOCAT, filter, parseMode, fr);
    }

    /**
     * Entrance of Filter Module.
     * @param logDir
     * @param filter
     * @param fr
     * @return
     */
    public static int filterText4KernelLog(File logDir, String filter, int parseMode, ParseResult fr) {
        return filterLogFile(logDir, LogScanParseEngine.LOG_TYPE_KERNEL_LOG, filter, parseMode, fr);
    }

    /**
     * Entrance of Filter Module.
     * @param logDir
     * @param filter
     * @param fr
     * @return
     */
    public static int filterText4EventLog(File logDir, String filter, int parseMode, ParseResult fr) {
        return filterLogFile(logDir, LogScanParseEngine.LOG_TYPE_EVENT_LOG, filter, parseMode, fr);
    }

    /**
     * Entrance of Filter Module.
     * @param logDir
     * @param filter
     * @param fr
     * @return
     */
    public static int filterText4Traces(File logDir, String filter, int parseMode, ParseResult fr) {
        return filterLogFile(logDir, LogScanParseEngine.LOG_TYPE_TRACES, filter, parseMode, fr);
    }

    private static int filterLogFile(File logFileDir, int logType, String filter, int parseMode, ParseResult parseResult) {
        if (!checkParams(logFileDir, filter, parseResult)) {
            Logger.getLogger(null).log(TAG, Logger.LOG_LEVEL_ERROR, String.format("filterLogFile(dir : %s)'s params incorrect.", logFileDir.getAbsolutePath()));
            return ErrorCode.PARSER_RESULT_PARAMS_ILLEGAL;
        }

        int parsedResult = ErrorCode.PARSER_RESULT_FAILED;
        try {
            switch (logType) {
                case LogScanParseEngine.LOG_TYPE_APPLOCAT:
                      parsedResult = LogScanParseEngine.parseAppLocat(getFiles(logFileDir, logType), filter, parseMode, parseResult);
                      parseResult.setValid(true);

                      Logger.getLogger(null).log(TAG, Logger.LOG_LEVEL_INFO, "LogScanParseEngine.parseAppLocat, errCode=" + parsedResult);
                      break;
                case LogScanParseEngine.LOG_TYPE_EVENT_LOG:
                      parsedResult = LogScanParseEngine.parseLogEvent(getFiles(logFileDir, logType), filter, parseMode, parseResult);
                      parseResult.setValid(true);

                      Logger.getLogger(null).log(TAG, Logger.LOG_LEVEL_INFO, "LogScanParseEngine.parseLogEvent, errCode=" + parsedResult);
                      break;
                case LogScanParseEngine.LOG_TYPE_KERNEL_LOG:
                      parsedResult = LogScanParseEngine.parseKernelLog(getFiles(logFileDir, logType), filter, parseMode, parseResult);
                      parseResult.setValid(true);

                      Logger.getLogger(null).log(TAG, Logger.LOG_LEVEL_INFO, "LogScanParseEngine.parseKernelLog, errCode=" + parsedResult);
                      break;
                case LogScanParseEngine.LOG_TYPE_TRACES:
                      parsedResult = LogScanParseEngine.parseTracesLog(getFiles(logFileDir, logType), filter, parseMode, parseResult);
                      parseResult.setValid(true);

                      Logger.getLogger(null).log(TAG, Logger.LOG_LEVEL_INFO, "LogScanParseEngine.parseTracesLog, errCode=" + parsedResult);
                      break;
                default:
                      parseResult.setValid(false);
                      Logger.getLogger(null).log(TAG, Logger.LOG_LEVEL_INFO, "Not LogScanParseEngine support parse type, errCode=" + ErrorCode.PARSER_NOT_SUPPORTED_TYPE);
                      return ErrorCode.PARSER_NOT_SUPPORTED_TYPE;
            }
        } catch (IOException ioex) {
            parseResult.setValid(false);
            Logger.getLogger(null).log(TAG, ioex.getMessage(), ioex);
        } finally {
            Logger.getLogger(null).log(TAG, Logger.LOG_LEVEL_INFO, String.format("Parse %s finished.", logFileDir.getAbsoluteFile()));
        }

        return parsedResult;
    }

    private static File[] getFiles(File dir, final int logType) {
        if (dir == null || !dir.exists())
            return null;

        if (dir.isFile())
            return new File[] { dir };

        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (logType == LogScanParseEngine.LOG_TYPE_APPLOCAT && name.startsWith(LogScanParseEngine.LOG_FILENAME_APPLOGCAT)) {
                    return true;
                } else if (logType == LogScanParseEngine.LOG_TYPE_EVENT_LOG && name.startsWith(LogScanParseEngine.LOG_FILENAME_EVENT_LOG)) {
                    return true;
                } else if (logType == LogScanParseEngine.LOG_TYPE_KERNEL_LOG && name.startsWith(LogScanParseEngine.LOG_FILENAME_KERNEL_LOG)) {
                    return true;
                } else if (logType == LogScanParseEngine.LOG_TYPE_TRACES && name.startsWith(LogScanParseEngine.LOG_FILENAME_TRACES)) {
                    return true;
                }

                return false;
            }
        });

        if (files.length <= 1)
            return files;

        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                String fname1 = o1.getName();
                String fname2 = o2.getName();

                return -fname1.compareToIgnoreCase(fname2);
            }
        });

        return files;
    }

    private static boolean checkParams(File logFile, String filter, ParseResult fr) {
        if (logFile == null || !logFile.exists()) {
            Logger.getLogger(null).log(TAG, Logger.LOG_LEVEL_ERROR, String.format("logFile(%s) not exists.", logFile == null ? "null" : logFile.getAbsolutePath()));
            return false;
        }

        if (fr == null)
            return false;

        return true;
    }

}
