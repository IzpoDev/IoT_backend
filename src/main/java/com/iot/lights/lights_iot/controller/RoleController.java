package com.iot.lights.lights_iot.controller;

import com.iot.lights.lights_iot.model.dto.RoleRequestDto;
import com.iot.lights.lights_iot.model.dto.RoleResponseDto;
import com.iot.lights.lights_iot.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/role")
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;
    // Aquí puedes agregar los métodos para manejar las operaciones relacionadas con roles
    @PostMapping("/create")
    public ResponseEntity<RoleResponseDto> createRole(@RequestBody @Valid RoleRequestDto roleRequestDto) {
        RoleResponseDto createdRole = roleService.createRole(roleRequestDto);
        return new ResponseEntity<>(createdRole, HttpStatus.CREATED);
    }
    @GetMapping("/get/{id}")
    public ResponseEntity<RoleResponseDto> getRoleById(@PathVariable("id") Long id) {
        RoleResponseDto roleResponseDto = roleService.getRole(id);
        return new ResponseEntity<>(roleResponseDto, HttpStatus.OK);
    }
    @GetMapping("/get-list")
    public ResponseEntity<Iterable<RoleResponseDto>> getAllRoles() {
        Iterable<RoleResponseDto> roles = roleService.getAllRoles();
        return new ResponseEntity<>(roles, HttpStatus.OK);
    }

}
