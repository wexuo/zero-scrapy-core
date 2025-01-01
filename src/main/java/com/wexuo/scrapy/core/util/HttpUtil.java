package com.wexuo.scrapy.core.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpUtil {
    private static final RestTemplate REST_TEMPLATE = RestTemplateBuilder.builder().build();
    private static final RestTemplate PROXY_REST_TEMPLATE = RestTemplateBuilder.builder().proxy().build();
    public static final String REGEX = "(?<=//|)((\\w)+\\.)+\\w+";
    public static final String HTTPS_PROTOCOL = "https://";
    public static final String HTTP_PROTOCOL = "http://";

    public static String getDomain(final String url) {
        if (StringUtils.isEmpty(url)) {
            throw new IllegalArgumentException("Invalid URL: " + url);
        }
        final Pattern p = Pattern.compile(REGEX);
        final Matcher matcher = p.matcher(url);
        if (matcher.find()) {
            final String domain = matcher.group();
            if (url.startsWith(HTTPS_PROTOCOL)) {
                return HTTPS_PROTOCOL + domain;
            }
            return HTTP_PROTOCOL + domain;
        }
        throw new IllegalArgumentException("Invalid URL: " + url);
    }

    public static RestTemplate getProxyRestTemplate() {
        return PROXY_REST_TEMPLATE;
    }

    public static RestTemplate getRestTemplate() {
        return REST_TEMPLATE;
    }
}
