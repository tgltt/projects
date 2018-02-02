package com.uc.phantom.bra.framework.warehouse.problems.knowledge_lib.anr;

import com.uc.phantom.bra.framework.warehouse.problems.knowledge_lib.BaseProblem;
import com.uc.phantom.bra.ui.swing.utils.Logger;
import com.uc.phantom.bra.ui.swing.utils.StringUtils;

import java.lang.reflect.Array;
import java.util.*;

/**
 * ANR Problem Base Class.
 * @Author Tam.
 * Modifications :
 * 2017/4/17 : Created.
 */
public class ANRBase extends BaseProblem {

    private final static String TAG = "ANRBase";

    public final static int PARSE_RESULT_DETECT_REASON     = 1;
    public final static int PARSE_RESULT_OK = 0;
    public final static int PARSE_RESULT_DETECT_NO_REASON  = -1;
    public final static int PARSE_RESULT_TRACES_MAYNOT_CORRECT = -9;
    public final static int PARSE_RESULT_NO_TRACES = -10;

    public final static int DIAGNOSE_TRACK_OK = 101;
    public final static int DIAGNOSE_TRACK_FAILED = -101;
    public final static int DIAGNOSE_TRACK_TRACES_OK     = 100;
    public final static int DIAGNOSE_TRACK_TRACES_FAILED = -100;



    private ANRParsedResult mANRParsedResult;

    public ANRBase(StringBuilder problemStringBlock) {
        super(problemStringBlock);
    }

    public ANRBase(StringBuilder problemStringBlock, ANRParsedResult parsedResult) {
        super(problemStringBlock);
        mANRParsedResult = parsedResult;
    }

    public List<String> getProblemFeatures() {
        return null;
    }

    public List<String> getProblemReasons() {
        return null;
    }

    public List<String> getSolution() {
        return null;
    }

    public ANRDiagnoseResult diagnose(ANRParsedResult parsedResult) {
        if (parsedResult == null)
            return null;

        List<ANRParsedResult.ANRAgoLater> agoLaters = parsedResult.getANRAgoLaterList();
        if (agoLaters == null || agoLaters.size() <= 0) // No enough information, return the parsed result as diagnose result.
            return new ANRDiagnoseResult(parsedResult);
        // Get the first item to analyze.
        ANRParsedResult.ANRAgoLater anrAgoLater = agoLaters.get(0);
        List<ANRItem> agoTopCpuProcessList   = new ArrayList<ANRItem>();
        List<ANRItem> laterTopCpuProcessList = new ArrayList<ANRItem>();

        analyzeAgoLaterTopCpuProcess(anrAgoLater, agoTopCpuProcessList, laterTopCpuProcessList);

        // Diagnosing
        ANRItem causeProcess = getCauseProcess(agoTopCpuProcessList, laterTopCpuProcessList);
        if (causeProcess == null) {
            return new ANRDiagnoseResult(parsedResult);
        }

        return diagnose(parsedResult, causeProcess);
    }

    private ANRDiagnoseResult diagnose(ANRParsedResult parsedResult,  ANRItem causeProcess) {
        ANRDiagnoseResult diagnoseResult = new ANRDiagnoseResult(parsedResult);

        String pid = causeProcess.getThreadId();
        List<String> tidList = new ArrayList<String>();
        List<ANRItem> anrItems = causeProcess.getThreadsCpuFrequencies();
        if (anrItems != null && anrItems.size() > 1) {
            for (ANRItem anrItem : anrItems) {
                tidList.add(anrItem.getThreadId());
            }
        } else {
            tidList.add(pid);
        }

        diagnoseResult.setMostExaustTimeItem(causeProcess);

        ANRParsedResult.Traces traces = parsedResult.getTraces();
        final List<ANRParsedResult.Traces.ThreadTraces> listThreadTraces = traces.getThreadTraces();
        for (ANRParsedResult.Traces.ThreadTraces threadTrace : listThreadTraces) {
            String tid = threadTrace.getThreadId();
            if (tidList.contains(tid)) {
                diagnoseResult.getThreadTraces().add(threadTrace);
            }

            if (threadTrace.getThreadName().equals("main")) {
                diagnoseResult.setMainThreadStatus(threadTrace);
            }
        }

        List<String> diagnoseSection = diagnoseResult.getDiagnoseResult();
        diagnoseSection.add(String.format("Your most exaust time process is [%s/%s]. ", causeProcess.getThreadId(), causeProcess.getThreadName()));
        double totalCpuPercent = causeProcess.getCpuPercentTotal();
        if (totalCpuPercent <= 30.0) {
            diagnoseSection.add(String.format("But %s's cpu occupied rate %.2f is not heavy, you should observe other log part to ananylze ANR. ", causeProcess.getThreadName(), totalCpuPercent));
        } else {
            diagnoseSection.add(String.format("%s's cpu occupied rate %.2f is heavy. ", causeProcess.getThreadName(), totalCpuPercent));

            Map<Double, String> heavyPartMap = new HashMap<Double, String>();
            heavyPartMap.put(causeProcess.getCpuPercentKernel(), "kernel");
            heavyPartMap.put(causeProcess.getCpuPercentUser(), "user");
            heavyPartMap.put(causeProcess.getCpuPercentIOWait(), "iowait");
            heavyPartMap.put(causeProcess.getCpuPercentSoftIrq(), "softirq");

            double[] heavyArr = new double[]{
                    causeProcess.getCpuPercentKernel(),
                    causeProcess.getCpuPercentUser(),
                    causeProcess.getCpuPercentIOWait(),
                    causeProcess.getCpuPercentSoftIrq()
            };

            Arrays.sort(heavyArr);

            diagnoseSection.add(String.format("%s's part is exaust cpu heavily. ", heavyPartMap.get(heavyArr[heavyArr.length - 1])));
        }

        return diagnoseResult;
    }

