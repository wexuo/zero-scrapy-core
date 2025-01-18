package com.wexuo.scrapy.core.downloader;

import com.wexuo.scrapy.core.Page;
import com.wexuo.scrapy.core.Request;
import com.wexuo.scrapy.core.util.HttpUtil;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * 默认下载器，使用 RestTemplate 请求网页内容
 *
 * @see org.springframework.web.client.RestTemplate
 */
public class RestTemplateDownloader extends AbstractDownloader {

    private final RestTemplate restTemplate;

    public RestTemplateDownloader(final boolean proxy) {
        this(proxy, null);
    }

    public RestTemplateDownloader(final boolean proxy, final DownloaderFilter filter) {
        super(filter);
        this.restTemplate = proxy ? HttpUtil.getProxyRestTemplate() : HttpUtil.getRestTemplate();
    }

    @Override
    public Page download(final Request request, final Page page) {
        final String url = request.getUrl();
        final HttpMethod method = request.getMethod();
        final HttpHeaders headers = request.getHeaders();
        final HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(headers);
        final ResponseEntity<String> entity = restTemplate.exchange(url, method, httpEntity, String.class);
        final HttpStatus statusCode = entity.getStatusCode();
        page.setStatus(statusCode);
        page.setHeaders(entity.getHeaders());
        if (statusCode.is2xxSuccessful()) {
            page.setRawText(entity.getBody());
        }
        return page;
    }
}
