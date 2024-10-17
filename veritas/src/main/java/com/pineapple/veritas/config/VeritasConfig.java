package com.pineapple.veritas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Represents a bean definition source.
 */
@Configuration
public class VeritasConfig {
  @Bean
  public WebClient.Builder webClientBuilder() {
    return WebClient.builder();
  }
}
