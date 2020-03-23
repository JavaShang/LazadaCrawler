package com.hj.crawler.lazada;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class LazadaWebDriver {

    private WebDriver mDriver;

    public LazadaWebDriver() {
        System.setProperty("webdriver.chrome.driver", System.getProperty("user.dir") + "\\chromedriver.exe");

        ChromeOptions options = new ChromeOptions();

        mDriver = new ChromeDriver(options);
    }

    public WebDriver getWebDriver() {
        return mDriver;
    }
}