    private ANRItem getCauseProcess(List<ANRItem> agoTopCpuProcessList, List<ANRItem> laterTopCpuProcessList) {
        ANRItem agoTopCpuProcess = null;
        if (agoTopCpuProcessList.size() >= 2) { // Get the toppest cpu percentage process.
            agoTopCpuProcess = agoTopCpuProcessList.get(0);
            ANRItem anotherAgoTopCpuProcess = agoTopCpuProcessList.get(1);
            if (agoTopCpuProcess.getCpuPercentTotal() < anotherAgoTopCpuProcess.getCpuPercentTotal())
                agoTopCpuProcess = anotherAgoTopCpuProcess;
        }

        ANRItem laterTopCpuProcess = null;
        if (laterTopCpuProcessList.size() >= 2) { // Get the toppest cpu percentage process.
            laterTopCpuProcess = laterTopCpuProcessList.get(0);
            ANRItem anotherLaterTopCpuProcess = laterTopCpuProcessList.get(1);
            if (laterTopCpuProcess.getCpuPercentTotal() < anotherLaterTopCpuProcess.getCpuPercentTotal())
                laterTopCpuProcess = anotherLaterTopCpuProcess;
        }
        // Get the most cpu consumed process.
        ANRItem causeProcess = agoTopCpuProcess;
        if (agoTopCpuProcess != null && laterTopCpuProcess == null) {
            causeProcess = agoTopCpuProcess;
        } else if (agoTopCpuProcess == null && laterTopCpuProcess != null) {
            causeProcess = laterTopCpuProcess;
        } else if (agoTopCpuProcess != null && laterTopCpuProcess != null) {
            if (causeProcess.getCpuPercentTotal() < laterTopCpuProcess.getCpuPercentTotal()) {
                causeProcess = laterTopCpuProcess;
            }
        }

        return causeProcess;
    }

    private void analyzeAgoLaterTopCpuProcess(ANRParsedResult.ANRAgoLater anrAgoLater, List<ANRItem> agoTopCpuProcessList, List<ANRItem> laterTopCpuProcessList) {
        ANRItem agoTotal;
        ANRItem laterTotal;
        // Get ago part.
        ANRParsedResult.ANRAgoLater.ANRAgo ago = anrAgoLater.getANRAgo();
        List<List<ANRItem>> agoAnrItemsList = ago.getAgoCpuUsagePercentageProcess();
        if (agoAnrItemsList != null && agoAnrItemsList.size() > 0) {
            List<ANRItem> anrItems = agoAnrItemsList.get(0); // Get the first ago part.
            if (anrItems != null && anrItems.size() > 0) {
                int len = anrItems.size();
                if (len > 2) // Only get 2 top cpu percentage process to analyze.
                    len = 2;

                for (int i = 0 ; i < len ; i++) {
                    ANRItem anrItem = anrItems.get(i);
                    agoTopCpuProcessList.add(anrItem);
                }

            }
        }

        List<ANRItem> anrTotalItems = ago.getTotalItems();
        if (anrTotalItems != null && anrTotalItems.size() > 0) { // Get total item.
            agoTotal = anrTotalItems.get(0);
        } else {
            agoTotal = null;
        }
        // Get the later part.
        ANRParsedResult.ANRAgoLater.ANRLater later = anrAgoLater.getANRLater();
        List<List<ANRItem>> laterAnrItemsList = later.getLaterCpuUsagePercentageProcess();
        if (laterAnrItemsList != null && laterAnrItemsList.size() > 0) {
            List<ANRItem> anrItems = laterAnrItemsList.get(0); // Get the first later part.
            if (anrItems != null && anrItems.size() > 0) {
                int len = anrItems.size();
                if (len > 2) // Only get 2 top cpu percentage process to analyze.
                    len = 2;

                for (int i = 0 ; i < len ; i++) {
                    ANRItem anrItem = anrItems.get(i);
                    laterTopCpuProcessList.add(anrItem);
                }

            }
        }

        anrTotalItems = later.getTotalItems();
        if (anrTotalItems != null && anrTotalItems.size() > 0) { // Get total item.
            laterTotal = anrTotalItems.get(0);
        } else {
            laterTotal = null;
        }
    }

