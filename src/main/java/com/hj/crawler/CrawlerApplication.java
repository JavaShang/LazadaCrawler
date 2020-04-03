package com.hj.crawler;

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

        if (args == null || args.length < 1) {
            return;
        }

        List<String> urlList = IOUtils.readLines(args[0]);
        if (urlList == null || urlList.isEmpty()) {
            return;
        }

        LazadaWebDriver webDriverProxy = new LazadaWebDriver();
        PageGenerator pageGenerator = new PageGenerator(webDriverProxy.getWebDriver());
        LazadaPage lazadaPage = pageGenerator.create(LazadaPage.class);

        lazadaPage.fetchDataByUrls(urlList);

        lazadaPage.close();
        lazadaPage.quit();
    }
}
