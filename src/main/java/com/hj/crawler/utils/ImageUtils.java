package com.hj.crawler.utils;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageUtils {

    public static void addCover(String originPath, String coverPath) {
        try {
            String newPath = StringUtils.getNameWithoutExtension(originPath) + "_covered." + StringUtils.getExtension(originPath);
            BufferedImage originImage = ImageIO.read(new File(originPath));
            Thumbnails.of(originImage)
                    .size(originImage.getWidth(), originImage.getHeight())
                    .watermark(Positions.CENTER, ImageIO.read(new File(coverPath)), 1)
                    .outputQuality(1.0f)
                    .toFile(newPath);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
