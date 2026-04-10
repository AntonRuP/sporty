package com.sporty.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI sportyOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sporty Status API")
                        .description("Test sport event")
                        .version("v1")
                        .contact(new Contact().name("Sporty Prototype")));
    }
}
