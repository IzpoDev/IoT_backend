package com.iot.lights.lights_iot.utils;

import com.iot.lights.lights_iot.model.document.LightStatusDocument;
import com.iot.lights.lights_iot.model.dto.LightStatusDTO;


import java.util.List;
import java.util.stream.Collectors;

/**
 * Clase de utilidad para la conversión manual de objetos entre DTOs y Documentos
 * de la base de datos. Utiliza métodos estáticos para no requerir instanciación.
 */
public class LightStatusConverter {

    // Constructor privado para evitar que la clase sea instanciada
    private LightStatusConverter() {
        // Clase de utilidad
    }

    /**
     * Convierte un LightStatusDocument (entidad de MongoDB) a un LightStatusDTO (para el frontend/salida WebSocket).
     *
     * @param document El LightStatusDocument a convertir.
     * @return El LightStatusDTO convertido.
     */
    public static LightStatusDTO toDto(LightStatusDocument document) {
        if (document == null) {
            return null;
        }
        // Cuando se convierte de un documento de DB a un DTO para el frontend,
        // podemos establecer el tipo a "UPDATE" o un tipo específico para datos históricos.
        return new LightStatusDTO(
                "UPDATE", // Tipo de mensaje por defecto al obtener de DB para el frontend
                document.getCuadra(),
                document.getEstado(),
                document.getTimestamp() // Incluye el timestamp del registro de la DB
        );
    }

    /**
     * Convierte un LightStatusDTO (del ESP32/entrada) a un LightStatusDocument (para persistencia en MongoDB).
     *
     * @param dto El LightStatusDTO a convertir.
     * @return El LightStatusDocument convertido.
     */
    public static LightStatusDocument toDocument(LightStatusDTO dto) {
        LightStatusDocument document = new LightStatusDocument();
        document.setCuadra(dto.getCuadra());
        document.setEstado(dto.getEstado());
        document.setTimestamp(dto.getTimestamp());
        return document;
    }

    /**
     * Convierte una lista de objetos LightStatusDocument a una lista de objetos LightStatusDTO.
     *
     * @param documents La lista de LightStatusDocument a convertir.
     * @return La lista de LightStatusDTO convertida.
     */
    public static List<LightStatusDTO> toDtoList(List<LightStatusDocument> documents) {
        if (documents == null) {
            return null;
        }
        return documents.stream()
                .map(LightStatusConverter::toDto) // Usa el método estático toDto para cada elemento
                .collect(Collectors.toList());
    }

    /**
     * Convierte una lista de objetos LightStatusDTO a una lista de objetos LightStatusDocument.
     *
     * @param dtos La lista de LightStatusDTO a convertir.
     * @return La lista de LightStatusDocument convertida.
     */
    public static List<LightStatusDocument> toDocumentList(List<LightStatusDTO> dtos) {
        if (dtos == null) {
            return null;
        }
        return dtos.stream()
                .map(LightStatusConverter::toDocument) // Usa el método estático toDocument para cada elemento
                .collect(Collectors.toList());
    }
}