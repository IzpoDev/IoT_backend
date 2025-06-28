package com.iot.lights.lights_iot.model.event;

import org.springframework.context.ApplicationEvent;

// Evento para notificar cambios de estado de una luz individual (alarma)
public class LightStatusUpdateEvent extends ApplicationEvent {
    private final String messageJson;

    public LightStatusUpdateEvent(Object source, String messageJson) {
        super(source);
        this.messageJson = messageJson;
    }

    public String getMessageJson() {
        return messageJson;
    }
}