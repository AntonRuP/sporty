package com.sporty.service;

import com.sporty.configuration.AppProperties;
import com.sporty.controller.model.EventStatus;
import com.sporty.controller.model.EventStatusRequest;
import com.sporty.controller.model.EventStatusResponse;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EventLifecycleService {

    private final ConcurrentMap<String, ManagedEvent> events = new ConcurrentHashMap<>();
    private final EventPollingService eventPollingService;
    private final TaskScheduler taskScheduler;

    private final long intervalMs;

    public EventLifecycleService(EventPollingService eventPollingService, TaskScheduler taskScheduler, AppProperties appProperties) {
        this.eventPollingService = eventPollingService;
        this.taskScheduler = taskScheduler;
        this.intervalMs = appProperties.polling().intervalMs();
    }

    public EventStatusResponse updateStatus(EventStatusRequest request) {
        ManagedEvent updated = events.compute(request.eventId(), (eventId, current) -> {
            ManagedEvent managedEvent = current == null ? ManagedEvent.notLive(eventId) : current;
            return switch (request.status()) {
                case LIVE -> startPolling(managedEvent);
                case NOT_LIVE -> stopPolling(managedEvent);
            };
        });

        log.info("Event {} status changed to {}", updated.eventId(), updated.status());
        return new EventStatusResponse(updated.eventId(), updated.status(), updated.isPollingActive());
    }

    ManagedEvent startPolling(ManagedEvent current) {
        if (current.isPollingActive()) {
            return current.withStatus(EventStatus.LIVE);
        }

        ScheduledFuture<?> future = taskScheduler.scheduleAtFixedRate(
                () -> eventPollingService.poll(current.eventId()),
                Duration.ofMillis(intervalMs)
        );
        log.info("Started polling for event {}", current.eventId());
        return current.withStatus(EventStatus.LIVE).withFuture(future);
    }

    ManagedEvent stopPolling(ManagedEvent current) {
        if (current.future() != null) {
            current.future().cancel(false);
            log.info("Stopped polling for event {}", current.eventId());
        }
        return current.withStatus(EventStatus.NOT_LIVE).withFuture(null);
    }

    record ManagedEvent(String eventId, EventStatus status, ScheduledFuture<?> future) {
        static ManagedEvent notLive(String eventId) {
            return new ManagedEvent(eventId, EventStatus.NOT_LIVE, null);
        }

        ManagedEvent withStatus(EventStatus newStatus) {
            return new ManagedEvent(eventId, newStatus, future);
        }

        ManagedEvent withFuture(ScheduledFuture<?> newFuture) {
            return new ManagedEvent(eventId, status, newFuture);
        }

        boolean isPollingActive() {
            return future != null && !future.isCancelled();
        }
    }
}
