package com.uc.phantom.bra.framework.warehouse.problems.knowledge_lib;

import com.uc.phantom.bra.framework.warehouse.problems.knowledge_lib.anr.ANRBase;

import java.io.File;
import java.util.List;

/**
 * Base Problem.
 * @Author Tam.
 * Modifications :
 * 2017/4/17 : Created.
 */
public abstract class BaseProblem {

    protected StringBuilder mProblemStringBlock;
    protected boolean mParsedFlag;

    protected BaseProblem(StringBuilder problemStringBlock) {
        mProblemStringBlock = problemStringBlock;
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

    public ANRBase.ANRParsedResult recognize(final List<String> anrLogcat, List<String> anrTraces) {
        return null;
    }

}
