package com.wexuo.scrapy.core;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class Site {
    private String domain;
    private String userAgent;
    private String charset;

    private int delay = 100;
    private int maxRetry = 0;
    private int timeout = 5000;

    private final Map<String, String> headers = new HashMap<>();
    private final Map<String, String> cookies = new HashMap<>();

    public static Site me() {
        return new Site();
    }

    public Site setDomain(final String domain) {
        this.domain = domain;
        return this;
    }

    public Site setUserAgent(final String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    public Site setCharset(final String charset) {
        this.charset = charset;
        return this;
    }

    public Site setMaxRetry(final int maxRetry) {
        this.maxRetry = maxRetry;
        return this;
    }

    public Site setDelay(final int delay) {
        this.delay = delay;
        return this;
    }


    public Site setTimeout(final int timeout) {
        this.timeout = timeout;
        return this;
    }

    public Site addCookie(final String name, final String value) {
        cookies.put(name, value);
        return this;
    }

    public Site addHeader(final String key, final String value) {
        headers.put(key, value);
        return this;
    }
}
