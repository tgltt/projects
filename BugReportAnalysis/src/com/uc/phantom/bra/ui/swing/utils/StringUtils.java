package com.uc.phantom.bra.ui.swing.utils;

/**
 * Created by tam on 2017-5-6.
 */
public class StringUtils {

    /**
     * 获得字符串的next函数值
     *
     * @param t
     *            字符串
     * @return next函数值
     */
    public static int[] next(char[] t) {
        int[] next = new int[t.length];
        next[0] = -1;
        int i = 0;
        int j = -1;
        while (i < t.length - 1) {
            if (j == -1 || t[i] == t[j]) {
                i++;
                j++;
                if (t[i] != t[j]) {
                    next[i] = j;
                } else {
                    next[i] = next[j];
                }
            } else {
                j = next[j];
            }
        }
        return next;
    }

    public static int kmp_indexOf(String target, String pattern, int fromIndex) {
        if (target == null || target.equals(""))
            return -1;

        if (pattern == null || pattern.equals(""))
            return -1;

        if (fromIndex < 0 || fromIndex >= target.length())
            return -1;

        String subStr = target.substring(fromIndex);

        return kmp_indexOf(subStr, pattern);
    }

    /**
     * KMP匹配字符串
     *
     * @param s
     *            主串
     * @param t
     *            模式串
     * @return 若匹配成功，返回下标，否则返回-1
     */
    public static int kmp_indexOf(String target, String pattern) {
        if (target == null || target.equals(""))
            return -1;

        if (pattern == null || pattern.equals(""))
            return -1;


        char[] s = target.toCharArray();
        char[] t = pattern.toCharArray();

        int[] next = next(t);
        int i = 0;
        int j = 0;
        while (i <= s.length - 1 && j <= t.length - 1) {
            if (j == -1 || s[i] == t[j]) {
                i++;
                j++;
            } else {
                j = next[j];
            }
        }
        if (j < t.length) {
            return -1;
        } else
            return i - t.length; // 返回模式串在主串中的头下标
    }
}
