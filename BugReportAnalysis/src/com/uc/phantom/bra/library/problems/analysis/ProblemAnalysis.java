package com.uc.phantom.bra.library.problems.analysis;

import com.uc.phantom.bra.library.problems.data.Problem;

import java.io.File;
import java.util.List;

/**
 * Problem Analysis.
 * @Author Tam.
 * Modifications :
 * 2017/4/25 : Created.
 */
public class ProblemAnalysis {

    private static ProblemAnalysis sProblemAnalysis;
    public synchronized static ProblemAnalysis getInstance() {
        if (sProblemAnalysis == null) {
            sProblemAnalysis = new ProblemAnalysis();
        }

        return sProblemAnalysis;
    }

    public Problem analyze(Problem problem) {
        return null;
    }

}
