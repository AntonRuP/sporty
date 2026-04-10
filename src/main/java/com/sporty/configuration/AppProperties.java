package com.sporty.configuration;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        @Valid Polling polling,
        @Valid Kafka kafka
) {

    public record Polling(@Min(1000) long intervalMs) {
    }

    public record Kafka(@NotBlank String topic) {
    }
}
