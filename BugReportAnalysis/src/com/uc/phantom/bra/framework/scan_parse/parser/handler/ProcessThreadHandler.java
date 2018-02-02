package com.uc.phantom.bra.framework.scan_parse.parser.handler;

import com.uc.phantom.bra.framework.common.Consts;
import com.uc.phantom.bra.framework.scan_parse.parser.BugReportParseEngine;
import com.uc.phantom.bra.framework.scan_parse.parser.StatisticInfo;
import com.uc.phantom.bra.framework.utils.FileWrapper;
import com.uc.phantom.bra.ui.swing.utils.StringUtils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

/**
 * Processes and Threads Chapter Handler of BugReport.
 * @Author Tam.
 * Modifications :
 * 2017/4/17 : Created.
 */
public class ProcessThreadHandler extends ModuleHandler {

    private final static String TAG = "ProcessThreadHandler";

    public ProcessThreadHandler(FileWrapper fileWrapper, String filter, int parseMode) {
        super(TAG, StatisticInfo.ChapterMapping.CHAPTER_PROCESSES_AND_THREADS, fileWrapper, filter, parseMode);
    }

    @Override
    public void onModuleStart(String curLine, FileWrapper fileWrapper, BugReportParseEngine.ParseResult parseResult) throws IOException {
        super.onModuleStart(curLine, fileWrapper, parseResult);

        mChapterFilterLineAtInfo.setFeature(StatisticInfo.ChapterMapping.ChapterFilterLineAtInfo.FLAG_FEATURE_SPARSE);

        List<Long> extraIncludeLinesList = mChapterFilterLineAtInfo.getExtraIncludeLinesList();

        String line = BugReportParseEngine.readLine(fileWrapper, mFilter, mParseMode, parseResult);
        if (line == null) {
            onModuleEnd(line, fileWrapper, parseResult);
            return;
        }

        extraIncludeLinesList.add(mCacheLinesStatistic.getCurrentLine());

        do {
            line = BugReportParseEngine.readLine(fileWrapper, mFilter, mParseMode, parseResult);

            if (StringUtils.kmp_indexOf(line, mFilter) > -1) {
                // Record filtered line.
                extraIncludeLinesList.add(mCacheLinesStatistic.getCurrentLine());
            }
        } while (line != null && StringUtils.kmp_indexOf(line, Consts.BUGREPORT_CHAPTER_PROCESSES_AND_THREADS_END) < 0);

        onModuleEnd(line, fileWrapper, parseResult);
    }

    @Override
    public void onModuleEnd(String curLine, FileWrapper fileWrapper, BugReportParseEngine.ParseResult parseResult) throws IOException {
        super.onModuleEnd(curLine, fileWrapper, parseResult);
    }
}
