package com.syos.shared;

import java.io.Serializable;

public class PushEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String eventType;
    private final Object payload;

    public PushEvent(String eventType, Object payload) {
        this.eventType = eventType;
        this.payload = payload;
    }

    public String getEventType() {
        return eventType;
    }

    public Object getPayload() {
        return payload;
    }
}
