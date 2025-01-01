package com.wexuo.scrapy.core.downloader;

import com.wexuo.scrapy.core.Page;
import com.wexuo.scrapy.core.Request;
import com.wexuo.scrapy.core.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

/**
 * 远程下载器，基于 api 请求网页内容
 */
public class RemoteDownloader implements Downloader {

    private static final Logger LOG = LoggerFactory.getLogger(RemoteDownloader.class);

    @Override
    public Page download(final Request request) {
        final Page page = new Page(request);
        try {
            final String address = getRemoteAddress(request);
            final RestTemplate restTemplate = HttpUtil.getRestTemplate();
            final ResponseEntity<HtmlPage> entity = restTemplate.getForEntity(address, HtmlPage.class);
            final HtmlPage htmlPage = entity.getBody();
            if (Objects.isNull(htmlPage)) {
                page.setStatus(HttpStatus.NOT_FOUND);
                return page;
            }
            page.setRawText(htmlPage.getHtml());
            page.setStatus(HttpStatus.OK);
            return page;
        } catch (final Exception e) {
            onError(request, e);
            LOG.info("download page {} error", request.getUrl(), e);
            throw e;
        }
    }

    public String getRemoteAddress(final Request request) {
        final String address = System.getProperty("http.remote.address");
        if (Objects.isNull(address)) {
            throw new RuntimeException("http.remote.address is null");
        }
        return address + request.getUrl();
    }

    protected void onError(final Request request, final Throwable e) {

    }
}
