package com.wexuo.scrapy.core.downloader;

import com.wexuo.scrapy.core.Page;

/**
 * 页面下载过滤，符合条件的跳过
 */
public interface DownloaderFilter {
    boolean filter(Page page);
}
