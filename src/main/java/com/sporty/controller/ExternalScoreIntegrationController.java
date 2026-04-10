package com.sporty.controller;

import com.sporty.controller.model.ExternalScoreResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ExternalScoreIntegrationController {

    private static final String MOCK_SCORE = "2:1";

    public ExternalScoreResponse fetchScore(String eventId) {
        log.debug("Returning mocked score for event {}", eventId);
        return new ExternalScoreResponse(eventId, MOCK_SCORE);
    }
}
