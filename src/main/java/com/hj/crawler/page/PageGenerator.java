package com.hj.crawler.page;

import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

@Log4j2
public class PageGenerator {

    public WebDriver mWebDriver;

    public PageGenerator(WebDriver webDriver) {
        mWebDriver = webDriver;
    }

    public <T extends BasePage> T create(Class<T> pageClass) {
        try {
            return PageFactory.initElements(mWebDriver, pageClass);
        } catch (Exception ex) {
            log.error(ex);
            return null;
        }
    }
}
