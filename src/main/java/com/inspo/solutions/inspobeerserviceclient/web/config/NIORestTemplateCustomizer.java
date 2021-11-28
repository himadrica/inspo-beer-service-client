package com.inspo.solutions.inspobeerserviceclient.web.config;

import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.reactor.IOReactorException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsAsyncClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

//@Component
public class NIORestTemplateCustomizer implements RestTemplateCustomizer {
    private final Integer nioMaxTotal;
    private final Integer nioMaxPerRoute;
    private final Integer nioThreadCount;
    private final Integer connectionRequestTimeout;
    private final Integer socketTimeout;

    public NIORestTemplateCustomizer(@Value("${inspo.httpclient.nioMaxTotal}") Integer nioMaxTotal,
                                        @Value("${inspo.httpclient.nioMaxPerRoute}") Integer nioMaxPerRoute,
                                            @Value("${inspo.httpclient.nioThreadCount}") Integer nioThreadCount,
                                                @Value("${inspo.httpclient.connectionrequesttimeout}")Integer connectionRequestTimeout,
                                                    @Value("${inspo.httpclient.sockettimeout}")Integer socketTimeout) {
        this.nioMaxTotal = nioMaxTotal;
        this.nioMaxPerRoute = nioMaxPerRoute;
        this.nioThreadCount = nioThreadCount;
        this.connectionRequestTimeout = connectionRequestTimeout;
        this.socketTimeout = socketTimeout;
    }

    public ClientHttpRequestFactory clientHttpRequestFactory() throws IOReactorException {

        final DefaultConnectingIOReactor ioreactor = new DefaultConnectingIOReactor(IOReactorConfig.custom().
                setConnectTimeout(3000).
                setIoThreadCount(connectionRequestTimeout).
                setSoTimeout(socketTimeout).
                build());

        final PoolingNHttpClientConnectionManager connectionManager = new PoolingNHttpClientConnectionManager(ioreactor);
        connectionManager.setDefaultMaxPerRoute(nioMaxPerRoute);
        connectionManager.setMaxTotal(nioMaxTotal);

        CloseableHttpAsyncClient httpAsyncClient = HttpAsyncClients.custom()
                .setConnectionManager(connectionManager)
                .build();

        return new HttpComponentsAsyncClientHttpRequestFactory(httpAsyncClient);

    }

    @Override
    public void customize(RestTemplate restTemplate) {
        try {
            restTemplate.setRequestFactory(clientHttpRequestFactory());
        } catch (IOReactorException e) {
            e.printStackTrace();
        }
    }
}
