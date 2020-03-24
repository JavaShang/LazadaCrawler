package com.hj.crawler.utils;

import lombok.extern.log4j.Log4j2;

import java.io.File;

@Log4j2
public class StringUtils {

    public static String getNameWithoutExtension(String file) {
        try {
            String fileName = new File(file).getName();
            int dotIndex = fileName.lastIndexOf('.');
            return (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
        } catch (Exception ex) {
            log.error(ex);
            return null;
        }
    }

    public static String getExtension(String file, boolean withDot) {
        try {
            String fileName = new File(file).getName();
            int dotIndex = fileName.lastIndexOf('.');
            return (dotIndex == -1) ? "" : fileName.substring(withDot ? dotIndex : dotIndex + 1);
        } catch (Exception ex) {
            log.error(ex);
            return null;
        }
    }
}
