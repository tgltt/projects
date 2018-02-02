package com.uc.phantom.bra.framework.utils;

import java.io.File;
import java.util.List;

/**
 * Created by tam on 2017-5-6.
 */
public class FileWrapper {
    private File mFile;
    private final List<String> mFileContents;
    private int mCurLineNo;
    private int mFileLineCount;

    public FileWrapper(File file, List<String> contents) {
        mFile = file;
        mFileContents = contents;
        mCurLineNo = 0;
        mFileLineCount = contents.size();
    }

    public String readLine() {
        if (mCurLineNo < mFileLineCount) {
            return mFileContents.get(mCurLineNo++);
        }

        return null;
    }

    public void reset() {
        mCurLineNo = 0;

    }

    public void release() {
        mCurLineNo = -1;
        mFileLineCount = -1;
        mFileContents.clear();
    }
}
