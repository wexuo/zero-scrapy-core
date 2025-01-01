package com.wexuo.scrapy.core.downloader;

import com.wexuo.scrapy.core.Page;
import com.wexuo.scrapy.core.Request;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.net.URL;
import java.time.Duration;
import java.util.Objects;

/**
 * Selenium 页面下载器，需结合 webdriver.remote.address 配置使用
 *
 * @see org.openqa.selenium.remote.RemoteWebDriver
 */
public class SeleniumDownloader implements Downloader {

    private static final Logger LOG = LoggerFactory.getLogger(SeleniumDownloader.class);

    private final String xpath;

    private final long timeout;

    public SeleniumDownloader(final String xpath, final long timeout) {
        this.xpath = xpath;
        this.timeout = timeout;
    }

    @Override
    public Page download(final Request request) {
        final Page page = new Page(request);
        WebDriver driver = null;
        try {
            driver = getLocalWebDriver();
            try {
                driver.manage().timeouts().pageLoadTimeout(Duration.ofMillis(timeout));
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
        } finally {
            if (Objects.nonNull(driver)) {
                driver.quit();
            }
        }
    }

    protected void afterDownload(final WebDriver driver, final Request request) {
        if (Objects.nonNull(xpath)) {
            final WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));
        }
    }

    protected void onError(final Request request, final Throwable e) {

    }

    private WebDriver getRemoteWebDriver() {
        try {
            final String address = System.getProperty("webdriver.remote.address");
            if (Objects.isNull(address)) {
                throw new RuntimeException("webdriver.remote.address is null");
            }
            return new RemoteWebDriver(new URL(address), getChromeOptions());
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public WebDriver getLocalWebDriver() {
        return new ChromeDriver(getChromeOptions());
    }

    private ChromeOptions getChromeOptions() {
        final ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--ignore-certificate-errors");
        options.addArguments("--allow-running-insecure-content");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--no-sandbox");
        options.addArguments("--remote-allow-origins=*");
        final String address = System.getProperty("webdriver.debugger.address");
        if (Objects.nonNull(address)) {
            options.setExperimentalOption("debuggerAddress", address);
        }
        return options;
    }
}
