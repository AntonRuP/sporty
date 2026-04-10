package com.sporty.controller.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Event status update request")
public record EventStatusRequest(

        @Schema(description = "Unique event identifier", example = "1234")
        @NotBlank String eventId,

        @Schema(description = "Event status", allowableValues = {"LIVE", "NOT_LIVE"})
        @NotNull EventStatus status

) {
}

