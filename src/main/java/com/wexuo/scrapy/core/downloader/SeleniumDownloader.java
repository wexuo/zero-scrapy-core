package com.wexuo.scrapy.core.downloader;

import com.wexuo.scrapy.core.Page;
import com.wexuo.scrapy.core.Request;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Selenium 页面下载器，需结合 webdriver.remote.address 配置使用
 *
 * @see org.openqa.selenium.remote.RemoteWebDriver
 */
public class SeleniumDownloader extends AbstractDownloader {

    private static final Logger LOG = LoggerFactory.getLogger(SeleniumDownloader.class);

    private final String xpath;

    private final long timeout;

    private final WebDriver driver;

    public SeleniumDownloader(final String xpath, final long timeout, final WebDriver driver) {
        this(xpath, timeout, null, driver);
    }

    public SeleniumDownloader(final String xpath, final long timeout, final DownloaderFilter filter, final WebDriver driver) {
        super(filter);
        this.xpath = xpath;
        this.timeout = timeout;
        this.driver = driver;
    }

    @Override
    public Page download(final Request request, final Page page) {
        try {
            try {
                driver.manage().timeouts().pageLoadTimeout(timeout, TimeUnit.MILLISECONDS);
                driver.get(request.getUrl());
            } catch (final Exception e) {
                // DO NOTHING
            }
            afterDownload(driver, request);

            final WebElement webElement = driver.findElement(By.xpath("/html"));
            final String outerHTML = webElement.getAttribute("outerHTML");

            page.setRawText(outerHTML);
            page.setStatus(HttpStatus.OK);
            return page;
        } catch (final Exception e) {
            onError(request, e);
            LOG.info("download page {} error", request.getUrl(), e);
            throw e;
        }
    }

    protected void afterDownload(final WebDriver driver, final Request request) {
        if (Objects.nonNull(xpath)) {
            final WebDriverWait wait = new WebDriverWait(driver, timeout / 1000);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));
        }
    }

    protected void onError(final Request request, final Throwable e) {

    }
}
