package com.uc.phantom.bra.framework.filters.filter_text;

import com.uc.phantom.bra.framework.common.ErrorCode;
import com.uc.phantom.bra.framework.scan_parse.parser.BugReportParseEngine;
import com.uc.phantom.bra.framework.scan_parse.parser.BugReportParseEngine.ParseResult;
import com.uc.phantom.bra.framework.scan_parse.parser.exception.IllegalParserStateException;
import com.uc.phantom.bra.framework.utils.Logger;

import java.io.File;

/**
 * Filters of Text or Problem.
 * @Author Tam.
 * Modifications :
 * 2017/4/17 : Created.
 */
public class BugReportFilters {

    private final static String TAG = "BugReportFilters";

    /**
     * Entrance of Filter Module.
     * @param logFile
     * @param filter
     * @param fr
     * @return
     */
    public static int filterText4LogFile(File logFile, String filter, ParseResult fr) {
        return filterLogFile(logFile, filter, BugReportParseEngine.FLAG_ONLY_FILTER_TEXT, fr);
    }

    /**
     * Entrance of Filter Module.
     * @param logFile
     * @param filter
     * @param fr
     * @return
     */
    public static int filterProblem4LogFile(File logFile, String filter, ParseResult fr) {
        return filterLogFile(logFile, filter, BugReportParseEngine.FLAG_ONLY_FILTER_PROBLEM, fr);
    }

    /**
     * Entrance of Filter Module.
     * @param logFile
     * @param filter
     * @param fr
     * @return
     */
    public static int filterTextAndProblem4LogFile(File logFile, String filter, ParseResult fr) {
        return filterLogFile(logFile, filter, BugReportParseEngine.FLAG_FILTER_TEXT_AND_PROBLEM, fr);
    }


    private static int filterLogFile(File logFile, String filter, int filterType, ParseResult fr) {
        if (!checkParams(logFile, filter, filterType, fr)) {
            Logger.getLogger(null).log(TAG, Logger.LOG_LEVEL_ERROR, "filterText4LogFile's params incorrect.");
            return ErrorCode.PARSER_RESULT_PARAMS_ILLEGAL;
        }

        BugReportParseEngine.startBugReportEngine();

        int parsedResult = ErrorCode.PARSER_RESULT_FAILED;
        try {
            parsedResult = BugReportParseEngine.parseBugReport(logFile, filter, fr, filterType);
            fr.setValid(true);

            Logger.getLogger(null).log(TAG, Logger.LOG_LEVEL_INFO, "BugReportParseEngine.parseBugReport, errCode=" + parsedResult);
        } catch (IllegalParserStateException illpsex) {
            fr.setValid(false);
            Logger.getLogger(null).log(TAG, illpsex.getMessage(), illpsex);
        }

        BugReportParseEngine.stopBugReportEngine();

        return parsedResult;
    }

    private static boolean checkParams(File logFile, String filter, int filterType, ParseResult fr) {
        if (logFile == null || !logFile.exists())
            return false;

        if (filterType != BugReportParseEngine.FLAG_FILTER_NONE
                && filterType != BugReportParseEngine.FLAG_ONLY_FILTER_TEXT
                && filterType != BugReportParseEngine.FLAG_ONLY_FILTER_PROBLEM
                && filterType != BugReportParseEngine.FLAG_FILTER_TEXT_AND_PROBLEM ) {
            return false;
        }

        if (fr == null)
            return false;

        return true;
    }

}
