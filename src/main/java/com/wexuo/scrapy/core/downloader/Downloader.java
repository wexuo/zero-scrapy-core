package com.wexuo.scrapy.core.downloader;

import com.wexuo.scrapy.core.Page;
import com.wexuo.scrapy.core.Request;

public interface Downloader {
    Page download(Request request);
}
