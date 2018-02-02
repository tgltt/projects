package com.uc.phantom.bra.framework.scan_parse.parser;

import com.uc.phantom.bra.framework.utils.FileUtils;
import com.uc.phantom.bra.framework.utils.Logger;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Statistic Information of Parsing BugReport.
 * @Author Tam.
 * Modifications :
 * 2017/4/17 : Created.
 */
public class StatisticInfo {
    public static class ChapterMapping {
        public final static int CHAPTER_DUMPSYS_MEMINFO                  = 1;
        public final static int CHAPTER_DUMPSYS_CPUINFO                  = 2;
        public final static int CHAPTER_UPTIME                             = 3;
        public final static int CHAPTER_MEMORY_INFO                       = 4;
        public final static int CHAPTER_CPUINFO                            = 5;
        public final static int CHAPTER_PROCESSES_AND_THREADS            = 6;
        public final static int CHAPTER_SMAPS_OF_ALL_PROCESSES           = 7;
        public final static int CHAPTER_BLOCKED_PROCESSES_WAIT_CHANNELS = 8;
        public final static int CHAPTER_PROCESS_TIME                       = 9;
        public final static int CHAPTER_VM_TRACESJUST_NOW                 = 10;
        public final static int CHAPTER_CHECKIN_USAGESTATS                = 11;
        public final static int CHAPTER_CHECKIN_PACKAGE                   = 12;
        public final static int CHAPTER_APP_ACTIVITIES                    = 13;
        public final static int CHAPTER_APP_SERVICES                       = 14;
        public final static int CHAPTER_APP_PROVIDERS                      = 15;
        public final static int CHAPTER_LOGCAT                              = 16;
        public final static int CHAPTER_PROBLEMS                            = 17;
        public final static int CHAPTER_OTHERS                              = 18;
        public final static int CHAPTER_APPLOCAT                            = 19;
        public final static int CHAPTER_EVENTS                              = 20;
        public final static int CHAPTER_KERNEL_LOGS                        = 21;
        public final static int CHAPTER_TRACES                              = 22;

        private static Map<Integer, ChapterFilterLineAtInfo> mChapterStatisticInfo;
        public synchronized static Map<Integer, ChapterFilterLineAtInfo> getChapterStatisticInfo() {
            if (mChapterStatisticInfo == null) {
                mChapterStatisticInfo = new HashMap<Integer, ChapterFilterLineAtInfo>();
            }

            return mChapterStatisticInfo;
        }

        public synchronized static void release() {
            if (mChapterStatisticInfo != null) {
                mChapterStatisticInfo.clear();
                mChapterStatisticInfo = null;
            }
        }

        public static String getChapterTitle(int chapterId) {
            switch (chapterId) {
                case CHAPTER_DUMPSYS_MEMINFO:
                      return "DUMPSYS MEMINFO";
                case CHAPTER_DUMPSYS_CPUINFO:
                      return "DUMPSYS CPUINFO";
                case CHAPTER_UPTIME:
                      return "UPTIME";
                case CHAPTER_MEMORY_INFO:
                      return "MEMORY INFO";
                case CHAPTER_CPUINFO:
                      return "CPUINFO";
                case CHAPTER_PROCESSES_AND_THREADS:
                      return "PROCESSES AND THREADS";
                case CHAPTER_SMAPS_OF_ALL_PROCESSES:
                      return "SMAPS OF ALL_PROCESSES";
                case CHAPTER_BLOCKED_PROCESSES_WAIT_CHANNELS:
                      return "BLOCKED PROCESSES WAIT CHANNELS";
                case CHAPTER_PROCESS_TIME:
                      return "PROCESS TIME";
                case CHAPTER_VM_TRACESJUST_NOW:
                      return "VM TRACESJUST NOW";
                case CHAPTER_CHECKIN_USAGESTATS:
                      return "CHAPTER CHECKIN USAGESTATS";
                case CHAPTER_CHECKIN_PACKAGE:
                      return "CHAPTER CHECKIN PACKAGE";
                case CHAPTER_APP_ACTIVITIES:
                      return "CHAPTER APP ACTIVITIES";
                case CHAPTER_APP_SERVICES:
                      return "CHAPTER APP SERVICES";
                case CHAPTER_APP_PROVIDERS:
                      return "CHAPTER APP PROVIDERS";
                case CHAPTER_LOGCAT:
                      return "LOGCAT";
                case CHAPTER_PROBLEMS:
                      return "PROBLEMS";
                default:
                      return "OTHERS";
            }
        }

