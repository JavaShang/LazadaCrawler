package com.hj.crawler;

import com.google.common.base.Preconditions;
import com.hj.crawler.lazada.LazadaPage;
import com.hj.crawler.lazada.LazadaWebDriver;
import com.hj.crawler.page.PageGenerator;
import com.hj.crawler.utils.IOUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@Log4j2
@SpringBootApplication
public class CrawlerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrawlerApplication.class, args);

        Preconditions.checkElementIndex(0, args.length, "args should not be empty,");

        String urlListPath = args[0];
        List<String> urlList = IOUtils.readLines(urlListPath);

        Preconditions.checkNotNull(urlList, "url list should not be null");
        Preconditions.checkElementIndex(0, urlList.size(), "url list should not be empty,");

        LazadaWebDriver webDriverProxy = new LazadaWebDriver();
        PageGenerator pageGenerator = new PageGenerator(webDriverProxy.getWebDriver());
        LazadaPage lazadaPage = pageGenerator.create(LazadaPage.class);
        lazadaPage.fetchDataByUrls(urlList);
        lazadaPage.close();
        lazadaPage.quit();

        List<String> failedList = lazadaPage.downloadImage();
        if (failedList != null) {
            for (String failedUrl : failedList) {
                log.error(failedUrl);
            }
        }
        lazadaPage.makeGalleryCovered();
    }
}
