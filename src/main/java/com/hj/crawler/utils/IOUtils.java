package com.hj.crawler.utils;

import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Log4j2
public class IOUtils {

    public static List<String> readLines(String path) {
        try {
            return Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            log.error(ex);
            return null;
        }
    }

    public static String readString(String path) {
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(path));
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            log.error(ex);
            return null;
        }
    }

    public static boolean saveToPath(String savePath, String fileName, byte[] bytes) {
        try {
            File dir = new File(savePath);
            if (!dir.exists() && !dir.mkdirs()) {
                return false;
            }

            File file = new File(savePath, fileName);
            if (!file.exists() && !file.createNewFile()) {
                return false;
            }

            Files.write(Paths.get(file.getAbsolutePath()), bytes);

            return true;
        } catch (Exception ex) {
            log.error(ex);
            return false;
        }
    }
}