        /**
         * Statistic Scope of Each BugReport Chapter.
         */
        public static class ChapterFilterLineAtInfo {
            /**
             * Sparse to record filter lines.
             */
            public final static int FLAG_FEATURE_SPARSE = 0;
            /**
             * Block to record filter lines.
             */
            public final static int FLAG_FEATURE_BLOCK = 1;
            /**
             * Sparse and block to record filter lines.
             */
            public final static int FLAG_FEATURE_SPARSE_AND_BLOCK = 2;
            /**
             * Start line of the chapter in source log file.
             */
            private long mChapterStartLine;
            /**
             * End line of the chapter in source log file.
             */
            private long mChapterEndLine;
            /**
             * Start line of the chapter filter content in source log file.
             */
            private long mChapterFilterStartLine;
            /**
             * End line of the chapter filter content in source log file.
             */
            private long mChapterFilterEndLine;

            private List<Long> mExtraIncludeLinesList;

            private int mFeature;

            public ChapterFilterLineAtInfo() {
                mExtraIncludeLinesList = new ArrayList<Long>();
            }

            public long getChapterStartLine() {
                return mChapterStartLine;
            }

            public void setChapterStartLine(long chapterStartLine) {
                mChapterStartLine = chapterStartLine;
            }

            public long getChapterEndLine() {
                return mChapterEndLine;
            }

            public void setChapterEndLine(long chapterEndLine) {
                mChapterEndLine = chapterEndLine;
            }

            public long getChapterFilterStartLine() {
                return mChapterFilterStartLine;
            }

            public void setChapterFilterStartLine(long chapterFilterStartLine) {
                mChapterFilterStartLine = chapterFilterStartLine;
            }

            public long getChapterFilterEndLine() {
                return mChapterFilterEndLine;
            }

            public void setChapterFilterEndLine(long chapterFilterEndLine) {
                mChapterFilterEndLine = chapterFilterEndLine;
            }

            public List<Long> getExtraIncludeLinesList() {
                return mExtraIncludeLinesList;
            }

            public int getFeature() {
                return mFeature;
            }

            public void setFeature(int feature) {
                mFeature = feature;
            }
        }
    }

    /**
     * Global Statistic Class 4 Parsing BugReport.
     */
    public static class CacheLinesStatistic {
        private final static String TAG = "CacheLinesStatistic";

        /**
         * Line No. of first line of cached lines in source log file.
         */
        private long mCachedStartLine;
        /**
         * Line No. of last line of cached lines in source log file.
         */
        private long mCachedEndLine;
        /**
         * Total parsed line by now.
         */
        private long mTotalLine;
        /**
         * Total memory size of cached lines.
         */
        private long mCachedAllLinesSize;
        /**
         * Current Line.
         */
        private long mCurrentLine;
        /**
         * Mapping of each line to its memory size.
         */
        private List<Integer> mLineSizeList;
        /**
         * Store read lines.
         */
        private List<String> mLinesList;

        public CacheLinesStatistic() {
            mCachedStartLine = 0;
            mCachedEndLine = -1;
            mCurrentLine = -1;
            mTotalLine = 0;
            mCachedAllLinesSize = 0;
            mLineSizeList = new ArrayList<Integer>();
            mLinesList = new ArrayList<String>();
        }

        public long getCachedStartLine() {
            return mCachedStartLine;
        }


        public long getCachedEndLine() {
            return mCachedEndLine;
        }


