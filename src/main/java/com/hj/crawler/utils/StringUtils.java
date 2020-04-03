package com.hj.crawler.utils;

import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static String getNameFromUrl(String url, boolean withExtension) {
        try {
            url = url.split("\\?")[0];
            int separatorIndex = url.lastIndexOf("/");
            String name = (separatorIndex == -1) ? url : url.substring(separatorIndex + 1);
            if (withExtension) {
                return name;
            } else {
                int dotIndex = name.lastIndexOf('.');
                return name.substring(0, dotIndex);
            }
        } catch (Exception ex) {
            log.error(ex);
            return null;
        }
    }

    public static String getExtensionFromUrl(String url, boolean withDot) {
        try {
            int dotIndex = url.lastIndexOf('.');
            return (dotIndex == -1) ? "" : url.substring(withDot ? dotIndex : dotIndex + 1);
        } catch (Exception ex) {
            log.error(ex);
            return null;
        }
    }

    public static String getLazadaItemId(String url) {
        try {
            url = url.split("\\?")[0];
            String regex = "-i\\d+-";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(url);
            if (matcher.find()) {
                return matcher.group(0).replaceAll("-", "")
                        .replace("i", "");
            }
        } catch (Exception ex) {
            log.error(ex);
        }
        return String.valueOf(System.currentTimeMillis());
    }
}
