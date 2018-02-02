package com.uc.phantom.bra.framework.scan_parse.parser.handler;

import com.uc.phantom.bra.framework.common.Consts;
import com.uc.phantom.bra.framework.scan_parse.parser.BugReportParseEngine;
import com.uc.phantom.bra.framework.scan_parse.parser.StatisticInfo;
import com.uc.phantom.bra.framework.utils.FileWrapper;
import com.uc.phantom.bra.framework.utils.Logger;
import com.uc.phantom.bra.ui.swing.utils.StringUtils;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * DumpSys MemInfo Chapter Handler of BugReport.
 * @Author Tam.
 * Modifications :
 * 2017/4/17 : Created.
 */
public class DumpSysMemInfoHandler extends ModuleHandler {

    private final static String TAG = "DUMPSYS MEMINFO";

    public DumpSysMemInfoHandler(FileWrapper fileWrapper, String filter, int parseMode) {
        super(TAG, StatisticInfo.ChapterMapping.CHAPTER_DUMPSYS_MEMINFO, fileWrapper, filter, parseMode);
    }

    @Override
    public void onModuleStart(String curLine, FileWrapper fileWrapper, BugReportParseEngine.ParseResult parseResult) throws IOException {
        super.onModuleStart(curLine, fileWrapper, parseResult);

        mChapterFilterLineAtInfo.setFeature(StatisticInfo.ChapterMapping.ChapterFilterLineAtInfo.FLAG_FEATURE_BLOCK);

        boolean filterModuleStartFlag =false, filterModuleEndFlag = true;
        String line;
        do {
            line = BugReportParseEngine.readLine(fileWrapper, mFilter, mParseMode, parseResult);

            if (line == null && filterModuleStartFlag && !filterModuleEndFlag) {
                // Record filter module end line.
                mChapterFilterLineAtInfo.setChapterFilterEndLine(mCacheLinesStatistic.getCurrentLine());
                break;
            }

            if (StringUtils.kmp_indexOf(line, Consts.BUGREPORT_CHAPTER_DUMPSYS_MEMINFO_FILTER) > -1) {
                if (filterModuleStartFlag) { // Filter module has started.
                    if (!filterModuleEndFlag) { // Filter module has not ended.
                        Logger.getLogger(null).log(mModuleName, Logger.LOG_LEVEL_INFO, "Filter Block End, LineNo=" + mCacheLinesStatistic.getCurrentLine());

                        filterModuleStartFlag = false;
                        filterModuleEndFlag = true;
                        // Record filter module end line.
                        mChapterFilterLineAtInfo.setChapterFilterEndLine(mCacheLinesStatistic.getCurrentLine() - 1);
                    }
                    // Pass left parts.
                } else { // Filter module has not started.
                    if (StringUtils.kmp_indexOf(line, mFilter) > -1) { // Detect qualified filter module started.
                        Logger.getLogger(null).log(mModuleName, Logger.LOG_LEVEL_INFO, "Filter Block Start, LineNo=" + mCacheLinesStatistic.getCurrentLine());

                        filterModuleStartFlag = true;
                        filterModuleEndFlag = false;
                        // Record filter module start line.
                        mChapterFilterLineAtInfo.setChapterFilterStartLine(mCacheLinesStatistic.getCurrentLine());
                    }
                }
            }
        } while (line != null && StringUtils.kmp_indexOf(line, Consts.BUGREPORT_CHAPTER_DUMPSYS_MEMINFO_END) < 0);

        onModuleEnd(line, fileWrapper, parseResult);
    }

    @Override
    public void onModuleEnd(String curLine, FileWrapper fileWrapper, BugReportParseEngine.ParseResult parseResult) throws IOException {
        super.onModuleEnd(curLine, fileWrapper, parseResult);
    }
}
