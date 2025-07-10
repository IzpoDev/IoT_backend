package com.iot.lights.lights_iot.controller;

import com.iot.lights.lights_iot.model.dto.LightStatusDTO;
import com.iot.lights.lights_iot.service.LightStatusService;
import lombok.RequiredArgsConstructor; // Solo RequiredArgsConstructor
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/lights")
@RequiredArgsConstructor
public class LightStatusController {

    private final LightStatusService lightStatusService; // Hacer el campo 'final' para @RequiredArgsConstructor

    @GetMapping("/current")
    public ResponseEntity<List<LightStatusDTO>> getCurrentLightStatus() {
        return ResponseEntity.ok(lightStatusService.getCurrentLightStates());
    }

    @GetMapping("/history")
    public ResponseEntity<List<LightStatusDTO>> getAllLightHistory() {
        return ResponseEntity.ok(lightStatusService.getAllLightHistory());
    }

    @GetMapping("/history/{cuadraName}")
    public ResponseEntity<List<LightStatusDTO>> getLightHistoryByCuadra(@PathVariable String cuadraName) {
        return ResponseEntity.ok(lightStatusService.getLightHistoryByCuadra(cuadraName));
    }
}