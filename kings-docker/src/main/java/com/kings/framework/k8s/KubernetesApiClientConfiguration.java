package com.kings.framework.k8s;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.util.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class KubernetesApiClientConfiguration {
    @Value("${spring.cloud.kubernetes.client.master-url}")
    private String master;
    @Value("${spring.cloud.kubernetes.client.oauth-token}")
    private String token;

    @Bean
    public ApiClient client() {
        ApiClient client = Config.fromToken(master, token, false);
        client.setLenientOnJson(true);
        client.setReadTimeout(0);
        io.kubernetes.client.openapi.Configuration.setDefaultApiClient(client);
        return client;
    }
}
