package com.hj.crawler.lazada;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hj.crawler.lazada.model.Lazada;
import com.hj.crawler.lazada.model.Sku;
import com.hj.crawler.page.BasePage;
import com.hj.crawler.utils.IOUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;


public class LazadaPage extends BasePage {

    private static final String RESULT_DIR = System.getProperty("user.dir") + "\\CrawlerResult";

    private static final int PRICE_INCREMENT = 30000;
    private static final float MIN_MULTIPLE = 1.1F;
    private static final float MAX_MULTIPLE = 1.3F;

    private Random mRandom;
    private Gson mGson;

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
    }

    public void fetchDataByUrls(List<String> urlList) {
        Preconditions.checkNotNull(urlList, "url list should not be null");
        Preconditions.checkElementIndex(0, urlList.size(), "url list should not be empty,");

        mRandom = new Random();
        mGson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create();

        for (String url : urlList) {
            fetchDataByUrl(url);
        }
    }

    private void fetchDataByUrl(String url) {
        Preconditions.checkNotNull(url, "url should not be null");
        Preconditions.checkArgument(!url.isEmpty(), "url should not be empty");

        try {
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
                        System.out.println("before: " + imgUrl);
                        imgUrl = imgUrl.split("_")[0];
                        imgUrlList.add(imgUrl);
                        System.out.println("after: " + imgUrl);
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
                                detailTextList.add(textElement.getText());
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
                detailContentList.add(mDetailContentElement.getText());
            }

            List<WebElement> textElementList = mDetailContentElement.findElements(By.tagName("p"));
            if (!textElementList.isEmpty()) {
                for (WebElement textElement : textElementList) {
                    if (textElement != null && !Strings.isNullOrEmpty(textElement.getText())) {
                        detailContentList.add(textElement.getText());
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

        IOUtils.saveToPath(goodsDir, "info.json", mGson.toJson(lazada).getBytes(StandardCharsets.UTF_8));

        List<String> galleryList = lazada.getGalleryList();
        if (galleryList != null && !galleryList.isEmpty()) {
            String galleryDir = goodsDir + File.separator + "gallery";
            for (int i = 0; i < galleryList.size(); i++) {
                String gallery = galleryList.get(i);
                if (!Strings.isNullOrEmpty(gallery)) {
                    try {
                        int index = i;
                        IOUtils.download(gallery, new Callback() {
                            @Override
                            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                                ResponseBody responseBody = response.body();
                                if (responseBody != null) {
                                    String suffix = gallery.substring(gallery.lastIndexOf("."));
                                    IOUtils.saveToPath(galleryDir, String.format("gallery-%d%s", index, suffix), responseBody.bytes());
                                }
                            }

                            @Override
                            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                e.printStackTrace();
                                System.out.println("fail to download : " + gallery);
                            }
                        });
                    } catch (Exception ex) {
                        System.out.println("fail to download :" + gallery);
                        ex.printStackTrace();
                    }
                }
            }
        }

        List<String> detailImageList = lazada.getDetailImageList();
        if (detailImageList != null && !detailImageList.isEmpty()) {
            String detailImageDir = goodsDir + File.separator + "detail";
            for (int i = 0; i < detailImageList.size(); i++) {
                String detailImage = detailImageList.get(i);
                if (!Strings.isNullOrEmpty(detailImage)) {
                    try {
                        int index = i;
                        IOUtils.download(detailImage, new Callback() {
                            @Override
                            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                                ResponseBody responseBody = response.body();
                                if (responseBody != null) {
                                    String suffix = detailImage.substring(detailImage.lastIndexOf("."));
                                    IOUtils.saveToPath(detailImageDir, String.format("detail-%d%s", index, suffix), responseBody.bytes());
                                }
                            }

                            @Override
                            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                System.out.println("fail to download : " + detailImage);
                                e.printStackTrace();
                            }
                        });
                    } catch (Exception ex) {
                        System.out.println("fail to download :" + detailImage);
                        ex.printStackTrace();
                    }
                }
            }
        }
    }
}
