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
 * Block Processes Wait-Channel Chapter Handler of BugReport.
 * @Author Tam.
 * Modifications :
 * 2017/4/17 : Created.
 */
public class BlockedProcessesWaitChannelHandler extends ModuleHandler {

    private final static String TAG = "BLOCKED PROCESS WAIT-CHANNELS";

    public BlockedProcessesWaitChannelHandler(FileWrapper fileWrapper, String filter, int parseMode) {
        super(TAG, StatisticInfo.ChapterMapping.CHAPTER_BLOCKED_PROCESSES_WAIT_CHANNELS, fileWrapper, filter, parseMode);
    }

    @Override
    public void onModuleStart(String curLine, FileWrapper fileWrapper, BugReportParseEngine.ParseResult parseResult) throws IOException {
        super.onModuleStart(curLine, fileWrapper, parseResult);

        mChapterFilterLineAtInfo.setFeature(StatisticInfo.ChapterMapping.ChapterFilterLineAtInfo.FLAG_FEATURE_BLOCK);

        int startIndentIndex = Integer.MAX_VALUE;
        boolean filterModuleStartFlag =false, filterModuleEndFlag = true;
        String line;
        do { // If has multiple qualified block, we select the last new one.
            line = BugReportParseEngine.readLine(fileWrapper, mFilter, mParseMode, parseResult);

            if (line == null && filterModuleStartFlag && !filterModuleEndFlag) {
                // Record filter module end line.
                mChapterFilterLineAtInfo.setChapterFilterEndLine(mCacheLinesStatistic.getCurrentLine());
                break;
            }

            if (!filterModuleStartFlag && filterModuleEndFlag) {
                if (StringUtils.kmp_indexOf(line, mFilter) > -1) { // Detect qualified filter module started.
                    Logger.getLogger(null).log(mModuleName, Logger.LOG_LEVEL_INFO, "Filter Block Start, LineNo=" + mCacheLinesStatistic.getCurrentLine());

                    filterModuleStartFlag = true;
                    filterModuleEndFlag = false;
                    // Record filter module start line.
                    mChapterFilterLineAtInfo.setChapterFilterStartLine(mCacheLinesStatistic.getCurrentLine());

                    startIndentIndex = getStartIndex(line);
                }
            } else {
                int subStartIndentIndex = getStartIndex(line);
                if (startIndentIndex >= subStartIndentIndex) {
                    filterModuleStartFlag = false;
                    filterModuleEndFlag = true;

                    mChapterFilterLineAtInfo.setChapterFilterEndLine(mCacheLinesStatistic.getCurrentLine());
                }
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

    private int getStartIndex(String line) {
        if (line.equals(""))
            return 0;

        boolean hasDetectedSpace = false;
        int len = line.length();
        for (int i = 0 ; i < len ; i++) {
            if (line.charAt(i) == ' ') {
                if (!hasDetectedSpace) {
                    hasDetectedSpace = true;
                    continue;
                }
            } else if (hasDetectedSpace) {
                return i;
            }
        }
        // No space char found, return line length as indent index.
        return len;
    }
}
