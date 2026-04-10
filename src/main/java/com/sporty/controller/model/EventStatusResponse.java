package com.sporty.controller.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Event status update response")
public record EventStatusResponse(
        @Schema(example = "1234") String eventId,
        @Schema(example = "LIVE") EventStatus status,
        @Schema(example = "true") boolean pollingActive
) {
}

