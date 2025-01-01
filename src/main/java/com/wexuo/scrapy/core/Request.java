package com.wexuo.scrapy.core;

import com.wexuo.scrapy.core.util.HttpUtil;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

@Getter
public class Request {

    private final String url;

    private final HttpMethod method = HttpMethod.GET;

    private final HttpHeaders headers = new HttpHeaders();

    @Setter
    private String parentId;

    public Request(final String url) {
        this.url = url;
    }

    public void addHeader(final String name, final String value) {
        headers.add(name, value);
    }

    public String getDomain() {
        return HttpUtil.getDomain(url);
    }
}
