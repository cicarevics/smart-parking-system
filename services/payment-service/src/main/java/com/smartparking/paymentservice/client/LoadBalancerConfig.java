package com.smartparking.paymentservice.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestClient;

@Configuration
public class LoadBalancerConfig {

    private final ObjectMapper objectMapper;

    public LoadBalancerConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // @LoadBalanced makes this builder resolve lb://RESERVATION-SERVICE
    // against Eureka instead of DNS -- same mechanism the gateway uses for
    // its routes, just called from application code instead of config.
    //
    // RestClient.builder() on its own wires up a fresh, DEFAULT Jackson
    // ObjectMapper -- NOT the app-wide bean Boot builds from
    // spring.jackson.property-naming-strategy: SNAKE_CASE. Swapping in the
    // injected ObjectMapper here is what makes request/response bodies
    // sent through this client actually match reservation-service's wire
    // format (e.g. additional_minutes, not additionalMinutes).
    @Bean
    @LoadBalanced
    public RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder()
                .messageConverters(converters -> {
                    converters.removeIf(c -> c instanceof MappingJackson2HttpMessageConverter);
                    converters.add(new MappingJackson2HttpMessageConverter(objectMapper));
                });
    }
}
