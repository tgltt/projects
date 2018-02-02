package com.uc.phantom.bra.framework.warehouse.problems;

import java.io.File;

/**
 * Problem Knowledge Library.
 * @Author Tam.
 * Modifications :
 * 2017/4/17 : Created.
 */
public class ProblemKnowledgeLibrary {

    public static boolean parseBugReport(File bugReportFile, int flags) {
        if (bugReportFile == null || !bugReportFile.exists())
            return false;


        return false;
    }

    public static boolean parseBugReport(String bugReportPath, int flags) {
        if (bugReportPath == null || bugReportPath.trim().equals(""))
            return false;

        return parseBugReport(new File(bugReportPath), flags);
    }
}