    public ANRParsedResult recognize(final List<String> anrLogcat, List<String> anrTraces) {
        if (anrLogcat == null || anrLogcat.size() <= 0)
            return null;

        ANRParsedResult anrParsedResult = new ANRParsedResult();

        int totalLine = anrLogcat.size();
        int startIdx;
        String line;
        boolean anrBlockStart = false, agoStart = false, agoEnd = true, latterStart = false, latterEnd = true;
        ANRParsedResult.ANRAgoLater agoLater = null;
        for (int i = 0 ; i < totalLine ; i++) {
            line = anrLogcat.get(i);
            if ((startIdx = StringUtils.kmp_indexOf(line, "ANR in ")) >= 0) {
                anrBlockStart = true;
                anrParsedResult.setSource(line.substring(startIdx + "ANR in ".length()).trim(), i);
                anrParsedResult.getReasonDetail().getLogcatLineNoList().add(i);
            }

            if (!anrBlockStart) {
                continue;
            }

            if ((startIdx = StringUtils.kmp_indexOf(line, "PID:")) >= 0) {
                anrParsedResult.setPid(line.substring(startIdx + "PID:".length()).trim(), i);
                anrParsedResult.getReasonDetail().getLogcatLineNoList().add(i);
            } else if ((startIdx = StringUtils.kmp_indexOf(line, "Reason:")) >= 0) {
                anrParsedResult.setReason(line.substring(startIdx + "Reason:".length()).trim(), i);
                anrParsedResult.getReasonDetail().getLogcatLineNoList().add(i);
            } else if ((startIdx = StringUtils.kmp_indexOf(line, "CPU usage from")) >= 0) {
                anrParsedResult.getReasonDetail().getLogcatLineNoList().add(i);

                List<ANRItem> listAnrItem = new ArrayList<ANRItem>();
                if (StringUtils.kmp_indexOf(line, "ms ago (") >= 0) { // Ago part start.
                    if (agoLater != null && agoLater.getStatus() == ANRParsedResult.ANRAgoLater.PARSE_STATUS_FINISH_LATER) {
                        agoLater.finishLater();
                        agoLater.finish();
                    } else if (agoLater != null && agoLater.getStatus() == ANRParsedResult.ANRAgoLater.PARSE_STATUS_START_AGO) {
                        agoLater.finishAgo();
                        agoLater.finish();
                    }

                    agoLater = new ANRParsedResult.ANRAgoLater();
                    anrParsedResult.getANRAgoLaterList().add(agoLater);

                    agoLater.start();
                    agoLater.startAgo();

                    agoLater.getANRAgo().getAgoCpuUsagePercentageProcess().add(listAnrItem);
                } else { // Later part start.
                    if (agoLater == null) {
                        agoLater = new ANRParsedResult.ANRAgoLater();
                        anrParsedResult.getANRAgoLaterList().add(agoLater);

                        agoLater.start();
                        agoLater.startLater();
                    } else if (agoLater.getStatus() == ANRParsedResult.ANRAgoLater.PARSE_STATUS_START_AGO) {
                        agoLater.finishAgo();
                        agoLater.startLater();
                    } else if (agoLater.getStatus() == ANRParsedResult.ANRAgoLater.PARSE_STATUS_FINISH_AGO) {
                        agoLater.startLater();
                    } else if (agoLater.getStatus() == ANRParsedResult.ANRAgoLater.PARSE_STATUS_START_LATER) {
                        ANRItem anrItem = new ANRItem();
                        agoLater.getANRLater().getTotalItems().add(anrItem); // Add dumy total item.

                        agoLater.finishLater();
                        agoLater.startLater();
                    } else if (agoLater.getStatus() == ANRParsedResult.ANRAgoLater.PARSE_STATUS_FINISH_LATER) {
                        agoLater.startLater();
                    }

                    // Replace previous, if has multiple later part.
                    agoLater.getANRLater().getLaterCpuUsagePercentageProcess().add(listAnrItem);
                }
                i = parseCpuPercentage(anrLogcat, anrTraces, startIdx, i, totalLine, listAnrItem, anrParsedResult);
            } else if ((startIdx = StringUtils.kmp_indexOf(line, " TOTAL: ")) >= 0) {
                anrParsedResult.getReasonDetail().getLogcatLineNoList().add(i);

                if (agoLater != null) {
                    List<ANRItem> listAnrItem;
                    if (agoLater.getStatus() == ANRParsedResult.ANRAgoLater.PARSE_STATUS_START_AGO) {
                        agoLater.finishAgo();
                        listAnrItem = agoLater.getANRAgo().getTotalItems();
                    } else {
                        agoLater.finishLater();
                        listAnrItem = agoLater.getANRLater().getTotalItems();
                    }

                    i = parseAgoOrLaterTotalCpuPercentage(anrLogcat, anrTraces, startIdx, i, totalLine, listAnrItem, anrParsedResult);
                }
            }
        }

        if (agoLater != null) {
            if (agoLater.getStatus() == ANRParsedResult.ANRAgoLater.PARSE_STATUS_START_AGO) {
                agoLater.finishAgo();
            } else if (agoLater.getStatus() == ANRParsedResult.ANRAgoLater.PARSE_STATUS_START_LATER) {
                agoLater.finishLater();
            }
            agoLater.finish();
        }

        trackProcessTrace(anrTraces, anrParsedResult);

        return anrParsedResult;
    }

