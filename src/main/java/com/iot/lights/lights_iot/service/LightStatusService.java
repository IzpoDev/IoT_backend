package com.iot.lights.lights_iot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot.lights.lights_iot.model.document.LightStatusActualDocument;
import com.iot.lights.lights_iot.model.document.LightStatusDocument;
import com.iot.lights.lights_iot.model.dto.InitialStatusDTO;
import com.iot.lights.lights_iot.model.dto.LightStatusDTO;
import com.iot.lights.lights_iot.model.event.InitialLightStatusEvent;
import com.iot.lights.lights_iot.model.event.LightStatusUpdateEvent;
import com.iot.lights.lights_iot.repository.LightStatusActualRepository;
import com.iot.lights.lights_iot.repository.LightStatusRepository;
import com.iot.lights.lights_iot.utils.LightStatusConverter;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
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
@RequiredArgsConstructor // <-- MEJORA: Lombok genera el constructor, más limpio.
public class LightStatusService {

    // Inyecciones finales, gestionadas por @RequiredArgsConstructor
    private final LightStatusRepository lightStatusRepository;
    private final LightStatusActualRepository lightStatusActualRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final EmailService emailService;
    private final ObjectMapper objectMapper; // Spring Boot ya provee este Bean

    // MEJORA: Externalizar la configuración de las cuadras.
    @Value("${iot.lights.cuadras:Cuadra 1, Cuadra 2, Cuadra 3, Cuadra 4}")
    private List<String> cuadras;

    private final Map<String, LightStatusDTO> currentLightStates = new ConcurrentHashMap<>();

    @PostConstruct
    private void initializeCurrentLightStates() {
        log.info("Inicializando estado de luces para las cuadras: {}", cuadras);
        for (String cuadra : cuadras) {
            // La lógica de buscar el último estado es correcta.
            Optional<LightStatusDocument> latest = lightStatusRepository.findTop1ByCuadraOrderByTimestampDesc(cuadra);

            LightStatusDTO status = latest.map(LightStatusConverter::fromDocument)
                    .orElseGet(() -> LightStatusDTO.builder() // Estado por defecto si no hay historial
                            .cuadra(cuadra)
                            .estado("ENCENDIDA") // O "DESCONOCIDO"
                            .timestamp(LocalDateTime.now())
                            .build());

            currentLightStates.put(cuadra, status);
        }
        log.info("Estado inicial en memoria cargado para {} cuadras.", currentLightStates.size());
    }

    /**
     * REFACTORIZADO: Punto de entrada único y simplificado para procesar mensajes del ESP32.
     */
    public void processIncomingWebSocketMessage(String messageJson) {
        // Primero, retransmitimos el mensaje a todos los clientes UI para una respuesta en tiempo real.
        eventPublisher.publishEvent(new LightStatusUpdateEvent(this, messageJson));
        log.debug("Evento de actualización publicado para retransmisión: {}", messageJson);

        try {
            // MEJORA: Parseamos a un JsonNode genérico para leer el campo "type" sin errores.
            JsonNode rootNode = objectMapper.readTree(messageJson);
            JsonNode typeNode = rootNode.get("type");

            if (typeNode == null) {
                log.warn("Mensaje recibido sin campo 'type', se ignora: {}", messageJson);
                return;
            }

            String messageType = typeNode.asText();

            // MEJORA: Lógica de enrutamiento clara basada en el tipo.
            switch (messageType) {
                case "INITIAL_STATUS":
                    log.info("Procesando mensaje de estado inicial (INITIAL_STATUS).");
                    InitialStatusDTO initialStatus = objectMapper.treeToValue(rootNode, InitialStatusDTO.class);
                    // CORRECCIÓN: Iteramos y guardamos cada estado en la BD.
                    initialStatus.getLuces().forEach(this::processAndPersistState);
                    log.info("Estado inicial del ESP32 procesado y guardado en la base de datos.");
                    break;

                case "ALARM":
                    log.info("Procesando mensaje de alarma (ALARM).");
                    LightStatusDTO alarmStatus = objectMapper.treeToValue(rootNode, LightStatusDTO.class);
                    processAndPersistState(alarmStatus);
                    break;

                default:
                    log.warn("Tipo de mensaje no reconocido: '{}'. Mensaje: {}", messageType, messageJson);
            }

        } catch (JsonProcessingException e) {
            log.error("Error fatal al parsear JSON del WebSocket: {}", messageJson, e);
        }
    }

