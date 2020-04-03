package com.hj.crawler.lazada;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hj.crawler.constants.Constants;
import com.hj.crawler.lazada.model.Lazada;
import com.hj.crawler.lazada.model.Sku;
import com.hj.crawler.model.ProductInfo;
import com.hj.crawler.model.SkuInfo;
import com.hj.crawler.page.BasePage;
import com.hj.crawler.utils.IOUtils;
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
import java.util.concurrent.TimeUnit;

@Log4j2
public class LazadaPage extends BasePage {

    private static final String RESULT_DIR = Constants.ROOT_PATH + "\\CrawlerResult\\Lazada";
    private static final String INFO_FILE_NAME = "info.json";
    private static final String PRODUCT_INFO_FILE_NAME = "product_info.json";

    private static final String IMAGE_DIR_NAME = "img";

    private Gson mGson;

    public LazadaPage(WebDriver webDriver) {
        super(webDriver);
        mGson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }

    public void fetchDataByUrls(List<String> urlList) {
        if (urlList == null || urlList.isEmpty()) {
            return;
        }

        for (String url : urlList) {
            fetchDataByUrl(url);
        }
    }

    private void fetchDataByUrl(String url) {
        if (Strings.isNullOrEmpty(url)) {
            return;
        }

        try {

            log.info("start get url :" + url);
            get(url);

            Lazada lazada = new Lazada();
            lazada.setItemId(StringUtils.getLazadaItemId(url));
            lazada.setOriginUrl(url);

            Thread.sleep(TimeUnit.SECONDS.toMillis(30));

            setTitle(lazada);
            getGalleryImage(lazada);
            setSkuAndPrice(lazada);
            setSellerName(lazada);
            setProductHighlight(lazada);
            setDetailText(lazada);
            setDetailContent(lazada);

            String resultDir = RESULT_DIR + File.separator + lazada.getItemId();

            downloadImage(resultDir, lazada);
            saveLazada(resultDir, lazada);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setTitle(Lazada lazada) {
        WebElement titleElement = get(By.className("pdp-product-title"));
        if (titleElement != null) {
            String title = titleElement.getText();
            if (!Strings.isNullOrEmpty(title)) {
                lazada.setTitle(title);
            }
        }
    }

    private void getGalleryImage(Lazada lazada) {
        List<WebElement> galleryElementList = getList(By.className("pdp-mod-common-image"));
        if (galleryElementList != null && !galleryElementList.isEmpty()) {
            List<String> imgUrlList = new ArrayList<>();
            for (WebElement galleryElement : galleryElementList) {
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
        WebElement priceElement = get(By.className("pdp-price_type_normal"));
        List<WebElement> skuElementList = getList(By.className("sku-variable-name-text"));

        if (skuElementList != null && !skuElementList.isEmpty()) {
            List<Sku> skuList = new ArrayList<>();
            for (WebElement skuElement : skuElementList) {
                if (skuElement != null) {
                    Sku sku = new Sku();
                    if (!Strings.isNullOrEmpty(skuElement.getText())) {
                        sku.setSku(skuElement.getText());
                    }
                    click(skuElement);
                    if (priceElement != null) {
                        String priceStr = priceElement.getText();
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
            if (priceElement != null) {
                String priceStr = priceElement.getText();
                if (!Strings.isNullOrEmpty(priceStr)) {
                    long price = Long.parseLong(priceStr.replace("Rp", "")
                            .replace(".", ""));
                    lazada.setPrice(price);
                }
            }
        }
    }

    private void setSellerName(Lazada lazada) {
        WebElement sellerNameElement = get(By.className("seller-name__detail"));
        if (sellerNameElement != null && !Strings.isNullOrEmpty(sellerNameElement.getText())) {
            lazada.setSeller(sellerNameElement.getText());
        }
    }

    private void setProductHighlight(Lazada lazada) {
        WebElement productHighlightsElement = get(By.className("pdp-product-highlights"));
        if (productHighlightsElement != null && !Strings.isNullOrEmpty(productHighlightsElement.getText())) {
            List<String> productHighlightList = new ArrayList<>();
            String[] textArray = productHighlightsElement.getText().split("\n");
            Collections.addAll(productHighlightList, textArray);
            lazada.setProductHighlightList(productHighlightList);
        }
    }

    private void setDetailText(Lazada lazada) {
        List<WebElement> detailImageTextElementList = getList(By.className("module-detailImageText"));
        if (detailImageTextElementList != null && !detailImageTextElementList.isEmpty()) {
            List<String> detailTextList = new ArrayList<>();
            List<String> detailImageList = new ArrayList<>();
            for (WebElement detailImageTextElement : detailImageTextElementList) {
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
        WebElement detailContentElement = get(By.className("detail-content"));
        if (detailContentElement != null) {
            List<String> detailContentList = new ArrayList<>();

            if (!Strings.isNullOrEmpty(detailContentElement.getText())) {
                String[] textArray = detailContentElement.getText().split("\n");
                Collections.addAll(detailContentList, textArray);
            }

            List<WebElement> textElementList = detailContentElement.findElements(By.tagName("p"));
            if (!textElementList.isEmpty()) {
                for (WebElement textElement : textElementList) {
                    if (textElement != null && !Strings.isNullOrEmpty(textElement.getText())) {
                        String[] textArray = textElement.getText().split("\n");
                        Collections.addAll(detailContentList, textArray);
                    }
                }
            }

            lazada.setDetailContentList(detailContentList);

            List<WebElement> imageElementList = detailContentElement.findElements(By.tagName("img"));
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


    private void downloadImage(String resultDir, Lazada lazada) {
        String dirPath = resultDir + File.separator + IMAGE_DIR_NAME;

        if (lazada.getGalleryList() != null && !lazada.getGalleryList().isEmpty()) {
            for (String url : lazada.getGalleryList()) {
                IOUtils.downloadImage(dirPath, url);
            }
        }

        if (lazada.getDetailImageList() != null && !lazada.getDetailImageList().isEmpty()) {
            for (String url : lazada.getDetailImageList()) {
                IOUtils.downloadImage(dirPath, url);
            }
        }

        if (lazada.getDetailContentImageList() != null && !lazada.getDetailContentImageList().isEmpty()) {
            for (String url : lazada.getDetailContentImageList()) {
                IOUtils.downloadImage(dirPath, url);
            }
        }
    }

    private void saveLazada(String resultDir, Lazada lazada) {
        IOUtils.saveToPath(resultDir, INFO_FILE_NAME, mGson.toJson(lazada).getBytes(StandardCharsets.UTF_8));

        ProductInfo productInfo = new ProductInfo();
        productInfo.setUrl(lazada.getOriginUrl());
        productInfo.setName(lazada.getTitle());

        List<SkuInfo> skuInfoList = new ArrayList<>();

        List<Sku> skuList = lazada.getSkuList();
        if (skuList != null && !skuList.isEmpty()) {
            for (Sku sku : skuList) {
                SkuInfo skuInfo = new SkuInfo();
                skuInfo.setSkuName(sku.getSku());
                skuInfo.setPrice(sku.getPrice());
                skuInfoList.add(skuInfo);
            }
        } else {
            SkuInfo skuInfo = new SkuInfo();
            skuInfo.setPrice(lazada.getPrice());
            skuInfoList.add(skuInfo);
        }

        if (!skuInfoList.isEmpty()) {
            productInfo.setSkuInfoList(skuInfoList);
        }

        List<String> productHighlightList = lazada.getProductHighlightList();
        List<String> detailTextList = lazada.getDetailTextList();
        List<String> detailContentList = lazada.getDetailContentList();

        List<String> descriptionList = new ArrayList<>();
        if (productHighlightList != null && !productHighlightList.isEmpty()) {
            descriptionList.addAll(productHighlightList);
        }

        if (detailTextList != null && !detailTextList.isEmpty()) {
            descriptionList.addAll(detailTextList);
        }

        if (detailContentList != null && !detailContentList.isEmpty()) {
            descriptionList.addAll(detailContentList);
        }

        productInfo.setDescriptionList(descriptionList);

        IOUtils.saveToPath(resultDir, PRODUCT_INFO_FILE_NAME, mGson.toJson(productHighlightList).getBytes(StandardCharsets.UTF_8));
    }
}
