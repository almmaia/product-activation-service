package com.deroyque.activation.config;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import java.net.http.HttpClient;
@Configuration
@EnableConfigurationProperties(ProviderProperties.class)
public class HttpClientConfig {
    @Bean RestClient providerRestClient(ProviderProperties properties) {
        var factory = new JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(properties.connectTimeout()).build());
        factory.setReadTimeout(properties.readTimeout());
        return RestClient.builder().baseUrl(properties.baseUrl()).requestFactory(factory).build();
    }
}
