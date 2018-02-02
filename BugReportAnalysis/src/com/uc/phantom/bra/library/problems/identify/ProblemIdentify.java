package com.uc.phantom.bra.library.problems.identify;

import com.uc.phantom.bra.library.problems.data.Problem;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;

/**
 * Problem Identifier.
 * @Author Tam.
 * Modifications :
 * 2017/4/25 : Created.
 */
public class ProblemIdentify {

    private static ProblemIdentify sProblemIdentify;
    public synchronized static ProblemIdentify getInstance() {
        if (sProblemIdentify == null) {
            sProblemIdentify = new ProblemIdentify();
        }

        return sProblemIdentify;
    }

    public Problem identify(File logDir) {
        return null;
    }

    public Problem identify(File logcatFile, File eventFile, File kernelFile) {
        return null;
    }

    public Problem identify(List<String> logcat, List<String> events, List<String> kernels) {
        return null;
    }


}
