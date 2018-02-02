package com.uc.phantom.bra.framework.warehouse.problems.knowledge_lib.anr;

import com.uc.phantom.bra.framework.warehouse.problems.knowledge_lib.BaseProblem;

import java.util.List;

/**
 * ANR Problem Caused by Thread Deadlock, because each of them wanting to get the lock, which held by the other thread.
 * @Author Tam.
 * Modifications :
 * 2017/4/26 : Created.
 */
public class DeadLockANR extends BaseProblem {

    private final static String TAG = "DeadLockANR";

    public DeadLockANR(StringBuilder problemStringBlock) {
        super(problemStringBlock);
    }


    public BaseProblem recognize() {
        return null;
    }


}
