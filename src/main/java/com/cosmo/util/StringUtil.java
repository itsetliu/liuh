package com.cosmo.util;

/**
 * @author ZhaoZiQu
 * @version 2020/9/9
 */
public class StringUtil {
    //私有化构造器
    private StringUtil(){}
    public static boolean isEmpty(String str) {
        return str == null || "".equals(str.trim());
    }

}
