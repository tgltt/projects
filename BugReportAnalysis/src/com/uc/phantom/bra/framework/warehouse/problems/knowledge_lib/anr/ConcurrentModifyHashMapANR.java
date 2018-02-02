package com.uc.phantom.bra.framework.warehouse.problems.knowledge_lib.anr;

import com.uc.phantom.bra.framework.warehouse.problems.knowledge_lib.BaseProblem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ANR Problem Caused by Concurrent modify HashMap.
 * @Author Tam.
 * Modifications :
 * 2017/4/26 : Created.
 */
public class ConcurrentModifyHashMapANR extends ANRBase {

    private final static String TAG = "ConcurrentModifyHashMapANR";

    private List<String> mLogcat;
    private List<String> mTraces;

    public ConcurrentModifyHashMapANR(StringBuilder problemStringBlock) {
        super(problemStringBlock);
    }

    public ANRParsedResult recognize(final List<String> anrLogcat, final List<String> anrTraces) {
        mLogcat = anrLogcat;
        mTraces = anrTraces;

        return super.recognize(anrLogcat, anrTraces);
    }

    public ANRBase.ANRDiagnoseResult diagnose(ANRBase.ANRParsedResult anrParsedResult) {
        if (anrParsedResult == null)
            return null;

        List<ANRParsedResult.ANRAgoLater> agoLaters = anrParsedResult.getANRAgoLaterList();
        if (agoLaters == null || agoLaters.size() <= 0) // No enough information, return the parsed result as diagnose result.
            return new ANRDiagnoseResult(anrParsedResult);
        // Get the first item to analyze.
        ANRParsedResult.ANRAgoLater anrAgoLater = agoLaters.get(0);
        List<ANRItem> agoTopCpuProcessList   = new ArrayList<ANRItem>();
        List<ANRItem> laterTopCpuProcessList = new ArrayList<ANRItem>();

        analyzeAgoLaterTopCpuProcess(anrAgoLater, agoTopCpuProcessList, laterTopCpuProcessList);

        // Diagnosing
        ANRBase.ANRDiagnoseResult diagnoseResult = new ANRBase.ANRDiagnoseResult(anrParsedResult);
        // Diagnose ago part.
        if (agoTopCpuProcessList.size() >= 1) {
            ANRItem  agoTopCpuProcess = agoTopCpuProcessList.get(0);
            List<ANRItem> agoAnrItems = agoTopCpuProcess.getThreadsCpuFrequencies();
            if (agoAnrItems != null && agoAnrItems.size() >= 2) {
                // Get the top 2 threads to check.
                ANRItem anrItem1 = agoAnrItems.get(0);
                ANRItem anrItem2 = agoAnrItems.get(1);

                if (diagnoseKernel(anrItem1, anrItem2, diagnoseResult) == DIAGNOSE_TRACK_OK) {
                    return diagnoseResult;
                }
            }
        }

        // Diagnose later part.
        if (laterTopCpuProcessList.size() >= 1) {
            ANRItem  laterTopCpuProcess = laterTopCpuProcessList.get(0);
            diagnoseResult.setMostExaustTimeItem(laterTopCpuProcess);

            List<ANRItem> laterAnrItems = laterTopCpuProcess.getThreadsCpuFrequencies();
            if (laterAnrItems != null && laterAnrItems.size() >= 2) {
                // Get the top 2 threads to check.
                ANRItem anrItem1 = laterAnrItems.get(0);
                ANRItem anrItem2 = laterAnrItems.get(1);

                if (diagnoseKernel(anrItem1, anrItem2, diagnoseResult) == DIAGNOSE_TRACK_OK) {
                    return diagnoseResult;
                }
            }
        }

        return super.diagnose(anrParsedResult);
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

    private int diagnoseKernel(ANRItem anrItem1, ANRItem anrItem2, ANRBase.ANRDiagnoseResult diagnoseResult) {
        ANRBase.ANRParsedResult anrParsedResult = diagnoseResult.getANRParsedResult();

        if (anrItem1.getCpuPercentTotal() >= 60.0 && anrItem2.getCpuPercentTotal() >= 60.0) {
            int anrPlainLen = mLogcat.size();
            String line;
            boolean anrItem1Found = false, anrItem2Found = false;
            for (int i = 0 ; i < anrPlainLen ; i++) {
                line = mLogcat.get(i);
                if (line.contains(anrItem1.getThreadId() + "/" + anrItem1.getThreadName())) {
                    anrParsedResult.getReasonDetail().getLogcatLineNoList().add(i);
                    anrItem1Found = true;
                }

                if (line.contains(anrItem2.getThreadId() + "/" + anrItem2.getThreadName())) {
                    anrParsedResult.getReasonDetail().getLogcatLineNoList().add(i);
                    anrItem2Found = true;
                }

                if (anrItem1Found && anrItem2Found) {
                    break;
                }
            }

            List<ANRItem> listSuspect = new ArrayList<ANRItem>();

            listSuspect.add(anrItem1);
            listSuspect.add(anrItem2);

            int trackResult = trackTraces(mTraces, diagnoseResult, listSuspect);
            if (trackResult == DIAGNOSE_TRACK_TRACES_OK) {
                ANRParsedResult.Traces traces = diagnoseResult.getANRParsedResult().getTraces();
                final List<ANRParsedResult.Traces.ThreadTraces> listThreadTraces = traces.getThreadTraces();
                for (ANRParsedResult.Traces.ThreadTraces threadTrace : listThreadTraces) {

                    if (threadTrace.getThreadName().equals("main")) {
                        diagnoseResult.setMainThreadStatus(threadTrace);
                        break;
                    }
                }

                return DIAGNOSE_TRACK_OK;
            }
        }

        return DIAGNOSE_TRACK_FAILED;
    }

    private static int trackTraces(final List<String> anrTraces, ANRBase.ANRDiagnoseResult diagnoseResult, final List<ANRItem> suspectItems) {
        ANRBase.ANRParsedResult anrParsedResult = diagnoseResult.getANRParsedResult();

        String anrPid = diagnoseResult.getANRParsedResult().getPid();
        if (anrPid == null || anrPid.equals(""))
            return PARSE_RESULT_TRACES_MAYNOT_CORRECT;

        Map<String, Boolean> DealStatusMap = new HashMap<String, Boolean>();

        int i;
        int len = anrTraces.size();
        String line;
        for (ANRItem anrItem : suspectItems) {
            for (i = 0 ; i < len ; i++) {
                line = anrTraces.get(i);
                if (line.contains("sysTid=" + anrItem.getThreadId())) {
                    anrParsedResult.getReasonDetail().getTracesLineNoList().add(i);
                    for (i++ ; i < len ; i++) {
                        line = anrTraces.get(i);
                        if (!line.contains(" at ")) {
                            continue;
                        }

                        if (line.contains("HashMap") && (line.contains(".remove") || line.contains(".put"))) {
                            if (!DealStatusMap.containsKey(anrItem.getThreadId())) {
                                anrParsedResult.getReasonDetail().getTracesLineNoList().add(i);
                                DealStatusMap.put(anrItem.getThreadId(), true);
                                break;
                            }
                        }

                        if (line.trim().length() <= 0)
                            break;
                    }
                }
            }
        }

        if (DealStatusMap.size() < 2) {
            return DIAGNOSE_TRACK_TRACES_FAILED;
        }

        anrParsedResult.getReasonDetail().setDesc("Two or more threads concurrent modify HashMap.\nRecomend that you uses HashMap thread-safe mode.");
        diagnoseResult.getDiagnoseResult().add("Two or more threads concurrent modify HashMap.\nRecomend that you uses HashMap thread-safe mode.");
        anrParsedResult.setResultFlag(PARSE_RESULT_DETECT_REASON);


        return DIAGNOSE_TRACK_TRACES_OK;
    }

}
