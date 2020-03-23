package com.hj.crawler.utils;

import java.io.File;

public class StringUtils {

    public static String getNameWithoutExtension(String file) {
        try {
            String fileName = new File(file).getName();
            int dotIndex = fileName.lastIndexOf('.');
            return (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static String getExtension(String file) {
        try {
            String fileName = new File(file).getName();
            int dotIndex = fileName.lastIndexOf('.');
            return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
