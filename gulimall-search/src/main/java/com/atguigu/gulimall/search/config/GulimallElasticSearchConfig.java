package com.atguigu.gulimall.search.config;


import org.apache.http.HttpHost;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GulimallElasticSearchConfig {

    public static final RequestOptions COMMON_OPTIONS;

    static {
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
        COMMON_OPTIONS = builder.build();
    }

    @Value("${spring.cloud.elasticsearch.host}")
    private String host;

    @Value("${spring.cloud.elasticsearch.port}")
    private Integer port;

    @Value("${spring.cloud.elasticsearch.scheme}")
    private String scheme;


    @Bean
    public RestHighLevelClient esRestClient() {
        RestClientBuilder builder = null;
        builder = RestClient.builder(new HttpHost(host, port, scheme));
        RestHighLevelClient client = new RestHighLevelClient(builder);
        return client;
    }
}
