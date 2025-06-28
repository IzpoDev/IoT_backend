package com.iot.lights.lights_iot.service;

import com.iot.lights.lights_iot.model.dto.RoleRequestDto;
import com.iot.lights.lights_iot.model.dto.RoleResponseDto;

import java.util.List;

public interface RoleService {
    RoleResponseDto createRole(RoleRequestDto roleEntity);
    RoleResponseDto updateRole(RoleRequestDto roleEntity, Long id);
    RoleResponseDto getRole(Long id);
    void deleteRole(Long id);
    List<RoleResponseDto> getAllRoles();
}
