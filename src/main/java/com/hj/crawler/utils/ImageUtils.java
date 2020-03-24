package com.hj.crawler.utils;

import lombok.extern.log4j.Log4j2;
import net.coobird.thumbnailator.ThumbnailParameter;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import net.coobird.thumbnailator.name.Rename;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Log4j2
public class ImageUtils {

    public static void addCover(String originPath, String coverPath) {
        try {
            BufferedImage coverImage = ImageIO.read(new File(coverPath));

            Thumbnails.of(originPath)
                    .size(coverImage.getWidth(), coverImage.getHeight())
                    .watermark(Positions.CENTER, coverImage, 1)
                    .outputQuality(0.8f)
                    .toFiles(new Rename() {
                        @Override
                        public String apply(String name, ThumbnailParameter param) {
                            return appendSuffix(name, "_covered");
                        }
                    });
        } catch (IOException ex) {
            log.error(ex);
        }
    }
}
