package com.wexuo.scrapy.core.pipeline;

import com.wexuo.scrapy.core.Page;

public interface Pipeline {
    void process(Page page);
}
