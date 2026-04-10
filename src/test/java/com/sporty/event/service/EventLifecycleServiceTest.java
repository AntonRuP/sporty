package com.sporty.event.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.sporty.configuration.AppProperties;
import com.sporty.controller.model.EventStatus;
import com.sporty.controller.model.EventStatusRequest;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Delayed;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.sporty.service.EventPollingService;
import com.sporty.service.EventLifecycleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;

class EventLifecycleServiceTest {

    private RecordingTaskScheduler taskScheduler;
    private EventPollingService eventPollingService;
    private EventLifecycleService service;

    @BeforeEach
    void setUp() {
        taskScheduler = new RecordingTaskScheduler();
        eventPollingService = mock(EventPollingService.class);
        service = new EventLifecycleService(
                eventPollingService,
                taskScheduler,
                new AppProperties(
                        new AppProperties.Polling(10_000),
                        new AppProperties.Kafka("live-score-updates"))
        );
    }

    @Test
    void shouldSchedulePollingOnlyOnceForRepeatedLiveUpdates() {
        var first = service.updateStatus(new EventStatusRequest("1234", EventStatus.LIVE));
        var second = service.updateStatus(new EventStatusRequest("1234", EventStatus.LIVE));

        assertThat(first.pollingActive()).isTrue();
        assertThat(second.pollingActive()).isTrue();
        assertThat(taskScheduler.scheduleCalls).isEqualTo(1);
    }

    @Test
    void shouldCancelPollingWhenMarkedNotLive() {
        service.updateStatus(new EventStatusRequest("1234", EventStatus.LIVE));
        var response = service.updateStatus(new EventStatusRequest("1234", EventStatus.NOT_LIVE));

        assertThat(response.pollingActive()).isFalse();
        assertThat(taskScheduler.lastFuture.cancelled).isTrue();
    }

    static class RecordingTaskScheduler implements TaskScheduler {
        int scheduleCalls;
        TestScheduledFuture lastFuture;

        @Override
        public ScheduledFuture<?> schedule(Runnable task, Trigger trigger) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ScheduledFuture<?> schedule(Runnable task, Instant startTime) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Instant startTime, Duration period) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Duration period) {
            scheduleCalls++;
            lastFuture = new TestScheduledFuture();
            return lastFuture;
        }

        @Override
        public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Instant startTime, Duration delay) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Duration delay) {
            throw new UnsupportedOperationException();
        }
    }

    static class TestScheduledFuture implements ScheduledFuture<Object> {
        boolean cancelled;

        @Override
        public long getDelay(TimeUnit unit) {
            return 0;
        }

        @Override
        public int compareTo(Delayed other) {
            return 0;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            cancelled = true;
            return true;
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public boolean isDone() {
            return false;
        }

        @Override
        public Object get() {
            return null;
        }

        @Override
        public Object get(long timeout, TimeUnit unit) {
            return null;
        }
    }
}
