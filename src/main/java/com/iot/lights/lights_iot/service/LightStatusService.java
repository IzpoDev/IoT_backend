package com.iot.lights.lights_iot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot.lights.lights_iot.model.document.LightStatusDocument;
import com.iot.lights.lights_iot.model.dto.InitialStatusDTO;
import com.iot.lights.lights_iot.model.dto.LightStatusDTO;
import com.iot.lights.lights_iot.model.event.InitialLightStatusEvent; // Nueva importación
import com.iot.lights.lights_iot.model.event.LightStatusUpdateEvent;   // Nueva importación
import com.iot.lights.lights_iot.repository.LightStatusRepository;
import com.iot.lights.lights_iot.utils.LightStatusConverter;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher; // Nueva importación
// REMOVER: import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class LightStatusService {

    // REMOVER: private final LightStatusWebSocketHandler webSocketHandler;
    private final LightStatusRepository lightStatusRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, LightStatusDTO> currentLightStates = new ConcurrentHashMap<>();
    private final ApplicationEventPublisher eventPublisher; // ¡Nueva inyección!
    private final EmailService emailService; // Nueva inyección para EmailService

    // El constructor ahora NO necesita LightStatusWebSocketHandler
    public LightStatusService(LightStatusRepository lightStatusRepository,
                              ApplicationEventPublisher eventPublisher, EmailService emailService) { // Inyecta ApplicationEventPublisher
        this.lightStatusRepository = lightStatusRepository;
        this.eventPublisher = eventPublisher;
        initializeCurrentLightStates();
        this.emailService = emailService; // Inicializa EmailService
    }

    @PostConstruct
    private void initializeCurrentLightStates() {
        String[] cuadras = {"Cuadra 1", "Cuadra 2", "Cuadra 3", "Cuadra 4", "Cuadra 5", "Cuadra 6"};

        for (String cuadra : cuadras) {
            try {
                // Opción 1: Usar Optional para manejar caso cuando no existe
                Optional<LightStatusDocument> latest = lightStatusRepository.findTop1ByCuadraOrderByTimestampDesc(cuadra);

                LightStatusDTO defaultStatus;
                if (latest.isPresent()) {
                    defaultStatus = LightStatusConverter.fromDocument(latest.get());
                } else {
                    // Estado por defecto si no existe historial
                    defaultStatus = LightStatusDTO.builder()
                            .cuadra(cuadra)
                            .estado("ENCENDIDA")
                            .timestamp(LocalDateTime.now())
                            .build();
                }

                currentLightStates.put(cuadra, defaultStatus);

            } catch (Exception e) {
                log.error("Error inicializando estado para {}: {}", cuadra, e.getMessage());

                // Estado por defecto en caso de error
                LightStatusDTO defaultStatus = LightStatusDTO.builder()
                        .cuadra(cuadra)
                        .estado("ENCENDIDA")
                        .timestamp(LocalDateTime.now())
                        .build();

                currentLightStates.put(cuadra, defaultStatus);
            }
        }
    }

    // Método para procesar los mensajes JSON que llegan del ESP32 a través del WebSocketHandler
    public void processIncomingWebSocketMessage(String messageJson) {
        try {
            // Publica un evento para que el handler lo retransmita
            eventPublisher.publishEvent(new LightStatusUpdateEvent(this, messageJson));
            System.out.println("Mensaje recibido y evento publicado para retransmision: " + messageJson);

            LightStatusDTO receivedAlarmDto = null;
            try {
                receivedAlarmDto = objectMapper.readValue(messageJson, LightStatusDTO.class);
            } catch (Exception e) {
                System.out.println("No es un DTO de alarma directo, intentando como estado inicial o ignorando.");
            }

            if (receivedAlarmDto != null && "ALARM".equals(receivedAlarmDto.getType())) {
                receivedAlarmDto.setTimestamp(LocalDateTime.now());
                currentLightStates.put(receivedAlarmDto.getCuadra(), receivedAlarmDto);

                LightStatusDocument recordToSave = LightStatusConverter.toDocument(receivedAlarmDto);
                lightStatusRepository.save(recordToSave);
                System.out.println("Registro de estado de luz guardado en DB: " + recordToSave);

            } else {
                InitialStatusDTO initialStatusDto = null;
                try {
                    initialStatusDto = objectMapper.readValue(messageJson, InitialStatusDTO.class);
                } catch (Exception e) {
                    System.out.println("Mensaje no reconocido como ALARM o INITIAL_STATUS: " + messageJson);
                    return;
                }

                if (initialStatusDto != null && "INITIAL_STATUS".equals(initialStatusDto.getType())) {
                    System.out.println("Estado inicial recibido del ESP32. Sincronizando estado en memoria.");
                    if (initialStatusDto.getLuces() != null) {
                        initialStatusDto.getLuces().forEach(lightDto -> {
                            lightDto.setType("UPDATE");
                            lightDto.setTimestamp(LocalDateTime.now());
                            currentLightStates.put(lightDto.getCuadra(), lightDto);
                        });
                        System.out.println("Estado en memoria sincronizado con INITIAL_STATUS del ESP32.");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error al procesar mensaje WebSocket entrante: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<LightStatusDTO> getCurrentLightStates() {
        return new ArrayList<>(currentLightStates.values());
    }

    // Método para enviar el estado actual a un cliente específico cuando se conecta
    // Ahora publica un evento para que el Handler lo gestione
    public void sendCurrentLightStatesToClient(WebSocketSession session) {
        try {
            List<LightStatusDTO> currentStatesList = getCurrentLightStates();
            InitialStatusDTO initialStatus = new InitialStatusDTO(currentStatesList);
            String initialStatusJson = objectMapper.writeValueAsString(initialStatus);
            // Publica un evento de estado inicial, pasando la sesión objetivo
            eventPublisher.publishEvent(new InitialLightStatusEvent(this, initialStatusJson, session));
            System.out.println("Evento de estado inicial publicado para nueva sesion " + session.getId());
        } catch (Exception e) {
            System.err.println("Error al publicar evento de estado inicial para nueva sesion: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<LightStatusDTO> getAllLightHistory() {
        List<LightStatusDocument> records = lightStatusRepository.findAll();
        return LightStatusConverter.toDtoList(records);
    }

    public List<LightStatusDTO> getLightHistoryByCuadra(String cuadra) {
        List<LightStatusDocument> records = lightStatusRepository.findByCuadraOrderByTimestampDesc(cuadra);
        return LightStatusConverter.toDtoList(records);
    }

    public LightStatusDTO getCurrentLightStateByCuadra(String cuadra) {
        return currentLightStates.get(cuadra);
    }

    @EventListener
    public void detectOutage(LightStatusDTO status) {
        if ("APAGADA".equals(status.getEstado())) {
            // Usar el método correcto con Optional
            Optional<LightStatusDocument> lastStatusOpt = lightStatusRepository.findTop1ByCuadraOrderByTimestampDesc(status.getCuadra());

            if (lastStatusOpt.isPresent() && "ENCENDIDA".equals(lastStatusOpt.get().getEstado())) {
                // ¡Apagón detectado!
                sendOutageAlert(status.getCuadra());
            }
        }
    }

    private void sendOutageAlert(String cuadra) {
        // Envío dual: WebSocket + Email
        eventPublisher.publishEvent(
                new LightStatusUpdateEvent(this, "ALARM" + cuadra +  "APAGADA" + LocalDateTime.now())
        );
        emailService.sendLightOutageAlert(cuadra, LocalDateTime.now());
    }
}