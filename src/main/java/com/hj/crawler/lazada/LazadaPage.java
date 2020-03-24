package com.hj.crawler.lazada;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hj.crawler.lazada.model.Lazada;
import com.hj.crawler.lazada.model.Sku;
import com.hj.crawler.network.DownloadInfo;
import com.hj.crawler.network.INetworkCallbackListener;
import com.hj.crawler.network.NetworkHelper;
import com.hj.crawler.page.BasePage;
import com.hj.crawler.utils.IOUtils;
import com.hj.crawler.utils.ImageUtils;
import com.hj.crawler.utils.StringUtils;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Log4j2
public class LazadaPage extends BasePage {

    private static final String RESULT_DIR = System.getProperty("user.dir") + "\\CrawlerResult";
    private static final String INFO_FILE_NAME = "info.json";
    private static final String GALLERY_DIR_NAME = "gallery";
    private static final String DETAIL_DIR_NAME = "detail";
    private static final String IMAGE_NAME_PREFIX = "img";
    private static final String COVER_IMAGE_PATH = System.getProperty("user.dir") + "\\cover.png";

    private static final int PRICE_INCREMENT = 30000;
    private static final float MIN_MULTIPLE = 1.1F;
    private static final float MAX_MULTIPLE = 1.3F;

    private Random mRandom;
    private Gson mGson;
    private List<DownloadInfo> mDownloadInfoList;

    WebElement mTitleElement;
    WebElement mPriceElement;
    List<WebElement> mSkuElementList;
    List<WebElement> mGalleryElementList;
    WebElement mSellerNameElement;
    WebElement mProductHighlightsElement;
    List<WebElement> mDetailImageTextElementList;
    WebElement mDetailContentElement;

    public LazadaPage(WebDriver webDriver) {
        super(webDriver);
        mRandom = new Random();
        mGson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create();

        mDownloadInfoList = new ArrayList<>();
    }

    public void fetchDataByUrls(List<String> urlList) {
        Preconditions.checkNotNull(urlList, "url list should not be null");
        Preconditions.checkElementIndex(0, urlList.size(), "url list should not be empty,");
        for (String url : urlList) {
            fetchDataByUrl(url);
        }
    }

