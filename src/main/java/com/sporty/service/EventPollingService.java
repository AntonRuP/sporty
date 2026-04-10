package com.sporty.service;

import com.sporty.controller.ExternalScoreIntegrationController;
import com.sporty.kafka.KafkaScorePublisher;
import com.sporty.kafka.model.ScoreUpdateMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventPollingService {

    private final ExternalScoreIntegrationController externalScoreIntegrationController;
    private final KafkaScorePublisher kafkaScorePublisher;

    public void poll(String eventId) {
        try {
            var score = externalScoreIntegrationController.fetchScore(eventId);
            ScoreUpdateMessage message = new ScoreUpdateMessage(score.eventId(), score.currentScore());
            kafkaScorePublisher.publish(message);
            log.info("Processed live update for event {} with score {}", eventId, score.currentScore());
        } catch (Exception exception) {
            log.error("Polling failed for event {}", eventId, exception);
        }
    }
}
