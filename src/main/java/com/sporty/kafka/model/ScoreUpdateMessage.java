package com.sporty.kafka.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Message published to Kafka when a live score is fetched")
public record ScoreUpdateMessage(
        String eventId,
        String score
) {
}

