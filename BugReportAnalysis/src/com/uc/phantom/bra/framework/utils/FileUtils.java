package com.uc.phantom.bra.framework.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

/**
 * File Utils.
 * @Author Tam.
 * Modifications :
 * 2017/4/22 : Created.
 */
public class FileUtils {

    public static String getFileEncode(String fileName) throws Exception{
        return getFileEncode(new File(fileName));
    }

    /**
     * Get File Encoding.
     * @param file
     * @return
     * @throws Exception
     */
    public static String getFileEncode(File file) throws Exception{
        BufferedInputStream bin = new BufferedInputStream(new FileInputStream(file));
        int format = (bin.read() << 8) + bin.read();

        bin.close();

        String code = null;
        switch (format) {
            case 0xefbb:
                 code = "UTF-8";
                 break;
            case 0xfffe:
                 code = "Unicode";
                 break;
            case 0xfeff:
                 code = "UTF-16BE";
                 break;
            default:
                 code = "GBK";
        }

        return code;
    }
}
