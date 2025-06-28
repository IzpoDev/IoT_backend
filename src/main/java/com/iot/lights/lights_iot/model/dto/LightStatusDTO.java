package com.iot.lights.lights_iot.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime; // Para incluir timestamp en el DTO de salida

// DTO para los mensajes individuales de alarma o estado de luz
public class LightStatusDTO {
    @JsonProperty("type")
    private String type; // Por ejemplo: "ALARM", "UPDATE", "INITIAL_STATUS_ITEM"
    @JsonProperty("cuadra")
    private String cuadra;
    @JsonProperty("estado")
    private String estado; // "ENCENDIDA" o "APAGADA"
    // Campo adicional para el timestamp, útil para el frontend al mostrar el historial
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    // Constructor vacío es necesario para Jackson
    public LightStatusDTO() {}

    // Constructor para mensajes de ESP32 (sin timestamp, se añade en el backend)
    public LightStatusDTO(String type, String cuadra, String estado) {
        this.type = type;
        this.cuadra = cuadra;
        this.estado = estado;
    }

    // Constructor completo para respuestas que incluyen timestamp
    public LightStatusDTO(String type, String cuadra, String estado, LocalDateTime timestamp) {
        this.type = type;
        this.cuadra = cuadra;
        this.estado = estado;
        this.timestamp = timestamp;
    }

    // --- Getters y Setters ---
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCuadra() {
        return cuadra;
    }

    public void setCuadra(String cuadra) {
        this.cuadra = cuadra;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "LightStatusDTO{" +
                "type='" + type + '\'' +
                ", cuadra='" + cuadra + '\'' +
                ", estado='" + estado + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}