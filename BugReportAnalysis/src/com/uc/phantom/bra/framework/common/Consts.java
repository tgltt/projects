package com.uc.phantom.bra.framework.common;

import java.text.SimpleDateFormat;

/**
 * Constants.
 * @Author Tam.
 * Modifications :
 * 2017/4/18 : Created.
 */
public class Consts {

    public final static String BUGREPORT_FILENAME = "BugReport.txt";

    public final static String BUGREPORT_CHAPTER_PREFIX = "------";

    // DUMPSYS MEMINFO BLOCK.
    public final static String BUGREPORT_CHAPTER_DUMPSYS_MEMINFO_START  = "------ DUMPSYS MEMINFO";
    public final static String BUGREPORT_CHAPTER_DUMPSYS_MEMINFO_END    = "was the duration of 'DUMPSYS MEMINFO' ------";
    public final static String BUGREPORT_CHAPTER_DUMPSYS_MEMINFO_FILTER = "** MEMINFO in";

    // DUMPSYS CPUINFO BLOCK.
    public final static String BUGREPORT_CHAPTER_DUMPSYS_CPUINFO_START = "------ DUMPSYS CPUINFO";
    public final static String BUGREPORT_CHAPTER_DUMPSYS_CPUINFO_END   = "was the duration of 'DUMPSYS CPUINFO' ------";
    // PROCESSES AND THREADS BLOCK.
    public final static String BUGREPORT_CHAPTER_PROCESSES_AND_THREADS_START = "------ PROCESSES AND THREADS";
    public final static String BUGREPORT_CHAPTER_PROCESSES_AND_THREADS_END   = "was the duration of 'PROCESSES AND THREADS' ------";
    // UPTIME BLOCK.
    public final static String BUGREPORT_CHAPTER_UPTIME_START         = "------ UPTIME (uptime) ------";
    public final static String BUGREPORT_CHAPTER_UPTIME_END           = "was the duration of 'UPTIME' ------";
    // MEMORY INFO BLOCK.
    public final static String BUGREPORT_CHAPTER_MEMORY_INFO_START = "------ MEMORY INFO";
    public final static String BUGREPORT_CHAPTER_MEMORY_INFO_END   = "was the duration of 'MEMORY INFO' ------";
    // CPU INFO BLOCK.
    public final static String BUGREPORT_CHAPTER_CPU_INFO_START = "------ CPU INFO";
    public final static String BUGREPORT_CHAPTER_CPU_INFO_END   = "was the duration of 'CPU INFO' ------";
    // SMAPS OF ALL PROCESSES BLOCK.
    public final static String BUGREPORT_CHAPTER_SMAPS_OF_ALL_PROCESSES_START = "------ SMAPS OF ALL PROCESSES";
    public final static String BUGREPORT_CHAPTER_SMAPS_OF_ALL_PROCESSES_END = "";
    public final static String BUGREPORT_CHAPTER_SHOW_MAP_START = "------ SHOW MAP";
    public final static String BUGREPORT_CHAPTER_SHOW_MAP_END   = "was the duration of 'SHOW MAP";
    // BLOCKED PROCESS WAIT-CHANNELS BLOCK.
    public final static String BUGREPORT_CHAPTER_BLOCKED_PROCESS_WAIT_CHANNELS_START = "------ BLOCKED PROCESS WAIT-CHANNELS ------";
    public final static String BUGREPORT_CHAPTER_BLOCKED_PROCESS_WAIT_CHANNELS_END = "";
    // PROCESS TIMES BLOCK.
    public final static String BUGREPORT_CHAPTER_PROCESS_TIMES_START = "------ PROCESS TIMES";
    public final static String BUGREPORT_CHAPTER_PROCESS_TIMES_END   = "";
    // VM TRACES JUST NOW BLOCK.
    public final static String BUGREPORT_CHAPTER_VM_TRACES_JUST_NOW_START = "------ VM TRACES JUST NOW";
    public final static String BUGREPORT_CHAPTER_VM_TRACES_JUST_NOW_END = "was the duration of 'VM TRACES JUST NOW' ------";
    public final static String BUGREPORT_CHAPTER_VM_TRACES_JUST_NOW_APP_START = "----- pid";
    public final static String BUGREPORT_CHAPTER_VM_TRACES_JUST_NOW_APP_END   = "----- end";
    // CHECKIN USAGESTATS BLOCK.
    public final static String BUGREPORT_CHAPTER_CHECKIN_USAGESTATS_START = "------ CHECKIN USAGESTATS";
    public final static String BUGREPORT_CHAPTER_CHECKIN_USAGESTATS_END = "was the duration of 'CHECKIN USAGESTATS' ------";
    // CHECKIN PACKAGE BLOCK.
    public final static String BUGREPORT_CHAPTER_CHECKIN_PACKAGE_START = "------ CHECKIN PACKAGE";
    public final static String BUGREPORT_CHAPTER_CHECKIN_PACKAGE_END   = "was the duration of 'CHECKIN PACKAGE' ------";
    // APP ACTIVITIES BLOCK.
    public final static String BUGREPORT_CHAPTER_APP_ACTIVITIES_START = "------ APP ACTIVITIES";
    public final static String BUGREPORT_CHAPTER_APP_ACTIVITIES_END = "was the duration of 'APP ACTIVITIES' ------";
    public final static String BUGREPORT_CHAPTER_APP_ACTIVITIES_FILTER = "TASK ";

    // APP SERVICES BLOCK.
    public final static String BUGREPORT_CHAPTER_APP_SERVICES_START = "------ APP SERVICES";
    public final static String BUGREPORT_CHAPTER_APP_SERVICES_END   = "was the duration of 'APP SERVICES' ------";
    public final static String BUGREPORT_CHAPTER_APP_SERVICES_FILTER = "SERVICE ";
    // APP PROVIDERS BLOCK.
    public final static String BUGREPORT_CHAPTER_APP_PROVIDERS_START = "activity provider all) ------";
    public final static String BUGREPORT_CHAPTER_APP_PROVIDERS_END   = "was the duration of 'APP SERVICES' ------";
    public final static String BUGREPORT_CHAPTER_APP_PROVIDERS_FILTER = "PROVIDER ";

    public final static String BUGREPORT_CHAPTER_LOGCAT_START = "------ SYSTEM LOG";
    public final static String BUGREPORT_CHAPTER_LOGCAT_END = "was the duration of 'LOG STATISTICS'";

    public final static String PROBLEM_EXCEPTION_START = "Exception";
    public final static String PROBLEM_ERROR_START = "Error";
    public final static String PROBLEM_FATAL_START = " Fatal ";
    public final static String PROBLEM_BUILD_FINGERPRINT_START = "Build fingerprint:";
    public final static String PROBLEM_DEXOPT = "DexOpt:";

    public final static String DEFAULT_FILE_ENCODE = "UTF-8";

    public final static String ANDROID_DETAIL_LOG_DIR = "android_logs";
}
