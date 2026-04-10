package com.sporty.controller;

import com.sporty.controller.model.EventStatusRequest;
import com.sporty.controller.model.EventStatusResponse;
import com.sporty.service.EventLifecycleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/events")
@Tag(name = "Events")
@RequiredArgsConstructor
public class EventStatusController {

    private final EventLifecycleService eventLifecycleService;

    @PostMapping("/status")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Update event live status",
            description = "Marks an event as live or not live and starts or stops polling.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Status applied",
                            content = @Content(schema = @Schema(implementation = EventStatusResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Validation error")
            }
    )
    public EventStatusResponse updateStatus(@Valid @RequestBody EventStatusRequest request) {
        return eventLifecycleService.updateStatus(request);
    }
}