    private void fetchDataByUrl(String url) {
        Preconditions.checkNotNull(url, "url should not be null");
        Preconditions.checkArgument(!url.isEmpty(), "url should not be empty");

        try {

            log.info("start get url :" + url);
            get(url);

            Lazada lazada = new Lazada();
            lazada.setOriginUrl(url);

            Thread.sleep(TimeUnit.SECONDS.toMillis(30));

            mTitleElement = get(By.className("pdp-product-title"));
            setTitle(lazada);

            mGalleryElementList = getList(By.className("pdp-mod-common-image"));
            getGalleryImage(lazada);

            mPriceElement = get(By.className("pdp-price_type_normal"));
            mSkuElementList = getList(By.className("sku-variable-name-text"));
            setSkuAndPrice(lazada);

            mSellerNameElement = get(By.className("seller-name__detail"));
            setSellerName(lazada);

            mProductHighlightsElement = get(By.className("pdp-product-highlights"));
            setProductHighlight(lazada);

            mDetailImageTextElementList = getList(By.className("module-detailImageText"));
            setDetailText(lazada);

            mDetailContentElement = get(By.className("detail-content"));
            setDetailContent(lazada);

            saveLazada(lazada);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setTitle(Lazada lazada) {
        if (mTitleElement != null) {
            String title = mTitleElement.getText();
            if (!Strings.isNullOrEmpty(title)) {
                lazada.setTitle(title);
            }
        }
    }

    private void getGalleryImage(Lazada lazada) {
        if (mGalleryElementList != null && !mGalleryElementList.isEmpty()) {
            List<String> imgUrlList = new ArrayList<>();
            for (WebElement galleryElement : mGalleryElementList) {
                if (galleryElement != null) {
                    String imgUrl = galleryElement.getAttribute("src");
                    if (!Strings.isNullOrEmpty(imgUrl)) {
                        log.info("gallery img url origin: " + imgUrl);
                        imgUrl = imgUrl.split("_")[0];
                        imgUrlList.add(imgUrl);
                        log.info("gallery img url after: " + imgUrl);
                    }
                }
            }

            lazada.setGalleryList(imgUrlList);
        }
    }

    private void setSkuAndPrice(Lazada lazada) {
        if (mSkuElementList != null && !mSkuElementList.isEmpty()) {
            List<Sku> skuList = new ArrayList<>();
            for (WebElement skuElement : mSkuElementList) {
                if (skuElement != null) {
                    Sku sku = new Sku();
                    if (!Strings.isNullOrEmpty(skuElement.getText())) {
                        sku.setSku(skuElement.getText());
                    }
                    click(skuElement);
                    if (mPriceElement != null) {
                        String priceStr = mPriceElement.getText();
                        if (!Strings.isNullOrEmpty(priceStr)) {
                            long price = Long.parseLong(priceStr.replace("Rp", "")
                                    .replace(".", ""));
                            sku.setPrice(price);
                            skuList.add(sku);
                        }
                    }
                }
            }

            lazada.setSkuList(skuList);
        } else {
            if (mPriceElement != null) {
                String priceStr = mPriceElement.getText();
                if (!Strings.isNullOrEmpty(priceStr)) {
                    long price = Long.parseLong(priceStr.replace("Rp", "")
                            .replace(".", ""));
                    lazada.setPrice(price);
                }
            }
        }
    }

    private void setSellerName(Lazada lazada) {
        if (mSellerNameElement != null && !Strings.isNullOrEmpty(mSellerNameElement.getText())) {
            lazada.setSeller(mSellerNameElement.getText());
        }
    }

    private void setProductHighlight(Lazada lazada) {
        if (mProductHighlightsElement != null && !Strings.isNullOrEmpty(mProductHighlightsElement.getText())) {
            lazada.setProductHighlight(mProductHighlightsElement.getText());
        }
    }

    private void setDetailText(Lazada lazada) {
        if (mDetailImageTextElementList != null && !mDetailImageTextElementList.isEmpty()) {
            List<String> detailTextList = new ArrayList<>();
            List<String> detailImageList = new ArrayList<>();
            for (WebElement detailImageTextElement : mDetailImageTextElementList) {
                if (detailImageTextElement != null) {
                    List<WebElement> textElementList = detailImageTextElement.findElements(By.tagName("p"));
                    List<WebElement> imageElementList = detailImageTextElement.findElements(By.tagName("img"));
                    if (!textElementList.isEmpty()) {
                        for (WebElement textElement : textElementList) {
                            if (textElement != null && !Strings.isNullOrEmpty(textElement.getText())) {
                                String[] textArray = textElement.getText().split("\n");
                                Collections.addAll(detailTextList, textArray);
                            }
                        }
                    }

                    if (!imageElementList.isEmpty()) {
                        for (WebElement imageElement : imageElementList) {
                            if (imageElement != null && !Strings.isNullOrEmpty(imageElement.getAttribute("src"))) {
                                detailImageList.add(imageElement.getAttribute("src"));
                            }
                        }
                    }
                }
            }

            lazada.setDetailTextList(detailTextList);
            lazada.setDetailImageList(detailImageList);
        }
    }

    private void setDetailContent(Lazada lazada) {
        if (mDetailContentElement != null) {
            List<String> detailContentList = new ArrayList<>();

            if (!Strings.isNullOrEmpty(mDetailContentElement.getText())) {
                String[] textArray = mDetailContentElement.getText().split("\n");
                Collections.addAll(detailContentList, textArray);
            }

            List<WebElement> textElementList = mDetailContentElement.findElements(By.tagName("p"));
            if (!textElementList.isEmpty()) {
                for (WebElement textElement : textElementList) {
                    if (textElement != null && !Strings.isNullOrEmpty(textElement.getText())) {
                        String[] textArray = textElement.getText().split("\n");
                        Collections.addAll(detailContentList, textArray);
                    }
                }
            }

            lazada.setDetailContentList(detailContentList);

            List<WebElement> imageElementList = mDetailContentElement.findElements(By.tagName("img"));
            if (!imageElementList.isEmpty()) {
                List<String> detailContentImageList = new ArrayList<>();
                for (WebElement imageElement : imageElementList) {
                    if (imageElement != null && !Strings.isNullOrEmpty(imageElement.getAttribute("src"))) {
                        detailContentImageList.add(imageElement.getAttribute("src"));
                    }
                }

                lazada.setDetailContentImageList(detailContentImageList);
            }
        }
    }

    private void saveLazada(Lazada lazada) {

        String title = lazada.getTitle();

        title = title.toLowerCase()
                .replaceAll("\\(.*?\\) *", "")
                .replaceAll("（.*?） *", "")
                .replaceAll("\\[.*?] *", "")
                .replaceAll("【.*?】 *", "")
                .replaceAll("<.*?> *", "")
                .replaceAll("《.*?》 *", "")
                .replaceAll("\\{.*?} *", "")
                .replaceAll("cod", "")
                .replaceAll("bayar di tempat", "")
                .replaceAll("cash on delivery", "");

        while (title.startsWith(" ")) {
            title = title.replaceFirst("_", "");
        }

        while (title.endsWith(" ")) {
            title = title.substring(0, title.lastIndexOf(" "));
        }

        title = "free shipping " + title;

        lazada.setTitle(title);

        float multiple = mRandom.nextFloat() * (MAX_MULTIPLE - MIN_MULTIPLE) + MIN_MULTIPLE;

        long price = lazada.getPrice();
        List<Sku> skuList = lazada.getSkuList();

        if (price != 0) {
            price = (long) ((price + PRICE_INCREMENT) * multiple);
            lazada.setPrice(price);
        }

        if (skuList != null && !skuList.isEmpty()) {
            for (Sku sku : skuList) {
                if (sku != null) {
                    sku.setPrice((long) ((sku.getPrice() + PRICE_INCREMENT) * multiple));
                }
            }
        }

        List<String> detailTextList = lazada.getDetailTextList();
        if (detailTextList == null) {
            detailTextList = new ArrayList<>();
        }

        detailTextList.add(0, "Yang mau murunkan berat badan dengan cara efektif dan aman, " +
                "bisa search di shopee dengan kata kunci \"bosslim\".");

        lazada.setDetailTextList(detailTextList);

        String goodsDir = RESULT_DIR + File.separator + System.currentTimeMillis();

        IOUtils.saveToPath(goodsDir, INFO_FILE_NAME, mGson.toJson(lazada).getBytes(StandardCharsets.UTF_8));
    }

    public List<String> downloadImage() {
        File resultDir = new File(RESULT_DIR);
        try {
            File[] destDirArray = resultDir.listFiles();
            if (destDirArray == null || destDirArray.length == 0) {
                log.info("could not get any dest dir");
                return null;
            }

            for (File destDir : destDirArray) {
                File[] info = destDir.listFiles((file, name) -> INFO_FILE_NAME.equals(name));

                if (info == null || info.length == 0) {
                    log.info("could not get info.json on " + destDir.getAbsolutePath());
                    continue;
                }

                Lazada lazada = mGson.fromJson(IOUtils.readString(info[0].getAbsolutePath()), Lazada.class);
                if (lazada != null) {

                    if (lazada.getGalleryList() != null && !lazada.getGalleryList().isEmpty()) {
                        String galleryDir = destDir.getAbsolutePath() + File.separator + GALLERY_DIR_NAME;
                        for (int i = 0; i < lazada.getGalleryList().size(); i++) {
                            String galleryUrl = lazada.getGalleryList().get(i);
                            String suffix = StringUtils.getExtension(galleryUrl, false);
                            String destFileName = String.format("%s_%s.%s", IMAGE_NAME_PREFIX, i, suffix);
                            DownloadInfo downloadInfo = new DownloadInfo(galleryUrl, galleryDir, destFileName);
                            mDownloadInfoList.add(downloadInfo);
                        }
                    }

                    if (lazada.getDetailContentImageList() != null && !lazada.getDetailContentImageList().isEmpty()) {
                        String detailDir = destDir.getAbsolutePath() + File.separator + DETAIL_DIR_NAME;
                        for (int i = 0; i < lazada.getDetailContentImageList().size(); i++) {
                            String detailUrl = lazada.getDetailContentImageList().get(i);
                            String suffix = StringUtils.getExtension(detailUrl, false);
                            String destFileName = String.format("%s_%s.%s", IMAGE_NAME_PREFIX, i, suffix);
                            DownloadInfo downloadInfo = new DownloadInfo(detailUrl, detailDir, destFileName);
                            mDownloadInfoList.add(downloadInfo);
                        }
                    }
                }
            }

            List<String> failedList = new ArrayList<>();
            for (DownloadInfo downloadInfo : mDownloadInfoList) {
                failedList.add(downloadInfo.getUrl());
            }

            for (DownloadInfo downloadInfo : mDownloadInfoList) {
                NetworkHelper.getInstance().downloadExecute(downloadInfo, new INetworkCallbackListener() {
                    @Override
                    public void onProgress(int progress) {
                        log.info("download :" + downloadInfo.getUrl() + ", progress :" + progress);
                    }

                    @Override
                    public void onSuccess(String destFilePath) {
                        log.info("download :" + downloadInfo.getUrl() + ", success :" + downloadInfo.getDestDir());
                        failedList.remove(downloadInfo.getUrl());
                    }

                    @Override
                    public void onFailure(Exception ex) {
                        log.error("download : " + downloadInfo.getUrl() + ", failed", ex);
                    }
                });
            }

            return failedList;
        } catch (Exception ex) {
            log.error(ex);
            return null;
        }
    }

    public void makeGalleryCovered() {
        File resultDir = new File(RESULT_DIR);
        try {
            File[] destDirArray = resultDir.listFiles();
            if (destDirArray == null || destDirArray.length == 0) {
                log.info("could not get any dest dir");
                return;
            }

            for (File destDir : destDirArray) {
                File[] galleryDir = destDir.listFiles((file, name) -> GALLERY_DIR_NAME.equals(name));

                if (galleryDir == null || galleryDir.length == 0) {
                    log.info("could not get gallery dir on " + destDir.getAbsolutePath());
                    continue;
                }

                File[] galleryImageArray = galleryDir[0].listFiles();
                if (galleryImageArray == null || galleryImageArray.length == 0) {
                    log.info("cloud not get any image on " + galleryDir[0].getAbsolutePath());
                    continue;
                }

                File galleryFile = galleryImageArray[0];

                ImageUtils.addCover(galleryFile.getAbsolutePath(), COVER_IMAGE_PATH);
            }
        } catch (Exception ex) {
            log.error(ex);
        }
    }
}