    public static ANRBase analyzeParseResult(ANRBase.ANRParsedResult anrParsedResult) {
        return null;
    }

    private static int parseAgoOrLaterTotalCpuPercentage(final List<String> anrLogcat, List<String> anrTraces, int startIdx, int curLinePos, int totalLine, List<ANRItem> anrItems, ANRParsedResult parsedResult) {
        String line = anrLogcat.get(curLinePos++);

        ANRItem anrItem = new ANRItem();
        anrItems.add(anrItem);

        for (int i = startIdx ; i >= 0 ; i--) {
            char ch = line.charAt(i);
            if (ch == ':') {
                startIdx = i + 1;
                break;
            }
        }

        fillCpuPercentage(anrItem, line, startIdx);

        if (StringUtils.kmp_indexOf(line, "CPU usage from") >= 0
                || StringUtils.kmp_indexOf(line, "% TOTAL:") >= 0) {
            return curLinePos - 1;
        } else {
            return curLinePos;
        }
    }

    private static int parseCpuPercentage(final List<String> anrLogcat, List<String> anrTraces, int startIdx, int curLinePos, int totalLine, List<ANRItem> anrItems, ANRParsedResult parsedResult) {
        if (curLinePos + 1 >= totalLine) {
            return curLinePos;
        }

        String line = anrLogcat.get(++curLinePos);
        //parsedResult.getReasonDetail().getLogcatLineNoList().add(curLinePos);

        int len = line.length();
        while(startIdx < len && line.charAt(startIdx) == ' ') {
            startIdx++;
        }

        while (curLinePos < totalLine) {
            for (int j = 0 ; j < 3 ; j++) {
                line = anrLogcat.get(curLinePos);
                if (StringUtils.kmp_indexOf(line, "CPU usage from") >= 0
                     || StringUtils.kmp_indexOf(line, "% TOTAL:") >= 0)
                    return curLinePos - 1;

                parsedResult.getReasonDetail().getLogcatLineNoList().add(curLinePos);

                ANRItem anrItem = new ANRItem();
                fillCpuPercentage(anrItem, line, startIdx);

                do {
                    ++curLinePos;
                    if (curLinePos >= totalLine) {
                        break;
                    }

                    line = anrLogcat.get(curLinePos);
                    if (StringUtils.kmp_indexOf(line, "CPU usage from") >= 0
                            || StringUtils.kmp_indexOf(line, "% TOTAL:") >= 0) {
                        anrItems.add(anrItem);
                        return curLinePos - 1;
                    }

                    len = line.length();
                    if (startIdx >= len) {
                        continue;
                    }

                    if (line == null || line.charAt(startIdx) != ' ')
                        break;

//                    int k = startIdx + 1;
//                    while (line.charAt(k) == ' ')
//                        k++;

                    int subStartIdx = startIdx + 1;
                    while (subStartIdx < len && line.charAt(subStartIdx) == ' ')
                        subStartIdx++;

                    if (startIdx >= subStartIdx)
                        break;

                    ANRItem subItem = new ANRItem();
                    fillCpuPercentage(subItem, line, subStartIdx);
                    anrItem.getThreadsCpuFrequencies().add(subItem);
                } while (true);

                anrItems.add(anrItem);
            }
        }

        return curLinePos;
    }

