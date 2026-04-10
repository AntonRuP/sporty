package com.sporty;

import com.sporty.controller.model.EventStatus;
import com.sporty.controller.model.EventStatusRequest;
import com.sporty.kafka.model.ScoreUpdateMessage;
import com.sporty.service.EventLifecycleService;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestConstructor;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EmbeddedKafka(partitions = 1, topics = "live-score-updates")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class LiveScoreFlowIntegrationTest {

    private final EventLifecycleService eventLifecycleService;

    LiveScoreFlowIntegrationTest(EventLifecycleService eventLifecycleService) {
        this.eventLifecycleService = eventLifecycleService;
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("app.polling.interval-ms", () -> "1000");
        registry.add("spring.kafka.bootstrap-servers", () -> System.getProperty("spring.embedded.kafka.brokers"));
    }

    @Test
    void shouldPublishKafkaMessageWhenLiveEventIsPolled() {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(
                System.getProperty("spring.embedded.kafka.brokers"),
                "sporty-test",
                "true"
        );
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        JsonDeserializer<ScoreUpdateMessage> valueDeserializer = new JsonDeserializer<>(ScoreUpdateMessage.class);
        valueDeserializer.addTrustedPackages("*");

        var consumer = new DefaultKafkaConsumerFactory<>(
                consumerProps,
                new StringDeserializer(),
                valueDeserializer
        ).createConsumer();

        consumer.subscribe(List.of("live-score-updates"));

        eventLifecycleService.updateStatus(new EventStatusRequest("1234", EventStatus.LIVE));

        ConsumerRecord<String, ScoreUpdateMessage> record = KafkaTestUtils.getSingleRecord(
                consumer,
                "live-score-updates",
                Duration.ofSeconds(15)
        );

        assertThat(record.value().eventId()).isEqualTo("1234");
        assertThat(record.value().score()).isEqualTo("2:1");

        eventLifecycleService.updateStatus(new EventStatusRequest("1234", EventStatus.NOT_LIVE));
        consumer.close();
    }
}
