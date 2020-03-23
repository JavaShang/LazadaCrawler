package com.hj.crawler.page;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

public class PageGenerator {

    public WebDriver mWebDriver;

    public PageGenerator(WebDriver webDriver) {
        mWebDriver = webDriver;
    }

    public <T extends BasePage> T create(Class<T> pageClass) {
        try {
            return PageFactory.initElements(mWebDriver, pageClass);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
