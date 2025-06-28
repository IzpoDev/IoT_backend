package com.iot.lights.lights_iot.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

// DTO para enviar el estado inicial de todas las luces
public class InitialStatusDTO {
    @JsonProperty("type")
    private String type; // Siempre "INITIAL_STATUS" para este DTO
    @JsonProperty("luces")
    private List<LightStatusDTO> luces;

    // Constructor vacío necesario para Jackson
    public InitialStatusDTO() {
        this.type = "INITIAL_STATUS";
    }

    public InitialStatusDTO(List<LightStatusDTO> luces) {
        this.type = "INITIAL_STATUS";
        this.luces = luces;
    }

    // --- Getters y Setters ---
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<LightStatusDTO> getLuces() {
        return luces;
    }

    public void setLuces(List<LightStatusDTO> luces) {
        this.luces = luces;
    }

    @Override
    public String toString() {
        return "InitialStatusDTO{" +
                "type='" + type + '\'' +
                ", luces=" + luces +
                '}';
    }
}