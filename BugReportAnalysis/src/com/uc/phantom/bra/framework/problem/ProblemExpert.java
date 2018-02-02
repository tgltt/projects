package com.uc.phantom.bra.framework.problem;

import java.io.File;
import java.util.List;

/**
 * Problem Expert.
 * @Author Tam.
 * Modifications :
 * 2017/4/25 : Created.
 */
public class ProblemExpert {

    private ProblemCallback mProblemCallback;
    private static ProblemExpert sProblemExpert;
    public synchronized static ProblemExpert getInstance(ProblemCallback callback) {
        if (sProblemExpert == null) {
            sProblemExpert = new ProblemExpert(callback);
        }

        return sProblemExpert;
    }

    public ProblemExpert(ProblemCallback callback) {
        mProblemCallback = callback;
    }

    public Problem work(File logDir) {
        return null;
    }

    public Problem work(File logcatFile, File eventFile, File kernelFile) {
        return null;
    }

    public Problem work(List<String> logcat, List<String> events, List<String> kernels) {
        return null;
    }


    public interface ProblemCallback {
        boolean beforeDealWithProblem(Problem problem);
        boolean afterDealWithProblem(Problem problem, boolean resultFlag);
        boolean onDealWithProblem(Problem problem);
    }

}
