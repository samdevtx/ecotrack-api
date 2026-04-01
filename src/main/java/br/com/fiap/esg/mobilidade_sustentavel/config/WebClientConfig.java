package br.com.fiap.esg.mobilidade_sustentavel.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${spring.secret.ai.key}")
    private String hfApiKey;

    @Bean
    public WebClient aiWebClient(WebClient.Builder builder) {
        if (hfApiKey == null || hfApiKey.isEmpty() || hfApiKey.startsWith("hf")) {
            System.err.println("WARNING: Hugging Face API Key is not configured or is using a default placeholder. AI features may not work.");
        }
        
        return builder
            .baseUrl("https://api-inference.huggingface.co")
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + hfApiKey)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }
} 