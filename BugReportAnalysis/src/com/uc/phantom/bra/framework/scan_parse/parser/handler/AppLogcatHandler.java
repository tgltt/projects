package com.uc.phantom.bra.framework.scan_parse.parser.handler;

import com.uc.phantom.bra.framework.common.Consts;
import com.uc.phantom.bra.framework.scan_parse.parser.BugReportParseEngine;
import com.uc.phantom.bra.framework.scan_parse.parser.LogScanParseEngine;
import com.uc.phantom.bra.framework.scan_parse.parser.StatisticInfo;
import com.uc.phantom.bra.framework.utils.FileWrapper;
import com.uc.phantom.bra.ui.swing.utils.StringUtils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.regex.Pattern;

/**
 * App Logcat Chapter Handler of BugReport.
 * @Author Tam.
 * Modifications :
 * 2017/4/20 : Created.
 */
public class AppLogcatHandler extends ModuleHandler {

    private final static String TAG = "APPLOGCAT";

    public AppLogcatHandler(FileWrapper fileWrapper, String filter, int parseMode) {
        super(TAG, StatisticInfo.ChapterMapping.CHAPTER_APPLOCAT, fileWrapper, filter, parseMode);
    }

    @Override
    public void onModuleStart(String curLine, FileWrapper fileWrapper, BugReportParseEngine.ParseResult parseResult) throws IOException {
        super.onModuleStart(curLine, fileWrapper, parseResult);

        mChapterFilterLineAtInfo.setFeature(StatisticInfo.ChapterMapping.ChapterFilterLineAtInfo.FLAG_FEATURE_SPARSE_AND_BLOCK);

        StatisticInfo.PlainProblems problems = parseResult.getPlainProblems();
        List<StatisticInfo.PlainProblems.Problem> listException = problems.getExceptionList();
        List<StatisticInfo.PlainProblems.Problem> listFatal     = problems.getFatalList();
        List<StatisticInfo.PlainProblems.Problem> listBuildFingerprint = problems.getBuildFingerprintList();
        List<StatisticInfo.PlainProblems.Problem> listDexOpt    = problems.getDexOptList();

        ;
        List<Long> filterInfoList = mChapterFilterLineAtInfo.getExtraIncludeLinesList(); //parseResult.getFilterInfo().getFilterInfoList();

        boolean filterModuleStartFlag =false, filterModuleEndFlag = true;
        String line, filterTag = "";
        StatisticInfo.PlainProblems.Problem problem = null;
        do {
            line = LogScanParseEngine.readLine(fileWrapper, mFilter, mParseMode, parseResult);

            if (line == null/* && filterModuleStartFlag && !filterModuleEndFlag*/) {
                problem.getChapterFilterLineAtInfo().setChapterEndLine(mCacheLinesStatistic.getCurrentLine());
                break;
            }

            if (!filterModuleStartFlag && filterModuleEndFlag) {
                boolean detectedProblemFlag = false;
                if (StringUtils.kmp_indexOf(line, Consts.PROBLEM_EXCEPTION_START) > -1 || StringUtils.kmp_indexOf(line, Consts.PROBLEM_ERROR_START) > -1) {
                    detectedProblemFlag = true;

                    problem = new StatisticInfo.PlainProblems.Problem();
                    listException.add(problem);
                } else if (StringUtils.kmp_indexOf(line, Consts.PROBLEM_FATAL_START) > -1) {
                    detectedProblemFlag = true;

                    problem = new StatisticInfo.PlainProblems.Problem();
                    listFatal.add(problem);
                } else if (StringUtils.kmp_indexOf(line, Consts.PROBLEM_BUILD_FINGERPRINT_START) > -1) {
                    detectedProblemFlag = true;

                    problem = new StatisticInfo.PlainProblems.Problem();
                    listBuildFingerprint.add(problem);
                } else if (StringUtils.kmp_indexOf(line, Consts.PROBLEM_DEXOPT) > -1) {
                    detectedProblemFlag = true;

                    problem = new StatisticInfo.PlainProblems.Problem();
                    listDexOpt.add(problem);
                } else if (StringUtils.kmp_indexOf(line, mFilter) > -1) { // Current line contains filter text.
                    filterInfoList.add(mCacheLinesStatistic.getCurrentLine());
                }

                if (detectedProblemFlag) { // Detect problem text block.
                    problem.getChapterFilterLineAtInfo().setChapterStartLine(mCacheLinesStatistic.getCurrentLine());

                    filterModuleStartFlag = true;
                    filterModuleEndFlag   = false;

                    String[] lineParts = line.split(" ");
                    filterTag = lineParts[5];
                }

            } else if (StringUtils.kmp_indexOf(line, filterTag) < 0) {
                filterModuleStartFlag = false;
                filterModuleEndFlag   = true;

                problem.getChapterFilterLineAtInfo().setChapterEndLine(mCacheLinesStatistic.getCurrentLine() - 1);

                if (StringUtils.kmp_indexOf(line, mFilter) > -1) { // Current line contains filter text.
                    filterInfoList.add(mCacheLinesStatistic.getCurrentLine());
                }
            }
        } while (line != null && StringUtils.kmp_indexOf(line, Consts.BUGREPORT_CHAPTER_LOGCAT_END) < 0);

        onModuleEnd(line, fileWrapper, parseResult);
    }

    @Override
    public void onModuleEnd(String curLine, FileWrapper fileWrapper, BugReportParseEngine.ParseResult parseResult) throws IOException {
        super.onModuleEnd(curLine, fileWrapper, parseResult);
    }
}