    private static int trackProcessTrace(List<String> anrTraces, ANRParsedResult parsedResult) {
        ANRParsedResult.Traces traces = new ANRParsedResult.Traces();
        parsedResult.setTraces(traces);

        boolean processStart = false, processEnd = true;
        boolean nativeStart = false, kernelStart = false, javaStart = false;
        String line;
        int len = anrTraces.size(), startIdx, lineLen;
        ANRParsedResult.Traces.ThreadTraces threadTraces = null;
        for (int i = 0 ; i < len ; i++) {
            line = anrTraces.get(i);
            if (line.length() <= 0)
                continue;

            if ((startIdx = StringUtils.kmp_indexOf(line, "----- pid ")) > -1) {
                processStart = true;
                processEnd = false;

                startIdx += "----- pid ".length();
                lineLen = line.length();
                while (line.charAt(startIdx) == ' ' && startIdx < lineLen)
                    startIdx++;

                if (startIdx >= lineLen) {
                    continue;
                }

                for (int j = startIdx ; j < lineLen ; j++) {
                    char ch = line.charAt(j);
                    if (ch >= '0' && ch <= '9')
                        continue;

                    traces.setPid(line.substring(startIdx, j));
                    break;
                }
            } else if (StringUtils.kmp_indexOf(line, "----- end") > -1) {
                processStart = false;
                processEnd = true;

                break;
            } else if (line.charAt(0) == '"') { // Thread stack start.
                threadTraces = new ANRParsedResult.Traces.ThreadTraces();
                traces.getThreadTraces().add(threadTraces);

                nativeStart = false;
                kernelStart = false;
                javaStart = false;

                lineLen = line.length();
                startIdx = 1;
                for (int j = startIdx ; j < lineLen ; j++) {
                    if (line.charAt(j) == '"') {
                        threadTraces.setThreadName(line.substring(startIdx, j));
                        break;
                    }
                }

                startIdx = StringUtils.kmp_indexOf(line, " tid=");
                if (startIdx > -1 && startIdx < lineLen) {
                    startIdx += " tid=".length();
                    for (int j = startIdx ; j < lineLen ; j++) {
                        char ch = line.charAt(j);
                        if (ch >= '0' && ch <= '9') {
                            continue;
                        }

                        threadTraces.setThreadId(line.substring(startIdx, j));
                        startIdx = j;
                        break;
                    }
                } else {
                    continue;
                }

                while (line.charAt(startIdx) == ' ' && startIdx < lineLen)
                    startIdx++;

                if (startIdx < lineLen) {
                    threadTraces.setStatus(line.substring(startIdx));
                }
            } else if ((startIdx = StringUtils.kmp_indexOf(line, " sysTid=")) > -1) {
                lineLen = line.length();
                startIdx += " sysTid=".length();
                for (int j = startIdx ; j < lineLen ; j++) {
                    char ch = line.charAt(j);
                    if (ch >= '0' && ch <= '9') {
                        continue;
                    }

                    threadTraces.setSysTid(line.substring(startIdx, j));
                    break;
                }
            } else if (StringUtils.kmp_indexOf(line, "kernel:") > -1 && !kernelStart) {
                kernelStart = true;
                threadTraces.setKernelStartLineNo(i);
            } else if (StringUtils.kmp_indexOf(line, "native:") > -1 && !nativeStart) {
                nativeStart = true;
                threadTraces.setNativeStartLineNo(i);
            } else if (StringUtils.kmp_indexOf(line, " at ") > -1 && !javaStart) {
                javaStart = true;
                threadTraces.setJavaStartLineNo(i);
            }
        }

        return PARSE_RESULT_OK;
    }

    private static String getAnrPidFromTraces(List<String> anrTraces) {
        for (String line : anrTraces) {
            int startIdx = StringUtils.kmp_indexOf(line, "----- pid");
            if (startIdx < 0)
                continue;

            startIdx += "----- pid".length();

            while(line.charAt(startIdx++) == ' ');

            int endIdx = startIdx;
            char ch;
            do {
                ch = line.charAt(endIdx++);
            }
            while (ch >= '0' && ch <= '9');

            return line.substring(startIdx, endIdx);
        }

        return null;
    }

    private static void fillCpuPercentage(ANRItem anrItem, String line, int startIdx) {
        String subLine = line.substring(startIdx);

        anrItem.setThreadName(getThreadName(subLine));
        anrItem.setThreadId(getThreadID(subLine));

        anrItem.setCpuPercentTotal(getProcessCpuPercentage(subLine, 0));
        anrItem.setCpuPercentUser(getCpuPercentageDetail(subLine, "% user"));
        anrItem.setCpuPercentKernel(getCpuPercentageDetail(subLine, "% kernel"));
        anrItem.setCpuPercentIOWait(getCpuPercentageDetail(subLine, "% iowait"));
        anrItem.setCpuPercentSoftIrq(getCpuPercentageDetail(subLine, "% softirq"));
    }

