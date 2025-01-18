package com.wexuo.scrapy.core.downloader;

import com.wexuo.scrapy.core.Page;
import com.wexuo.scrapy.core.Request;
import org.springframework.http.HttpStatus;

import java.util.Objects;

public abstract class AbstractDownloader implements Downloader {

    private final DownloaderFilter filter;

    protected AbstractDownloader(final DownloaderFilter filter) {
        this.filter = filter;
    }

    @Override
    public Page download(final Request request) {
        final Page page = new Page(request);
        if (Objects.nonNull(filter) && filter.filter(page)) {
            page.setStatus(HttpStatus.OK);
            page.setIgnore(true);
            return page;
        }
        return download(request, page);
    }

    protected abstract Page download(Request request, Page page);
}
