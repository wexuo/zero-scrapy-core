package com.wexuo.scrapy.core.processor;

import com.wexuo.scrapy.core.Constant;
import com.wexuo.scrapy.core.Extractor;
import com.wexuo.scrapy.core.Page;
import com.wexuo.scrapy.core.Request;
import com.wexuo.scrapy.core.data.FieldRule;
import com.wexuo.scrapy.core.data.ItemLinkRule;
import com.wexuo.scrapy.core.data.PageLinkRule;
import com.wexuo.scrapy.core.util.DateUtil;
import com.wexuo.scrapy.core.value.ListFieldValue;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import us.codecraft.webmagic.selector.HtmlNode;
import us.codecraft.webmagic.selector.PlainText;
import us.codecraft.webmagic.selector.Selectable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 页面提取器，定义了页面内容提取流程
 */
public class ExtractorPageProcessor implements Processor {
    private final Extractor extractor;

    public ExtractorPageProcessor(final Extractor extractor) {
        this.extractor = extractor;
    }

    @Override
    public void process(final Page page) {
        if (extract(page)) {
            page.addTargets(extractLinks(page, extractor.getItemLinkRule()));
            extractPageFieldValue(page, extractor.getItemLinkRule(), extractor.getFields());
        } else {
            extractItemFieldValue(page, extractor.getFields());
        }
    }

    private void extractPageFieldValue(final Page page, final ItemLinkRule item, final List<FieldRule> fieldRules) {
        final String itemXpath = item.getItemXpath();
        final String linkXpath = item.getLinkXpath();
        final List<Selectable> selectables = page.getHtml().xpath(itemXpath).nodes();
        final List<ListFieldValue> result = new ArrayList<>(selectables.size());
        for (final Selectable selectable : selectables) {
            final ListFieldValue fieldValue = new ListFieldValue();
            for (final FieldRule fieldRule : fieldRules) {
                fieldValue.add(fieldRule.getName(), getSelectableValue(selectable, fieldRule));
            }
            fieldValue.setLinkId(link(selectable.xpath(linkXpath).get()));
            result.add(fieldValue);
        }
        page.setFieldValues(result);
    }

    public void extractItemFieldValue(final Page page, final List<FieldRule> fieldRules) {
        for (final FieldRule fieldRule : fieldRules) {
            final String name = fieldRule.getName();
            final Selectable selectable = getSelectable(page, fieldRule);
            final Object value = getSelectableValue(selectable, fieldRule);
            page.addFieldValue(name, value);
        }
    }

    private Selectable getSelectable(final Page page, final FieldRule fieldRule) {
        final String type = fieldRule.getType();
        if (Objects.equals(type, Constant.LINK)) {
            return page.getSelectable();
        }
        return page.getHtml();
    }

    private Object getSelectableValue(final Selectable selectable, final FieldRule fieldRule) {
        Selectable se = selectable;
        final List<String> xpaths = fieldRule.getXpaths();
        if (CollectionUtils.isNotEmpty(xpaths)) {
            for (final String xpath : xpaths) {
                if (se instanceof HtmlNode) {
                    se = se.xpath(xpath);
                }
                if (Objects.nonNull(se) && se.get() != null) {
                    break;
                }
            }
        }
        final List<String> regexs = fieldRule.getRegexs();
        if (CollectionUtils.isNotEmpty(regexs)) {
            for (final String regex : regexs) {
                se = se.regex(regex);
            }
        }
        return getValue(se, fieldRule);
    }

    private Object getValue(final Selectable selectable, final FieldRule fieldRule) {
        if (Objects.isNull(selectable)) {
            return null;
        }
        final Map<String, String> replaces = fieldRule.getReplaces();
        if (fieldRule.getMultiple()) {
            return selectable.all().stream().filter(StringUtils::isNotEmpty)
                    .map(String::trim).map(s -> replace(s, replaces))
                    .map(s -> transform(fieldRule, s)).collect(Collectors.toList());
        }
        return transform(fieldRule, replace(selectable.get(), replaces));
    }

    private String transform(final FieldRule fieldRule, final String s) {
        final String type = fieldRule.getType();
        if (Objects.equals(type, Constant.DATETIME)) {
            return DateUtil.parseFormatDateTime(s);
        }
        return s;
    }

    protected String replace(String s, final Map<String, String> replaces) {
        if (Objects.isNull(s)) {
            return null;
        }
        if (MapUtils.isEmpty(replaces)) {
            return s.trim();
        }
        for (final Map.Entry<String, String> entry : replaces.entrySet()) {
            s = replace(s, entry.getKey(), entry.getValue());
        }
        if (Objects.isNull(s)) {
            return null;
        }
        return s.trim();
    }

    protected String replace(final String s, final String regex, final String replacement) {
        if (Objects.isNull(s)) {
            return null;
        }
        return s.replaceAll(regex, replacement).trim();
    }

    private boolean extract(final Page page) {
        final Request request = page.getRequest();
        final PageLinkRule pageLinkRule = extractor.getPageLinkRule();
        final PlainText text = new PlainText(request.getUrl());
        return match(text, pageLinkRule);
    }

    private boolean match(final Selectable selectable, final PageLinkRule rule) {
        return selectable.regex(rule.getPattern()).match();
    }

    public List<Request> extractLinks(final Page page, final ItemLinkRule linkRule) {
        final Selectable selectable = page.getHtml();
        final String xpath = linkRule.getLinkXpath();
        if (StringUtils.isNotEmpty(xpath)) {
            final List<String> links = selectable.xpath(xpath).all();
            final List<Request> requests = new ArrayList<>();
            for (final String link : links) {
                final Request request = new Request(link(link));
                final HttpHeaders headers = page.getRequest().getHeaders();
                request.getHeaders().addAll(headers);
                requests.add(request);
            }
            return requests;
        }
        return Collections.emptyList();
    }

    private String link(final String link) {
        if (link.startsWith(Constant.HTTP)) {
            return link;
        }
        return extractor.getDomain() + link;
    }
}
