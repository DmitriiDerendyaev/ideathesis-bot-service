package ru.derendyaev.ideathesis_bot_service.config;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;

@Configuration
public class WebClientConfig {

    @Value("${app.values.auth.service-url}")
    private String authServiceUrl;

    @Value("${app.values.users.service-url}")
    private String usersServiceUrl;

    @Value("${app.values.topic.service-url}")
    private String topicServiceUrl;

    @Bean("authWebClient")
    public WebClient authWebClient() throws SSLException {
        return createWebClient(authServiceUrl);
    }

    @Bean("usersWebClient")
    public WebClient usersWebClient() throws SSLException {
        return createWebClient(usersServiceUrl);
    }

    @Bean("topicWebClient")
    public WebClient topicWebClient() throws SSLException {
        return createWebClient(topicServiceUrl);
    }

    private WebClient createWebClient(String baseUrl) throws SSLException {
        SslContext sslContext = SslContextBuilder
                .forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();

        HttpClient httpClient = HttpClient.create()
                .secure(t -> t.sslContext(sslContext));

        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}