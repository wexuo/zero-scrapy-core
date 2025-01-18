package com.wexuo.scrapy.core;

import com.wexuo.scrapy.core.data.FieldRule;
import com.wexuo.scrapy.core.data.ItemLinkRule;
import com.wexuo.scrapy.core.data.PageLinkRule;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Extractor {
    private String key;
    private String name;
    private String domain;
    private Boolean proxy = false;
    private String waitXpath;
    private List<String> args;
    private PageLinkRule pageLinkRule;
    private ItemLinkRule itemLinkRule;
    private final List<FieldRule> fields = new ArrayList<>();

    public void setPageLinkRule(final String format, final String pattern) {
        pageLinkRule = new PageLinkRule();
        pageLinkRule.setFormat(format);
        pageLinkRule.setPattern(pattern);
    }

    public void setItemLinkRule(final String xpath, final String pattern) {
        itemLinkRule = new ItemLinkRule();
        itemLinkRule.setItemXpath(xpath);
        itemLinkRule.setLinkXpath(pattern);
    }

    public void addFieldRules(final List<FieldRule> fieldRules) {
        fields.addAll(fieldRules);
    }
}
