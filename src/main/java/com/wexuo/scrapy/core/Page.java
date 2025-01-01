package com.wexuo.scrapy.core;

import com.wexuo.scrapy.core.value.ListFieldValue;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.Json;
import us.codecraft.webmagic.selector.PlainText;
import us.codecraft.webmagic.selector.Selectable;

import java.util.LinkedList;
import java.util.List;

@Getter
public class Page {

    private Html html;

    private Json json;

    @Setter
    private String rawText;

    @Setter
    private HttpHeaders headers;

    @Setter
    private HttpStatus status;

    private final Request request;

    private final List<Request> targets;

    @Setter
    private ListFieldValue fieldValue;

    @Setter
    private List<ListFieldValue> fieldValues;

    public Page(final Request request) {
        this.request = request;
        this.targets = new LinkedList<>();
    }

    public String getId() {
        return request.getUrl();
    }

    public boolean success() {
        return status.is2xxSuccessful();
    }

    public Html getHtml() {
        if (html == null) {
            html = new Html(rawText, request.getUrl());
        }
        return html;
    }

    public Json getJson() {
        if (json == null) {
            json = new Json(rawText);
        }
        return json;
    }

    public Selectable getSelectable() {
        return new PlainText(request.getUrl());
    }

    public void addTargets(final List<Request> requests) {
        if (CollectionUtils.isEmpty(requests)) {
            return;
        }
        requests.forEach(request -> request.setParentId(getId()));
        targets.addAll(requests);
    }

    public void addFieldValue(final String name, final Object value) {
        if (fieldValue == null) {
            fieldValue = new ListFieldValue();
        }
        fieldValue.add(name, value);
    }
}
