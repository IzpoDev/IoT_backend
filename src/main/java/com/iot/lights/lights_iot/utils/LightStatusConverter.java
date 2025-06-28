package com.iot.lights.lights_iot.utils;

import com.iot.lights.lights_iot.model.document.LightStatusDocument;
import com.iot.lights.lights_iot.model.dto.LightStatusDTO;
import java.util.List;
import java.util.stream.Collectors;

public class LightStatusConverter {

    public static LightStatusDTO fromDocument(LightStatusDocument document) {
        if (document == null) {
            return null;
        }

        LightStatusDTO dto = new LightStatusDTO();
        dto.setCuadra(document.getCuadra());
        dto.setEstado(document.getEstado());
        dto.setTimestamp(document.getTimestamp());
        dto.setType("UPDATE"); // Valor por defecto

        return dto;
    }

    public static LightStatusDocument toDocument(LightStatusDTO dto) {
        if (dto == null) {
            return null;
        }

        LightStatusDocument document = new LightStatusDocument();
        document.setCuadra(dto.getCuadra());
        document.setEstado(dto.getEstado());
        document.setTimestamp(dto.getTimestamp());

        return document;
    }

    public static List<LightStatusDTO> toDtoList(List<LightStatusDocument> documents) {
        return documents.stream()
                .map(LightStatusConverter::fromDocument)
                .collect(Collectors.toList());
    }
}