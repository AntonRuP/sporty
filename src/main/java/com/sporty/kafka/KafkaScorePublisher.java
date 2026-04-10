package com.sporty.kafka;

import com.sporty.configuration.AppProperties;
import com.sporty.exception.MessagePublishException;
import com.sporty.kafka.model.ScoreUpdateMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaScorePublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RetryTemplate retryTemplate;
    private final AppProperties appProperties;

    public void publish(ScoreUpdateMessage message) {
        try {
            retryTemplate.execute(context -> {
                log.debug("Publishing event {} to Kafka attempt {}", message.eventId(), context.getRetryCount() + 1);
                kafkaTemplate.send(appProperties.kafka().topic(), message.eventId(), message)
                        .get(5, TimeUnit.SECONDS);
                return null;
            });
            log.info("Published event {} to topic {}", message.eventId(), appProperties.kafka().topic());
        } catch (Exception exception) {
            throw new MessagePublishException("Failed to publish message for event " + message.eventId(), exception);
        }
    }
}