        public long getTotalLine() {
            return mTotalLine;
        }


        public long getCachedAllLinesSize() {
            return mCachedAllLinesSize;
        }


        public List<Integer> getLineSizeList() {
            return mLineSizeList;
        }

        public long getCurrentLine() {
            return mCurrentLine;
        }

        public List<String> getLinesList() {
            return mLinesList;
        }

        public synchronized void addNewLine(String line, String encode) {
            if (line == null)
                return;

//            int lineSize;
//            try {
//                lineSize = line.getBytes(encode).length;
//            } catch (UnsupportedEncodingException useex) {
//                Logger.getLogger(null).log(TAG,  "Get String(" + line + ") encode failed.", useex);
//                Logger.getLogger(null).log(TAG,  "Using default built-in encode to get String(" + line + ") bytes.", useex);
//
//                lineSize = line.getBytes().length;
//            }

            mCurrentLine++;
            mCachedEndLine++;
            mTotalLine++;

            mLinesList.add(line);
//            mLineSizeList.add(lineSize);
//            mCachedAllLinesSize += lineSize;
        }

        public synchronized void rollbackNewLine() {
            int newLineIndex = (int)(mTotalLine - 1);
            int newLineSize  = mLineSizeList.get(newLineIndex);

            mLinesList.remove(newLineIndex);
            mLineSizeList.remove(newLineIndex);

            mCachedAllLinesSize -= newLineSize;

            mCurrentLine--;
            mCachedEndLine--;
            mTotalLine--;
        }

    }

    /**
     * Store Plain Problem Text.
     */
    public static class PlainProblems {
        private List<Problem> mExceptionList;
        private List<Problem> mFatalList;
        private List<Problem> mBuildFingerprintList;
        private List<Problem> mDexOptList;

        public PlainProblems() {
            mExceptionList = new ArrayList<Problem>();
            mFatalList = new ArrayList<Problem>();
            mBuildFingerprintList = new ArrayList<Problem>();
            mDexOptList = new ArrayList<Problem>();
        }

        public List<Problem> getExceptionList() {
            return mExceptionList;
        }

        public void setExceptionList(List<Problem> exceptionList) {
            mExceptionList = exceptionList;
        }

        public List<Problem> getFatalList() {
            return mFatalList;
        }

        public void setFatalList(List<Problem> fatalList) {
            mFatalList = fatalList;
        }

        public List<Problem> getBuildFingerprintList() {
            return mBuildFingerprintList;
        }

        public void setBuildFingerprintList(List<Problem> buildFingerprintList) {
            mBuildFingerprintList = buildFingerprintList;
        }

        public List<Problem> getDexOptList() {
            return mDexOptList;
        }

        public void setDexOptList(List<Problem> dexOptList) {
            mDexOptList = dexOptList;
        }

        public void release() {
            mBuildFingerprintList.clear();
            mDexOptList.clear();
            mExceptionList.clear();
            mFatalList.clear();
        }

        public static class Problem {
            private ChapterMapping.ChapterFilterLineAtInfo mChapterFilterLineAtInfo;

            public Problem() {
                mChapterFilterLineAtInfo = new ChapterMapping.ChapterFilterLineAtInfo();
            }

            public ChapterMapping.ChapterFilterLineAtInfo getChapterFilterLineAtInfo() {
                return mChapterFilterLineAtInfo;
            }

            public void setChapterFilterLineAtInfo(ChapterMapping.ChapterFilterLineAtInfo chapterFilterLineAtInfo) {
                mChapterFilterLineAtInfo = chapterFilterLineAtInfo;
            }
        }

    }

    public static class FilterInfo {
        private  List<Long> mFilterInfoList;

        public FilterInfo() {
            mFilterInfoList = new ArrayList<Long>();
        }

        public List<Long> getFilterInfoList() {
            return mFilterInfoList;
        }

        public void setFilterInfoList(List<Long> filterInfoList) {
            mFilterInfoList = filterInfoList;
        }
    }
}
