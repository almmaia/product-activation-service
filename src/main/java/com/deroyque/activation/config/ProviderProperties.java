package com.deroyque.activation.config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.time.Duration;
@ConfigurationProperties(prefix = "provider")
public record ProviderProperties(String baseUrl, Duration connectTimeout, Duration readTimeout) { }
