package com.uc.phantom.bra.framework.scan_parse.parser.handler;

import com.uc.phantom.bra.framework.common.Consts;
import com.uc.phantom.bra.framework.scan_parse.parser.BugReportParseEngine;
import com.uc.phantom.bra.framework.scan_parse.parser.StatisticInfo;
import com.uc.phantom.bra.framework.utils.FileWrapper;
import com.uc.phantom.bra.framework.utils.Logger;
import com.uc.phantom.bra.ui.swing.utils.StringUtils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

/**
 * Process Time Chapter Handler of BugReport.
 * @Author Tam.
 * Modifications :
 * 2017/4/17 : Created.
 */
public class ProcessTimeHandler extends ModuleHandler {

    private final static String TAG = "PROCESS TIMES";

    public ProcessTimeHandler(FileWrapper fileWrapper, String filter, int parseMode) {
        super(TAG, StatisticInfo.ChapterMapping.CHAPTER_PROCESS_TIME, fileWrapper, filter, parseMode);
    }

    @Override
    public void onModuleStart(String curLine, FileWrapper fileWrapper, BugReportParseEngine.ParseResult parseResult) throws IOException {
        super.onModuleStart(curLine, fileWrapper, parseResult);

        mChapterFilterLineAtInfo.setFeature(StatisticInfo.ChapterMapping.ChapterFilterLineAtInfo.FLAG_FEATURE_SPARSE);

        List<Long> extraIncludeLinesList = mChapterFilterLineAtInfo.getExtraIncludeLinesList();

        String line;
        do {
            line = BugReportParseEngine.readLine(fileWrapper, mFilter, mParseMode, parseResult);

            if (line != null && StringUtils.kmp_indexOf(line, mFilter) > -1) {
                // Record filtered line.
                extraIncludeLinesList.add(mCacheLinesStatistic.getCurrentLine());
            }
        } while (line != null && !line.equals("") && StringUtils.kmp_indexOf(line, Consts.BUGREPORT_CHAPTER_PREFIX) < 0);

        onModuleEnd(line, fileWrapper, parseResult);
    }

    @Override
    public void onModuleEnd(String curLine, FileWrapper fileWrapper, BugReportParseEngine.ParseResult parseResult) throws IOException {
        super.onModuleEnd(curLine, fileWrapper, parseResult);

        int len = curLine.trim().length();
        if (len > 0) {
            //StatisticInfo.CacheLinesStatistic cacheLinesStatistic = parseResult.getCacheLinesStatistic();
            //cacheLinesStatistic.rollbackNewLine();
            //raf.seek(cacheLinesStatistic.getCachedAllLinesSize());

            // Set chapter end line.
            mChapterFilterLineAtInfo.setChapterEndLine(mCacheLinesStatistic.getCurrentLine() - 1);
            mChapterFilterLineAtInfo.setChapterFilterEndLine(mCacheLinesStatistic.getCurrentLine() - 1);

            Logger.getLogger(null).log(mModuleName, Logger.LOG_LEVEL_INFO, "onModuleEnd, Adjust LineNo=" + mChapterFilterLineAtInfo.getChapterEndLine());

            parseResult.setSkipNextRead(true);
        }
    }
}
