package com.iot.lights.lights_iot.model.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "light_status_history") // Mapea esta clase a la colección 'light_status_history'
@NoArgsConstructor
@AllArgsConstructor
@Data
public class LightStatusDocument { // Renombrado a Document para consistencia con tu estructura

    @Id // Marca este campo como el ID único del documento en MongoDB
    private String id;
    private String cuadra;
    private String estado; // "ENCENDIDA" o "APAGADA"
    private LocalDateTime timestamp; // Momento en que se registró el estado

    @Override
    public String toString() {
        return "LightStatusDocument{" +
                "id='" + id + '\'' +
                ", cuadra='" + cuadra + '\'' +
                ", estado='" + estado + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}