    /**
     * NUEVO MÉTODO CENTRALIZADO: Procesa, persiste y actualiza el estado de una luz.
     * Es llamado tanto para estados iniciales como para alarmas.
     */
    private void processAndPersistState(LightStatusDTO newStatus) {
        newStatus.setTimestamp(LocalDateTime.now()); // Aseguramos timestamp del servidor

        // 1. Detectar si es un apagón ANTES de actualizar el estado en memoria
        detectOutage(newStatus);

        // 2. Actualizar el estado en el caché de memoria
        currentLightStates.put(newStatus.getCuadra(), newStatus);

        // 3. Guardar en ambas colecciones de MongoDB usando tu método existente. ¡Esto es clave!
        saveOrUpdateCurrentAndHistory(newStatus);

        log.info("Estado para la cuadra '{}' actualizado a '{}' y persistido.", newStatus.getCuadra(), newStatus.getEstado());
    }

    /**
     * MEJORA: Este método ahora es el único punto de verdad para la persistencia.
     */
    public void saveOrUpdateCurrentAndHistory(LightStatusDTO dto) {
        // Guardar en light_status_actual (estado actual)
        LightStatusActualDocument actual = new LightStatusActualDocument(dto.getCuadra(), dto.getEstado(), dto.getTimestamp());
        lightStatusActualRepository.save(actual);

        // Guardar en light_status_history (historial)
        LightStatusDocument history = LightStatusConverter.toDocument(dto);
        lightStatusRepository.save(history);
    }

    /**
     * MEJORA: Lógica de detección de apagón.
     * Compara el estado entrante con el que todavía está en memoria (el estado anterior).
     */
    private void detectOutage(LightStatusDTO incomingStatus) {
        if ("APAGADA".equals(incomingStatus.getEstado())) {
            LightStatusDTO previousStatus = currentLightStates.get(incomingStatus.getCuadra());
            // Si existía un estado previo y era "ENCENDIDA", es un apagón.
            if (previousStatus != null && "ENCENDIDA".equals(previousStatus.getEstado())) {
                log.warn("¡APAGÓN DETECTADO! Cuadra: {}", incomingStatus.getCuadra());
                sendOutageAlert(incomingStatus.getCuadra());
            }
        }
    }

    private void sendOutageAlert(String cuadra) {
        // MEJORA: Construir un DTO para la alerta y serializarlo a JSON. Más robusto que concatenar strings.
        LightStatusDTO alertDto = LightStatusDTO.builder()
                .type("OUTAGE_ALERT") // Un tipo específico para la UI
                .cuadra(cuadra)
                .estado("APAGADA")
                .timestamp(LocalDateTime.now())
                .build();
        try {
            String alertJson = objectMapper.writeValueAsString(alertDto);
            eventPublisher.publishEvent(new LightStatusUpdateEvent(this, alertJson));
        } catch (JsonProcessingException e) {
            log.error("Error al serializar la alerta de apagón para la cuadra {}", cuadra, e);
        }

        // El envío de email está bien.
        emailService.sendLightOutageAlert(cuadra, LocalDateTime.now());
    }

    // --- El resto de tus métodos públicos (para el Controller y el Handler) están bien y no necesitan cambios ---

    public List<LightStatusDTO> getCurrentLightStates() {
        return new ArrayList<>(currentLightStates.values());
    }

    public void sendCurrentLightStatesToClient(WebSocketSession session) {
        try {
            InitialStatusDTO initialStatus = new InitialStatusDTO(getCurrentLightStates());
            String initialStatusJson = objectMapper.writeValueAsString(initialStatus);
            eventPublisher.publishEvent(new InitialLightStatusEvent(this, initialStatusJson, session));
            log.info("Evento de estado inicial publicado para la nueva sesión {}", session.getId());
        } catch (Exception e) {
            log.error("Error al publicar evento de estado inicial para la sesión {}: {}", session.getId(), e.getMessage(), e);
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
}