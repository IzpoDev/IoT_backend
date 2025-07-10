// src/main/java/com/iot/lights/lights_iot/model/document/LightStatusActualDocument.java
package com.iot.lights.lights_iot.model.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "light_status")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LightStatusActualDocument {
    @Id
    private String cuadra; // Usar cuadra como ID para sobrescribir siempre el estado actual
    private String estado;
    private LocalDateTime timestamp;
}