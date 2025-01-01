package com.wexuo.scrapy.core.util;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RestTemplateBuilder {

    private int connectTimeout = 50000;

    private int readTimeout = 50000;

    private Proxy proxy = Proxy.NO_PROXY;

    private List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();

    public static RestTemplateBuilder builder() {
        return new RestTemplateBuilder();
    }

    public RestTemplateBuilder proxy(final Proxy proxy) {
        this.proxy = proxy;
        return this;
    }

    public RestTemplateBuilder timeout(final int connectTimeout, final int readTimeout) {
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        return this;
    }

    public RestTemplateBuilder proxy(String proxy) {
        final String httpProxy = System.getenv("HTTP_PROXY");
        if (Objects.nonNull(httpProxy)) {
            proxy = httpProxy;
        }
        if (Objects.isNull(proxy)) {
            return this;
        }
        final Integer idx = proxy.lastIndexOf(":");
        final String hostname = proxy.substring(0, idx);
        final String port = proxy.substring(idx + 1);
        this.proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(hostname, Integer.parseInt(port)));
        return this;
    }

    public RestTemplateBuilder proxy() {
        final String proxy = System.getenv("HTTP_PROXY");
        if (Objects.isNull(proxy)) {
            return this;
        }
        final Integer idx = proxy.lastIndexOf(":");
        final String hostname = proxy.substring(0, idx);
        final String port = proxy.substring(idx + 1);
        this.proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(hostname, Integer.parseInt(port)));
        return this;
    }

    public RestTemplateBuilder messageConverters(final List<HttpMessageConverter<?>> messageConverters) {
        this.messageConverters = messageConverters;
        return this;
    }

    public RestTemplate build() {
        final RestTemplate restTemplate = new RestTemplate();
        final SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(this.connectTimeout);
        factory.setReadTimeout(this.readTimeout);
        factory.setProxy(this.proxy);
        restTemplate.setRequestFactory(factory);
        restTemplate.getMessageConverters().addAll(this.messageConverters);
        return restTemplate;
    }
}

