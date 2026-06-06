package com.tce.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${app.api.base-url:/}")
    private String apiBaseUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Temporal Contract Engine API")
                        .version("1.0")
                        .description("API documentation for the Temporal Contract Engine, a deterministic backend platform that manages delayed, rule-bound commitments.")
                        .contact(new Contact()
                                .name("TCE Team")
                                .email("support@tce.com")
                                .url("https://tce-portfolio.example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server().url(apiBaseUrl).description("Target API Server")
                ));
    }
}
