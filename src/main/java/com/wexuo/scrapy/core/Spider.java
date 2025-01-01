package com.wexuo.scrapy.core;

import com.wexuo.scrapy.core.downloader.Downloader;
import com.wexuo.scrapy.core.pipeline.CollectorPipeline;
import com.wexuo.scrapy.core.pipeline.Pipeline;
import com.wexuo.scrapy.core.processor.Processor;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class Spider implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(Spider.class);
    private final Site site;
    private final List<Pipeline> pipelines;
    private final BlockingQueue<Request> requests;
    private Downloader downloader;
    private Processor processor;

    private long timeout = 30;

    private int thread = 5;

    public Spider(final Site site) {
        this.site = site;
        this.pipelines = new ArrayList<>();
        this.requests = new LinkedBlockingQueue<>();
    }

    public static Spider create(final Site site) {
        return new Spider(site);
    }

    public Spider timeout(final long timeout) {
        this.timeout = timeout;
        return this;
    }

    public Spider thread(final int thread) {
        this.thread = thread;
        return this;
    }

    public Spider setDownloader(final Downloader downloader) {
        this.downloader = downloader;
        return this;
    }

    public Spider setProcessor(final Processor processor) {
        this.processor = processor;
        return this;
    }

    public Spider addPipeline(final Pipeline pipeline) {
        this.pipelines.add(pipeline);
        return this;
    }

    public Spider addRequest(final String url) {
        if (url.startsWith(Constant.HTTP)) {
            addRequest(new Request(url));
        } else {
            addRequest(new Request(site.getDomain() + url));
        }
        return this;
    }

    public void addRequest(final Request request) {
        final Map<String, String> headers = site.getHeaders();
        final Map<String, String> cookies = site.getCookies();
        headers.forEach(request::addHeader);
        cookies.forEach(request::addHeader);
        request.addHeader(Constant.USER_AGENT, site.getUserAgent());
        final String domain = site.getDomain();
        request.addHeader(Constant.REFERER, domain);
        request.addHeader(Constant.HOST, domain);
        this.requests.add(request);
    }

    @Override
    public void run() {
        check();
        final ExecutorService service = Executors.newFixedThreadPool(thread);
        final List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (final Request request : requests) {
            LOG.info("request start: {}", request.getUrl());
            final List<Request> targets = tryGetPage(request);
            if (CollectionUtils.isEmpty(targets)) {
                continue;
            }
            futures.addAll(targets.stream().map(target -> CompletableFuture.runAsync(() -> {
                try {
                    LOG.info("request start: {}", target.getUrl());
                    doWork(target);
                    onSuccess(target);
                    LOG.info("request success: {}", target.getUrl());
                } catch (final Exception e) {
                    onError(target, e);
                    LOG.error("process request error: {}", target.getUrl(), e);
                }
            }, service)).collect(Collectors.toList()));
        }
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0])).get(timeout, TimeUnit.SECONDS);
        } catch (final InterruptedException | ExecutionException | TimeoutException e) {
            LOG.error("request error: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            close();
            service.shutdown();
        }
    }

    private void check() {
        if (site == null) {
            LOG.error("site can not be null");
            throw new RuntimeException("site can not be null");
        }
        if (downloader == null) {
            LOG.error("downloader can not be null");
            throw new RuntimeException("downloader can not be null");
        }
        if (processor == null) {
            LOG.error("processor can not be null");
            throw new RuntimeException("processor can not be null");
        }
        if (pipelines.isEmpty()) {
            LOG.error("pipelines can not be empty");
            throw new RuntimeException("pipelines can not be empty");
        }
    }

    private List<Request> tryGetPage(final Request request) {
        final Page page = downloader.download(request);
        if (page.success()) {
            processor.process(page);
            for (final Pipeline pipeline : pipelines) {
                pipeline.process(page);
            }
            return page.getTargets();
        }
        throw new RuntimeException("download fail");
    }

    private void doWork(final Request request) {
        final Page page = downloader.download(request);
        if (page.success()) {
            onDownloadSuccess(page);
        }
        onDownloaderFail(request);
    }

    protected void onSuccess(final Request request) {
    }

    protected void onError(final Request request, final Exception e) {
    }

    public void close() {
        destroyEach(downloader);
        destroyEach(processor);
        for (final Pipeline pipeline : pipelines) {
            destroyEach(pipeline);
        }
    }

    private void destroyEach(final Object object) {
        if (object instanceof Closeable) {
            try {
                ((Closeable) object).close();
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void onDownloaderFail(final Request request) {
        doCycleRetry(request);
    }

    private void doCycleRetry(final Request request) {

    }

    private void onDownloadSuccess(final Page page) {
        processor.process(page);
        for (final Pipeline pipeline : pipelines) {
            pipeline.process(page);
        }
        addRequests(page);
    }

    protected void addRequests(final Page page) {
        final List<Request> requests = page.getTargets();
        if (CollectionUtils.isEmpty(requests)) {
            return;
        }
        requests.forEach(this::addRequest);
    }

    public <T> List<T> getResult(final String url, final Class<T> clazz) {
        this.requests.clear();
        this.pipelines.clear();
        this.addRequest(new Request(url));
        final CollectorPipeline<T> pipeline = new CollectorPipeline<>();
        this.addPipeline(pipeline).run();
        return pipeline.getList(clazz);
    }
}
