package com.wexuo.scrapy.core.data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageLinkRule {
    /**
     * 页码格式
     */
    private String format;
    /**
     * 页码正则表达式
     */
    private String pattern;
    /**
     * 页码链接对应的 xpath
     */
    private String xpath;
    /**
     * 起始页，可选，默认为 String.format(页码格式, 1)
     */
    private String start;
}
