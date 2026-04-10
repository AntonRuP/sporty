package com.sporty.controller.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum EventStatus {
    LIVE,
    NOT_LIVE;

    @JsonCreator
    public static EventStatus fromValue(String value) {
        return EventStatus.valueOf(value.trim().replace('-', '_').toUpperCase());
    }
}

