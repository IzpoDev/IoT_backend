package com.iot.lights.lights_iot.handler;

import com.iot.lights.lights_iot.model.event.InitialLightStatusEvent; // Nueva importación
import com.iot.lights.lights_iot.model.event.LightStatusUpdateEvent;   // Nueva importación
import com.iot.lights.lights_iot.service.LightStatusService; // Mantener la importación porque lo usas en handleTextMessage
import org.springframework.context.event.EventListener; // Nueva importación para @EventListener
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
public class LightStatusWebSocketHandler extends TextWebSocketHandler {

    private final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());
    // Mantener la referencia al service aquí porque el handler es el punto de entrada para los mensajes
    // y DELEGA al service. Ya no hay inyección bidireccional que cause el ciclo.
    private final LightStatusService lightStatusService;

    // Constructor limpio, sin ciclos. El service se inyecta normalmente.
    // lightStatusService es necesario para DELEGAR los mensajes recibidos del websocket al service.
    public LightStatusWebSocketHandler(LightStatusService lightStatusService) {
        this.lightStatusService = lightStatusService;
    }


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        System.out.println("Nueva sesion WebSocket conectada: " + session.getId() + ". Total sesiones: " + sessions.size());
        // Cuando un cliente se conecta, el servicio PUBLICARÁ un evento
        // para que este handler le envíe el estado inicial.
        // lightStatusService.sendCurrentLightStatesToClient(session); // ¡El service publica el evento, no se llama directamente aquí!
        // En su lugar, al conectarse, el service llamará sendCurrentLightStatesToClient(session)
        // y ese método publicará un InitialLightStatusEvent que este handler escuchará.
        lightStatusService.sendCurrentLightStatesToClient(session); // Llama al service para que PUBLIQUE el evento
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String receivedJson = message.getPayload();
        System.out.println("Mensaje JSON recibido del cliente " + session.getId() + ": " + receivedJson);
        // Delega el procesamiento del mensaje al servicio
        // El service manejará la lógica y PUBLICARÁ los eventos correspondientes
        lightStatusService.processIncomingWebSocketMessage(receivedJson);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        System.out.println("Sesion WebSocket cerrada: " + session.getId() + " con estado " + status + ". Restantes: " + sessions.size());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("Error de transporte en sesion " + session.getId() + ": " + exception.getMessage());
        exception.printStackTrace();
    }

    // --- Métodos de Escucha de Eventos (Event Listeners) ---

    @EventListener // Escucha eventos de actualización de estado de luz
    public void handleLightStatusUpdateEvent(LightStatusUpdateEvent event) {
        System.out.println("Recibido evento LightStatusUpdateEvent: " + event.getMessageJson());
        // Envía el mensaje a todas las sesiones conectadas
        broadcastMessage(event.getMessageJson());
    }

    @EventListener // Escucha eventos de estado inicial de luz
    public void handleInitialLightStatusEvent(InitialLightStatusEvent event) {
        System.out.println("Recibido evento InitialLightStatusEvent para sesion: " + event.getTargetSession());
        // Envía el mensaje solo a la sesión específica si se proporcionó
        if (event.getTargetSession() instanceof WebSocketSession targetSession && targetSession.isOpen()) {
            sendMessageToSession(targetSession, event.getInitialStatusJson());
        } else {
            // Si targetSession es null o no es una WebSocketSession, se podría broadcast a todas
            // o manejar como un error dependiendo de la lógica deseada.
            // Para el caso de INITIAL_STATUS, generalmente se quiere enviar solo a la nueva sesión.
            System.err.println("Advertencia: No se pudo enviar estado inicial a sesion específica o sesion inválida: " + event.getTargetSession());
            // Fallback: Si no se pudo enviar a la específica, broadcast a todas (considera si esto tiene sentido para tu UI)
            // broadcastMessage(event.getInitialStatusJson());
        }
    }


    /**
     * Envía un mensaje String (que se espera sea JSON) a todos los clientes WebSocket conectados.
     */
    public void broadcastMessage(String message) {
        sessions.forEach(session -> {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(message));
                }
            } catch (IOException e) {
                System.err.println("Error al enviar mensaje a sesion " + session.getId() + ": " + e.getMessage());
            }
        });
    }

    /**
     * Envía un mensaje String a una sesión WebSocket específica.
     */
    public void sendMessageToSession(WebSocketSession session, String message) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(message));
            }
        } catch (IOException e) {
            System.err.println("Error al enviar mensaje a sesion " + session.getId() + ": " + e.getMessage());
        }
    }
}