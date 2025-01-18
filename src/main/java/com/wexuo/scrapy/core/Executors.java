package com.wexuo.scrapy.core;

import com.wexuo.scrapy.core.data.PageLinkRule;
import com.wexuo.scrapy.core.downloader.Downloader;
import com.wexuo.scrapy.core.downloader.DownloaderFilter;
import com.wexuo.scrapy.core.downloader.RestTemplateDownloader;
import com.wexuo.scrapy.core.downloader.SeleniumDownloader;
import com.wexuo.scrapy.core.processor.ExtractorPageProcessor;
import com.wexuo.scrapy.core.processor.Processor;
import com.wexuo.scrapy.core.util.UserAgentUtil;
import org.openqa.selenium.WebDriver;

import java.util.List;
import java.util.Objects;

public class Executors {

    public static <T> List<T> getResult(final Extractor extractor, final Integer page, final Class<T> clazz) {
        return getResult(extractor, getTarget(extractor, page), clazz, null);
    }

    public static <T> List<T> getResult(final Extractor extractor, final Integer page, final Class<T> clazz, final DownloaderFilter filter) {
        return getResult(extractor, getTarget(extractor, page), clazz, filter);
    }

    public static <T> List<T> getResult(final Extractor extractor, final String target, final Class<T> clazz, final DownloaderFilter filter) {
        final Downloader downloader = new RestTemplateDownloader(extractor.getProxy(), filter);
        final Processor processor = new ExtractorPageProcessor(extractor);
        return Spider.create(getSite(extractor)).setDownloader(downloader).thread(20).setProcessor(processor).getResult(target, clazz);
    }

    private static Site getSite(final Extractor extractor) {
        return Site.me().setDelay(5000).setUserAgent(UserAgentUtil.random())
                .addHeader(Constant.HOST, extractor.getDomain())
                .addHeader(Constant.REFERER, extractor.getDomain());
    }

    public static <T> List<T> getRemoteResult(final Extractor extractor, final Integer page, final Class<T> clazz, final WebDriver driver) {
        return getRemoteResult(extractor, getTarget(extractor, page), clazz, driver);
    }

    public static <T> List<T> getRemoteResult(final Extractor extractor, final String target, final Class<T> clazz, final WebDriver driver) {
        final Site site = getSite(extractor);
        final Downloader downloader = new SeleniumDownloader(extractor.getWaitXpath(), site.getTimeout(), driver);
        final Processor processor = new ExtractorPageProcessor(extractor);
        return Spider.create(site).setDownloader(downloader).thread(20).setProcessor(processor).getResult(target, clazz);
    }

    public static String getTarget(final Extractor extractor, final Integer page) {
        final PageLinkRule rule = extractor.getPageLinkRule();
        final String format = rule.getFormat();
        return getTarget(extractor, page, rule.getStart(), format);
    }

    public static String getTarget(final Extractor extractor, final Integer page, final String... args) {
        final PageLinkRule rule = extractor.getPageLinkRule();
        String format = rule.getFormat();
        if (Objects.nonNull(args)) {
            for (final String arg : args) {
                format = format.replaceFirst("%s", arg);
            }
        }
        return getTarget(extractor, page, rule.getStart(), format);
    }

    private static String getTarget(final Extractor extractor, final Integer page, final String start, final String format) {
        if (page == 1 && Objects.nonNull(start)) {
            if (format.startsWith(Constant.HTTP)) {
                return String.format(format, start);
            }
            return String.format(extractor.getDomain() + format, start);
        }
        if (format.startsWith(Constant.HTTP)) {
            return String.format(format, page);
        }
        return String.format(extractor.getDomain() + format, page);
    }
}
