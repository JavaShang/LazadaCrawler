package com.hj.crawler.utils;

import lombok.extern.log4j.Log4j2;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
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

    public static boolean downloadImage(String savePath, String url) {
        try {
            BufferedImage bi = ImageIO.read(new URL(url));
            String ext = StringUtils.getExtensionFromUrl(url, false);
            File dir = new File(savePath);
            if ((!dir.exists() || !dir.isDirectory()) && !dir.mkdirs()) {
                return false;
            }
            String path = savePath + File.separator + StringUtils.getNameFromUrl(url, true);
            return ImageIO.write(bi, ext == null ? "jpg" : ext, new File(path));
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
