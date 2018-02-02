package com.uc.phantom.bra.framework.scan_parse.parser.handler;

import com.uc.phantom.bra.framework.scan_parse.parser.BugReportParseEngine;
import com.uc.phantom.bra.framework.scan_parse.parser.StatisticInfo;
import com.uc.phantom.bra.framework.utils.FileWrapper;
import com.uc.phantom.bra.framework.utils.Logger;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;

/**
 * Base Handler of BugReport Chapter Parser.
 * @Author Tam.
 * Modifications :
 * 2017/4/17 : Created.
 */
public abstract class ModuleHandler {
    protected String mModuleName;
    protected FileWrapper mFileWrapper;
    protected String mFilter;
    protected int mParseMode;
    protected int mModuleNo;

    protected StatisticInfo.ChapterMapping.ChapterFilterLineAtInfo mChapterFilterLineAtInfo;
    protected StatisticInfo.CacheLinesStatistic mCacheLinesStatistic;

    protected ModuleHandler(String moduleName, int moduleNo, FileWrapper fileWrapper, String filter, int parseMode) {
        mModuleName  = moduleName;
        mModuleNo    = moduleNo;
        mFileWrapper = fileWrapper;
        mFilter       = filter;
        mParseMode   = parseMode;
    }

    public void onModuleStart(String curLine, FileWrapper fileWrapper, BugReportParseEngine.ParseResult parseResult) throws IOException {
        mFileWrapper = fileWrapper;

        mCacheLinesStatistic = parseResult.getCacheLinesStatistic();

        Logger.getLogger(null).log(mModuleName, Logger.LOG_LEVEL_INFO, "onModuleStart, LineNo=" + mCacheLinesStatistic.getCurrentLine());

        Map<Integer, StatisticInfo.ChapterMapping.ChapterFilterLineAtInfo> mapChapterFilterLineAtInfo = parseResult.getChapterFilterLineAtInfo();
        mChapterFilterLineAtInfo = mapChapterFilterLineAtInfo.get(mModuleNo);
        if (mChapterFilterLineAtInfo == null) {
            mChapterFilterLineAtInfo = new StatisticInfo.ChapterMapping.ChapterFilterLineAtInfo();
            mapChapterFilterLineAtInfo.put(mModuleNo, mChapterFilterLineAtInfo);
        }

        // Set chapter start line.
        mChapterFilterLineAtInfo.setChapterStartLine(mCacheLinesStatistic.getCurrentLine());
        mChapterFilterLineAtInfo.setChapterFilterStartLine(mCacheLinesStatistic.getCurrentLine());
    }

    public void onModuleEnd(String curLine, FileWrapper fileWrapper, BugReportParseEngine.ParseResult parseResult) throws IOException {
        Logger.getLogger(null).log(mModuleName, Logger.LOG_LEVEL_INFO, "onModuleEnd, LineNo=" + mCacheLinesStatistic.getCurrentLine());
        // Set chapter end line.
        mChapterFilterLineAtInfo.setChapterEndLine(mCacheLinesStatistic.getCurrentLine());
    }

    public void setFilter(String filter) {
        mFilter = filter;
    }

    public interface OnFilterListener {
        boolean onFilter(String filter, String curLine);
    }
}
