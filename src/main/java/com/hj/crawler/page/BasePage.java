package com.hj.crawler.page;

import com.google.common.base.Preconditions;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

@Log4j2
public abstract class BasePage extends PageGenerator {

    private static final long TIMEOUT = 30;

    public BasePage(WebDriver webDriver) {
        super(webDriver);
    }

    public void get(String url) {
        Preconditions.checkNotNull(url, "url should not be null");
        Preconditions.checkArgument(!url.isEmpty(), "url should not be empty");

        mWebDriver.get(url);
    }

    public WebElement get(By by) {
        try {
            return mWebDriver.findElement(by);
        } catch (Exception ex) {
            log.error(ex);
            return null;
        }
    }

    public List<WebElement> getList(By by) {
        try {
            return mWebDriver.findElements(by);
        } catch (Exception ex) {
            log.error(ex);
            return null;
        }
    }

    public <T> T wait(ExpectedCondition<T> c) {
        return wait(c, TIMEOUT);
    }

    public <T> T wait(ExpectedCondition<T> c, long timeout) {
        WebDriverWait webDriverWait = new WebDriverWait(mWebDriver, timeout);
        try {
            return webDriverWait.until(c);
        } catch (Exception ex) {
            log.error(ex);
            return null;
        }
    }

    public <T> T waitIgnore(ExpectedCondition<T> c, long timeout) {
        WebDriverWait webDriverWait = new WebDriverWait(mWebDriver, timeout);
        webDriverWait.ignoring(NoSuchElementException.class);
        try {
            return webDriverWait.until(c);
        } catch (Exception ex) {
            log.error(ex);
            return null;
        }
    }

    public <T> void click(T element) {
        if (element.getClass().getName().contains("By")) {
            mWebDriver.findElement((By) element).click();
        } else {
            ((WebElement) element).click();
        }
    }

    public void close() {
        mWebDriver.close();
    }

    public void quit() {
        mWebDriver.quit();
    }
}
