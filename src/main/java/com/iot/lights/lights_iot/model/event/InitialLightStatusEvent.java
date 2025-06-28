package com.iot.lights.lights_iot.model.event;

import org.springframework.context.ApplicationEvent;

// Evento para notificar el estado inicial completo de las luces
public class InitialLightStatusEvent extends ApplicationEvent {
    private final String initialStatusJson;
    private final Object targetSession; // Podemos pasar la sesión específica a la que se debe enviar

    public InitialLightStatusEvent(Object source, String initialStatusJson, Object targetSession) {
        super(source);
        this.initialStatusJson = initialStatusJson;
        this.targetSession = targetSession;
    }

    public String getInitialStatusJson() {
        return initialStatusJson;
    }

    public Object getTargetSession() {
        return targetSession;
    }
}