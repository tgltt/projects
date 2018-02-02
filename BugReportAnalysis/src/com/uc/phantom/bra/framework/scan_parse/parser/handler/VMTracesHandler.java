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
 * VM Traces Chapter Handler of BugReport.
 * @Author Tam.
 * Modifications :
 * 2017/4/17 : Created.
 */
public class VMTracesHandler extends ModuleHandler {

    private final static String TAG = "VMTracesHandler";

    public VMTracesHandler(FileWrapper fileWrapper, String filter, int parseMode) {
        super(TAG, StatisticInfo.ChapterMapping.CHAPTER_VM_TRACESJUST_NOW, fileWrapper, filter, parseMode);
    }

    @Override
    public void onModuleStart(String curLine, FileWrapper fileWrapper, BugReportParseEngine.ParseResult parseResult) throws IOException {
        super.onModuleStart(curLine, fileWrapper, parseResult);

        mChapterFilterLineAtInfo.setFeature(StatisticInfo.ChapterMapping.ChapterFilterLineAtInfo.FLAG_FEATURE_BLOCK);

        boolean filterModuleStartFlag =false, filterModuleEndFlag = true;
        long lastBlockStartLine = -1, lastBlockEndLine = -1;
        String line;
        do {
            line = BugReportParseEngine.readLine(fileWrapper, mFilter, mParseMode, parseResult);

            if (line == null && filterModuleStartFlag && !filterModuleEndFlag) {
                // Record filter module end line.
                mChapterFilterLineAtInfo.setChapterFilterEndLine(mCacheLinesStatistic.getCurrentLine());
                break;
            }

            if (StringUtils.kmp_indexOf(line, Consts.BUGREPORT_CHAPTER_VM_TRACES_JUST_NOW_APP_START) > -1) {
                lastBlockStartLine = mCacheLinesStatistic.getCurrentLine();
                lastBlockEndLine = -1;
            } else if (StringUtils.kmp_indexOf(line, Consts.BUGREPORT_CHAPTER_VM_TRACES_JUST_NOW_APP_END) > -1) {
                lastBlockEndLine = mCacheLinesStatistic.getCurrentLine();
            }

            if (!filterModuleStartFlag && filterModuleEndFlag) {
                if (StringUtils.kmp_indexOf(line, mFilter) > -1) {
                    filterModuleStartFlag = true;
                    filterModuleEndFlag   = false;

                    mChapterFilterLineAtInfo.setChapterFilterStartLine(lastBlockStartLine);
                }
            } else if (StringUtils.kmp_indexOf(line, Consts.BUGREPORT_CHAPTER_VM_TRACES_JUST_NOW_APP_END) > -1) {
                filterModuleStartFlag = false;
                filterModuleEndFlag   = true;

                lastBlockEndLine = mCacheLinesStatistic.getCurrentLine();
                mChapterFilterLineAtInfo.setChapterFilterEndLine(lastBlockEndLine);
            }
        } while (line != null && StringUtils.kmp_indexOf(line, Consts.BUGREPORT_CHAPTER_VM_TRACES_JUST_NOW_END) < 0);

        onModuleEnd(line, fileWrapper, parseResult);
    }

    @Override
    public void onModuleEnd(String curLine, FileWrapper fileWrapper, BugReportParseEngine.ParseResult parseResult) throws IOException {
        super.onModuleEnd(curLine, fileWrapper, parseResult);
    }
}
