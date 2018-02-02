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
 * App Activities Chapter Handler of BugReport.
 * @Author Tam.
 * Modifications :
 * 2017/4/17 : Created.
 */
public class AppActivitiesHandler extends ModuleHandler {

    private final static String TAG = "APP ACTIVITIES";

    public AppActivitiesHandler(FileWrapper fileWrapper, String filter, int parseMode) {
        super(TAG, StatisticInfo.ChapterMapping.CHAPTER_APP_ACTIVITIES, fileWrapper, filter, parseMode);
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

            if (line.startsWith(Consts.BUGREPORT_CHAPTER_APP_ACTIVITIES_FILTER)) {
                if (!filterModuleStartFlag && filterModuleEndFlag) { // Filter module has started.
                    if (StringUtils.kmp_indexOf(line, mFilter) > -1) { // Detect qualified filter module started.
                        Logger.getLogger(null).log(mModuleName, Logger.LOG_LEVEL_INFO, "Filter Block Start, LineNo=" + mCacheLinesStatistic.getCurrentLine());

                        filterModuleStartFlag = true;
                        filterModuleEndFlag = false;
                        // Record filter module start line.
                        mChapterFilterLineAtInfo.setChapterFilterStartLine(mCacheLinesStatistic.getCurrentLine());
                    }
                } else { // Filter module has started.
                    Logger.getLogger(null).log(mModuleName, Logger.LOG_LEVEL_INFO, "Filter Block End, LineNo=" + (mCacheLinesStatistic.getCurrentLine() - 1));

                    filterModuleStartFlag = true;
                    filterModuleEndFlag = false;
                    // Record filter module end line.
                    mChapterFilterLineAtInfo.setChapterFilterEndLine(mCacheLinesStatistic.getCurrentLine() - 1);
                }
            }
        } while (line != null && StringUtils.kmp_indexOf(line, Consts.BUGREPORT_CHAPTER_APP_ACTIVITIES_END) < 0);

        onModuleEnd(line, fileWrapper, parseResult);
    }

    @Override
    public void onModuleEnd(String curLine, FileWrapper fileWrapper, BugReportParseEngine.ParseResult parseResult) throws IOException {
        super.onModuleEnd(curLine, fileWrapper, parseResult);
    }
}
