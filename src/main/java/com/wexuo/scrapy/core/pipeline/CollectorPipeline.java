package com.wexuo.scrapy.core.pipeline;

import com.wexuo.scrapy.core.value.ListFieldValue;
import com.wexuo.scrapy.core.Page;
import com.wexuo.scrapy.core.Request;
import lombok.Getter;
import org.apache.commons.collections4.MapUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 列表内容收集器
 */
@Getter
public class CollectorPipeline<T> implements Pipeline {

    private final Map<String, ListFieldValue> linkIdMap = new LinkedHashMap<>();

    @Override
    public synchronized void process(final Page page) {
        final Request request = page.getRequest();
        final String parentId = request.getParentId();
        if (parentId == null) {
            final List<ListFieldValue> fieldValues = page.getFieldValues();
            for (final ListFieldValue fieldValue : fieldValues) {
                final String linkId = fieldValue.getLinkId();
                linkIdMap.put(linkId, fieldValue);
            }
        } else {
            final ListFieldValue fieldValue = linkIdMap.get(request.getUrl());
            if (fieldValue != null) {
                fieldValue.merge(page.getFieldValue());
            } else {
                linkIdMap.put(request.getUrl(), page.getFieldValue());
            }
        }
    }

    public List<T> getList(final Class<T> clazz) {
        if (MapUtils.isEmpty(linkIdMap)) {
            return Collections.emptyList();
        }
        return linkIdMap.values().stream().map(fieldValue -> fieldValue.toObject(clazz)).collect(Collectors.toList());
    }
}