    private static double getProcessCpuPercentage(String subLine, int subLineStartIdx) {
        double percentageCpu = 0.0;
        try {
            percentageCpu = Double.parseDouble(subLine.substring(subLineStartIdx, StringUtils.kmp_indexOf(subLine, "%")));
        } catch (NumberFormatException nfex) {
            Logger.getLogger(null).log(TAG, nfex.getMessage(), nfex);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return percentageCpu;
    }

    private static String getThreadID(String line) {
        int startIdx = StringUtils.kmp_indexOf(line, "%");
        if (startIdx < 0)
            return null;

        while (line.charAt(++startIdx) == ' ');

        int len = line.length();
        int endIdx = startIdx;
        char ch;
        do {
            ch = line.charAt(endIdx++);
        } while (ch >= '0' && ch <= '9' && endIdx < len);

        if (startIdx >= endIdx) {
            return "";
        }

        return line.substring(startIdx, endIdx - 1);
    }

    private static String getThreadName(String line) {
        int startIdx = StringUtils.kmp_indexOf(line, "/");
        if (startIdx < 0)
            return null;

        int len = line.length();
        startIdx++;
        int endIdx = startIdx + 1;
        while (line.charAt(endIdx++) != ':');

        return line.substring(startIdx, endIdx);
    }


    private static double getCpuPercentageDetail(String line, String suffix) {
        int endIdx = StringUtils.kmp_indexOf(line, suffix);
        if (endIdx < 0)
            return 0.0;

        int startIdx = -1;
        for (int i = endIdx - 1; i >= 0 ; i--) {
            char ch = line.charAt(i);
            if ((ch >= '0' && ch <= '9') ||  ch == '.') {
                continue;
            }

            startIdx = i + 1;
            break;
        }

        if (startIdx < 0 || startIdx >= endIdx) {
            return 0.0;
        }

        double percentageCpu = 0.0;
        try {
            percentageCpu = Double.parseDouble(line.substring(startIdx, endIdx));
        } catch (NumberFormatException nfex) {
            Logger.getLogger(null).log(TAG, nfex.getMessage(), nfex);
        }

        return percentageCpu;
    }

    public static class ANRDiagnoseResult {
        private ANRParsedResult mANRParsedResult;

        private List<ANRParsedResult.Traces.ThreadTraces> mThreadTraces;
        private ANRItem mMostExaustTimeItem;
        private ANRParsedResult.Traces.ThreadTraces mMainThreadStatus;
        private List<String> mDiagnoseResult;


        public ANRDiagnoseResult(ANRParsedResult anrParsedResult) {
            mANRParsedResult = anrParsedResult;
            mThreadTraces = new ArrayList<ANRParsedResult.Traces.ThreadTraces>();
            mDiagnoseResult = new ArrayList<String>();
        }

        public List<ANRParsedResult.Traces.ThreadTraces> getThreadTraces() {
            return mThreadTraces;
        }

        public ANRParsedResult getANRParsedResult() {
            return mANRParsedResult;
        }

        public ANRItem getMostExaustTimeItem() {
            return mMostExaustTimeItem;
        }

        public void setMostExaustTimeItem(ANRItem mostExaustTimeItem) {
            mMostExaustTimeItem = mostExaustTimeItem;
        }

        public ANRParsedResult.Traces.ThreadTraces getMainThreadStatus() {
            return mMainThreadStatus;
        }

        public void setMainThreadStatus(ANRParsedResult.Traces.ThreadTraces mainThreadStatus) {
            mMainThreadStatus = mainThreadStatus;
        }

        public List<String> getDiagnoseResult() {
            return mDiagnoseResult;
        }
    }

    public static class ANRParsedResult {
        public final static String KEY_PID    = "pid";
        public final static String KEY_REASON = "reason";
        public final static String KEY_SOURCE = "source";


        private String mSource;
        private String mPid;
        private String mReason;
        private ReasonDetail mReasonDetail;
        private int mResultFlag;
        private List<ANRAgoLater> mANRAgoLaterList;
        private Traces mTraces;
        private String mTag;
        private Map<String, Integer> mLine2LineNo;


        public ANRParsedResult() {
            mReasonDetail = new ReasonDetail();
            mANRAgoLaterList = new ArrayList<ANRAgoLater>();
            mLine2LineNo = new HashMap<String, Integer>();
        }

        public Integer getLineNo(String key) {
            return mLine2LineNo.get(key);
        }

        public String getSource() {
            return mSource;
        }

        public void setSource(String source, int lineNo) {
            mSource = source;
            mLine2LineNo.put(KEY_SOURCE, lineNo);
        }

        public String getPid() {
            return mPid;
        }

        public void setPid(String pid, int lineNo) {
            mPid = pid;
            mLine2LineNo.put(KEY_PID, lineNo);
        }

        public String getReason() {
            return mReason;
        }

        public void setReason(String reason, int lineNo) {
            mReason = reason;
            mLine2LineNo.put(KEY_REASON, lineNo);
        }

        public ReasonDetail getReasonDetail() {
            return mReasonDetail;
        }

        public void setReasonDetail(ReasonDetail reasonDetail) {
            mReasonDetail = reasonDetail;
        }

        public int getResultFlag() {
            return mResultFlag;
        }

        public void setResultFlag(int resultFlag) {
            mResultFlag = resultFlag;
        }

        public List<ANRAgoLater> getANRAgoLaterList() {
            return mANRAgoLaterList;
        }

        public String getTag() {
            return mTag;
        }

        public void setTag(String tag) {
            mTag = tag;
        }

        public Traces getTraces() {
            return mTraces;
        }

        public void setTraces(Traces traces) {
            mTraces = traces;
        }

        public Map<String, Integer> getLine2LineNoMap() {
            return mLine2LineNo;
        }

        public void relocate(int logcatStartLine, int traceStartLine) {
            final Set<String> keySet = mLine2LineNo.keySet();
            for (String key : keySet) {
                try {
                    int lineNo = mLine2LineNo.get(key).intValue();
                    mLine2LineNo.put(key, new Integer(lineNo + logcatStartLine));
                } catch (Exception ex) {
                    ex.printStackTrace();

                }
            }

            if (mReasonDetail != null) {
                mReasonDetail.relocate(logcatStartLine, traceStartLine);
            }
        }

        public static class  ANRAgoLater {
            public final static int PARSE_STATUS_NEW          = 0;
            public final static int PARSE_STATUS_START        = 1;
            public final static int PARSE_STATUS_START_AGO    = 2;
            public final static int PARSE_STATUS_FINISH_AGO   = 3;
            public final static int PARSE_STATUS_START_LATER  = 4;
            public final static int PARSE_STATUS_FINISH_LATER = 5;
            public final static int PARSE_STATUS_FINISHED      = 6;

            public final static int BLOCK_TYPE_NONE  = 0;
            public final static int BLOCK_TYPE_AGO   = 1;
            public final static int BLOCK_TYPE_LATER = 2;

            private ANRAgo mANRAgo;
            private ANRLater mANRLater;

            private int mLastBlockType;
            private int mStatus;

            public ANRAgoLater() {
                mStatus = PARSE_STATUS_NEW;
                mLastBlockType = BLOCK_TYPE_NONE;

                mANRAgo = new ANRAgo();
                mANRLater = new ANRLater();
            }

            public void start() {
                mStatus = PARSE_STATUS_START;
            }

            public void finish() {
                mStatus = PARSE_STATUS_FINISHED;
                mLastBlockType = BLOCK_TYPE_NONE;
            }

            public void startAgo() {
                mStatus = PARSE_STATUS_START_AGO;
            }

            public void finishAgo() {
                mStatus = PARSE_STATUS_FINISH_AGO;
                mLastBlockType = BLOCK_TYPE_AGO;
            }

            public void startLater() {
                mStatus = PARSE_STATUS_START_LATER;
            }

            public void finishLater() {
                mStatus = PARSE_STATUS_FINISH_LATER;
                mLastBlockType = BLOCK_TYPE_LATER;
            }

            public int getStatus() {
                return mStatus;
            }

            public ANRAgo getANRAgo() {
                return mANRAgo;
            }

            public ANRLater getANRLater() {
                return mANRLater;
            }

            public int getLastBlockType() {
                return mLastBlockType;
            }

            public static class ANRLater {
                private List<List<ANRItem>> mLaterCpuUsagePercentageProcess;
                private List<ANRItem> mTotalItems;

                public ANRLater() {
                    mLaterCpuUsagePercentageProcess = new ArrayList<List<ANRItem>>();
                    mTotalItems = new ArrayList<ANRItem>();
                }

                public List<List<ANRItem>> getLaterCpuUsagePercentageProcess() {
                    return mLaterCpuUsagePercentageProcess;
                }

                public void setLaterCpuUsagePercentageProcess(List<List<ANRItem>> agoCpuUsagePercentageProcess) {
                    mLaterCpuUsagePercentageProcess = agoCpuUsagePercentageProcess;
                }

                public List<ANRItem> getTotalItems() {
                    return mTotalItems;
                }
            }

            public static class ANRAgo {
                private List<List<ANRItem>> mAgoCpuUsagePercentageProcess;
                private List<ANRItem> mTotalItems;

                public ANRAgo() {
                    mAgoCpuUsagePercentageProcess = new ArrayList<List<ANRItem>>();
                    mTotalItems = new ArrayList<ANRItem>();
                }

                public List<List<ANRItem>> getAgoCpuUsagePercentageProcess() {
                    return mAgoCpuUsagePercentageProcess;
                }

                public void setAgoCpuUsagePercentageProcess(List<List<ANRItem>> agoCpuUsagePercentageProcess) {
                    mAgoCpuUsagePercentageProcess = agoCpuUsagePercentageProcess;
                }

                public List<ANRItem> getTotalItems() {
                    return mTotalItems;
                }

            }
        }

        /**
         * Record traces.txt's thread state and function stack.
         */
        public static class Traces {
            private String mPid;
            private List<ThreadTraces> mThreadTraces;

            public Traces() {
                mThreadTraces = new ArrayList<ThreadTraces>();
            }

            public String getPid() {
                return mPid;
            }

            public void setPid(String pid) {
                mPid = pid;
            }

            public List<ThreadTraces> getThreadTraces() {
                return mThreadTraces;
            }

            public static class ThreadTraces {
                private String mThreadName;
                private String mThreadId;
                private String mSysTid;
                private String mStatus;
                private Integer mKernelStartLineNo;
                private Integer mNativeStartLineNo;
                private Integer mJavaStartLineNo;

                public String getThreadName() {
                    return mThreadName;
                }

                public void setThreadName(String threadName) {
                    mThreadName = threadName;
                }

                public String getThreadId() {
                    return mThreadId;
                }

                public void setThreadId(String threadId) {
                    mThreadId = threadId;
                }

                public String getSysTid() {
                    return mSysTid;
                }

                public void setSysTid(String sysTid) {
                    mSysTid = sysTid;
                }

                public String getStatus() {
                    return mStatus;
                }

                public void setStatus(String status) {
                    mStatus = status;
                }

                public Integer getKernelStartLineNo() {
                    return mKernelStartLineNo;
                }

                public void setKernelStartLineNo(Integer kernelStartLineNo) {
                    mKernelStartLineNo = kernelStartLineNo;
                }

                public Integer getNativeStartLineNo() {
                    return mNativeStartLineNo;
                }

                public void setNativeStartLineNo(Integer nativeStartLineNo) {
                    this.mNativeStartLineNo = mNativeStartLineNo;
                }

                public Integer getJavaStartLineNo() {
                    return mJavaStartLineNo;
                }

                public void setJavaStartLineNo(Integer javaStartLineNo) {
                    mJavaStartLineNo = javaStartLineNo;
                }
            }
        }

        public static class ReasonDetail {
            private List<Integer> mLogcatLineNoList;
            private List<Integer> mTracesLineNoList;
            private String mDesc;

            public ReasonDetail() {
                mLogcatLineNoList = new ArrayList<Integer>();
                mTracesLineNoList = new ArrayList<Integer>();
            }

            public List<Integer> getTracesLineNoList() {
                return mTracesLineNoList;
            }


            public List<Integer> getLogcatLineNoList() {
                return mLogcatLineNoList;
            }

            public String getDesc() {
                return mDesc;
            }

            public void setDesc(String desc) {
                mDesc = desc;
            }

            public void relocate(int logcatStartLine, int traceStartLine) {
                if (logcatStartLine >= 0) {
                    int len = mLogcatLineNoList.size();
                    for (int i = 0 ; i < len ; i++) {
                        Integer oldLineNo = mLogcatLineNoList.get(i);
                        mLogcatLineNoList.set(i, oldLineNo + logcatStartLine);
                    }
                }

                if (traceStartLine >= 0) {
                    int len = mTracesLineNoList.size();
                    for (int i = 0 ; i < len ; i++) {
                        Integer oldLineNo = mTracesLineNoList.get(i);
                        mTracesLineNoList.set(i, oldLineNo + traceStartLine);
                    }
                }
            }
        }
    }

    public static class ANRItem {
        private String mThreadId;
        private String mThreadName;

        private double mCpuPercentTotal;
        private double mCpuPercentUser;
        private double mCpuPercentKernel;
        private double mCpuPercentIOWait;
        private double mCpuPercentSoftIrq;

        private List<ANRItem> mThreadsCpuFrequencies;

        public ANRItem() {
            mThreadsCpuFrequencies = new ArrayList<ANRItem>();
        }

        public String getThreadId() {
            return mThreadId;
        }

        public void setThreadId(String threadId) {
            mThreadId = threadId;
        }

        public String getThreadName() {
            return mThreadName;
        }

        public void setThreadName(String threadName) {
            mThreadName = threadName;
        }

        public double getCpuPercentTotal() {
            return mCpuPercentTotal;
        }

        public void setCpuPercentTotal(double cpuPercentTotal) {
            mCpuPercentTotal = cpuPercentTotal;
        }

        public double getCpuPercentUser() {
            return mCpuPercentUser;
        }

        public void setCpuPercentUser(double cpuPercentUser) {
            mCpuPercentUser = cpuPercentUser;
        }

        public double getCpuPercentKernel() {
            return mCpuPercentKernel;
        }

        public void setCpuPercentKernel(double cpuPercentKernel) {
            mCpuPercentKernel = cpuPercentKernel;
        }

        public double getCpuPercentIOWait() {
            return mCpuPercentIOWait;
        }

        public void setCpuPercentIOWait(double cpuPercentIOWait) {
            mCpuPercentIOWait = cpuPercentIOWait;
        }

        public List<ANRItem> getThreadsCpuFrequencies() {
            return mThreadsCpuFrequencies;
        }

        public double getCpuPercentSoftIrq() {
            return mCpuPercentSoftIrq;
        }

        public void setCpuPercentSoftIrq(double cpuPercentSoftIrq) {
            mCpuPercentSoftIrq = cpuPercentSoftIrq;
        }
    }

    public ANRParsedResult getANRParsedResult() {
        return mANRParsedResult;
    }
}
