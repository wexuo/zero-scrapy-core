package com.wexuo.scrapy.core;

import com.wexuo.scrapy.core.data.PageLinkRule;
import com.wexuo.scrapy.core.downloader.DefaultDownloader;
import com.wexuo.scrapy.core.downloader.Downloader;
import com.wexuo.scrapy.core.downloader.SeleniumDownloader;
import com.wexuo.scrapy.core.processor.ExtractorPageProcessor;
import com.wexuo.scrapy.core.processor.Processor;
import com.wexuo.scrapy.core.util.UserAgentUtil;

import java.util.List;
import java.util.Objects;

public class Executors {

    public static <T> List<T> getResult(final Extractor extractor, final Integer page, final Class<T> clazz) {
        final String target = getTarget(extractor, page);
        final Site site = Site.me().setDelay(5000).setUserAgent(UserAgentUtil.random())
                .addHeader(Constant.HOST, extractor.getDomain())
                .addHeader(Constant.REFERER, extractor.getDomain());
        final Downloader downloader = new DefaultDownloader(extractor.getProxy());
        final Processor processor = new ExtractorPageProcessor(extractor);
        return Spider.create(site).setDownloader(downloader).thread(20).setProcessor(processor).getResult(target, clazz);
    }

    public static <T> List<T> getRemoteResult(final Extractor extractor, final Integer page, final Class<T> clazz) {
        final String target = getTarget(extractor, page);
        final Site site = Site.me().setDelay(5000).setUserAgent(UserAgentUtil.random())
                .addHeader(Constant.HOST, extractor.getDomain())
                .addHeader(Constant.REFERER, extractor.getDomain());
        final Downloader downloader = new SeleniumDownloader(extractor.getWaitXpath(), site.getTimeout());
        final Processor processor = new ExtractorPageProcessor(extractor);
        return Spider.create(site).setDownloader(downloader).thread(20).setProcessor(processor).getResult(target, clazz);
    }

    public static String getTarget(final Extractor extractor, final Integer page) {
        final PageLinkRule rule = extractor.getPageLinkRule();
        if (page == 1 && Objects.nonNull(rule.getStart())) {
            final String start = rule.getStart();
            if (start.startsWith(Constant.HTTP)) {
                return start;
            }
            return extractor.getDomain() + start;
        }
        final String format = rule.getFormat();
        if (format.startsWith(Constant.HTTP)) {
            return String.format(format, page);
        }
        return String.format(extractor.getDomain() + format, page);
    }
}
