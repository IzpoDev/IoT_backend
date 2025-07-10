package com.iot.lights.lights_iot.handler;

import com.iot.lights.lights_iot.model.event.InitialLightStatusEvent;
import com.iot.lights.lights_iot.model.event.LightStatusUpdateEvent;
import com.iot.lights.lights_iot.service.LightStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // <-- AÑADIR
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component
@Slf4j // <-- AÑADIR ANOTACIÓN
@RequiredArgsConstructor // <-- MEJORA: Usa esto para la inyección
public class LightStatusWebSocketHandler extends TextWebSocketHandler {

    private final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());
    private final LightStatusService lightStatusService;

    // El constructor es generado por @RequiredArgsConstructor, puedes borrar el que tenías.

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        // MEJORA: Usar el logger
        log.info("Nueva sesión WebSocket conectada: {}. Total sesiones: {}", session.getId(), sessions.size());
        lightStatusService.sendCurrentLightStatesToClient(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String receivedJson = message.getPayload();
        // MEJORA: Usar el logger
        log.info("Mensaje JSON recibido del cliente {}: {}", session.getId(), receivedJson);
        lightStatusService.processIncomingWebSocketMessage(receivedJson);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        // MEJORA: Usar el logger
        log.info("Sesión WebSocket cerrada: {} con estado {}. Restantes: {}", session.getId(), status, sessions.size());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        // MEJORA: Usar el logger para errores
        log.error("Error de transporte en sesión {}: {}", session.getId(), exception.getMessage(), exception);
    }

    // --- Métodos de Escucha de Eventos (Event Listeners) ---

    @EventListener
    public void handleLightStatusUpdateEvent(LightStatusUpdateEvent event) {
        log.info("Recibido evento LightStatusUpdateEvent, retransmitiendo: {}", event.getMessageJson());
        broadcastMessage(event.getMessageJson());
    }

    @EventListener
    public void handleInitialLightStatusEvent(InitialLightStatusEvent event) {
        log.info("Recibido evento InitialLightStatusEvent para sesión: {}", event.getTargetSession().toString());
        if (event.getTargetSession() instanceof WebSocketSession targetSession && targetSession.isOpen()) {
            sendMessageToSession(targetSession, event.getInitialStatusJson());
        } else {
            log.warn("No se pudo enviar estado inicial a sesión específica o la sesión es inválida: {}", event.getTargetSession());
        }
    }

    public void broadcastMessage(String message) {
        sessions.forEach(session -> sendMessageToSession(session, message));
    }

    public void sendMessageToSession(WebSocketSession session, String message) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(message));
            }
        } catch (IOException e) {
            // MEJORA: Usar el logger
            log.error("Error al enviar mensaje a sesión {}: {}", session.getId(), e.getMessage());
        }
    }
}