package com.uc.phantom.bra.framework.utils;

import com.uc.phantom.bra.ui.swing.utils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2017/6/19.
 */
public class DiffFiles {

    /**
     * Find leak items in dest file from src file.
     * @param srcFile
     * @param destFile
     * @return
     */
    public static List<Entry> scanDiff(File srcFile, File destFile) {
        if (srcFile == null || !srcFile.exists())
            return null;
        if (destFile == null || !destFile.exists())
            return null;

        List<Entry> listDiff = new ArrayList<Entry>();
        List<String> fileSrcContentList  = null;
        List<String> fileDestContentList = null;

        try {
            fileSrcContentList = org.apache.commons.io.FileUtils.readLines(srcFile, "utf-8");
            fileDestContentList = org.apache.commons.io.FileUtils.readLines(destFile, "utf-8");


            Pattern patternKey = Pattern.compile("name=\"[a-zA-Z_]*\"");
            Pattern patternValue = Pattern.compile(">[\\d\\D]*<");
            for (String line : fileSrcContentList) {
                Matcher matcher = patternKey.matcher(line);
                if (matcher == null || !matcher.find())
                    continue;

                String key = matcher.group();
                boolean hasAppearance = false;
                for (String destLine : fileDestContentList) {
                    if (StringUtils.kmp_indexOf(destLine, key) > -1) {
                        hasAppearance = true;
                        break;
                    }
                }

                if (!hasAppearance) {
                    Entry entry = new Entry();
                    entry.setKey(key);

                    matcher = patternValue.matcher(line);
                    if (matcher != null && matcher.find()) {
                        entry.setValue(matcher.group());
                    }

                    listDiff.add(entry);
                }
            }
        } catch (IOException ioex) {
            ioex.printStackTrace();
        }

        return listDiff;
    }

    public static class Entry {
        private String mKey;
        private String mValue;

        public String getKey() {
            return mKey;
        }

        public void setKey(String key) {
            mKey = key;
        }

        public String getValue() {
            return mValue;
        }

        public void setValue(String value) {
            mValue = value;
        }
    }

    public static void main(String[] args) {
        String filename1 = "D:\\workspace\\svn\\tgl_url_20170614\\BrowserShell\\platform\\android\\resources\\strings\\zh-cn\\strings.xml";
        //String filename2 = "D:\\workspace\\svn\\tgl_url_20170614\\BrowserShell\\platform\\android\\resources\\strings\\en-us\\strings.xml";
        //String filename2 = "D:\\workspace\\svn\\tgl_url_20170614\\BrowserShell\\platform\\android\\resources\\strings\\zh-hk\\strings.xml";
        String filename2 = "D:\\workspace\\svn\\tgl_url_20170614\\BrowserShell\\platform\\android\\resources\\strings\\zh-tw\\strings.xml";

        File file1 = new File(filename1);
        File file2 = new File(filename2);
        List<Entry> listDiff = scanDiff(file1, file2);
        if (listDiff == null || listDiff.isEmpty()) {
            System.out.println("No match!!");
            return;
        }

        int i = 0;
        for (Entry entry : listDiff) {
            System.out.println(String.format("%d、%s, %s", i++, entry.getKey(), entry.getValue()));
        }

    }
}
