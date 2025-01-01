package com.wexuo.scrapy.core.data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemLinkRule {
    /**
     * 列表单个项对应的 xpath 表达式
     */
    private String itemXpath;
    /**
     * 列表单个项对应的链接 xpath 表达式，可选
     */
    private String linkXpath;
}
