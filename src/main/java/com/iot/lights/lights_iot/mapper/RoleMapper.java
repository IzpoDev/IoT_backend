package com.iot.lights.lights_iot.mapper;

import com.iot.lights.lights_iot.model.dto.RoleRequestDto;
import com.iot.lights.lights_iot.model.dto.RoleResponseDto;
import com.iot.lights.lights_iot.model.entity.RoleEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RoleMapper {
    public static RoleEntity toEntity(RoleRequestDto roleRequestDto) {
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setName(roleRequestDto.getName());
        roleEntity.setDescription(roleRequestDto.getDescription());
        return roleEntity;
    }
    public static RoleResponseDto toDto(RoleEntity roleEntity) {
        RoleResponseDto roleResponseDto = new RoleResponseDto();
        roleResponseDto.setId(roleEntity.getId());
        roleResponseDto.setName(roleEntity.getName());
        roleResponseDto.setDescription(roleEntity.getDescription());
        return roleResponseDto;
    }
    public static List<RoleResponseDto> toListDto(List<RoleEntity> roleEntities) {
        return roleEntities.stream()
                .map(RoleMapper::toDto)
                .toList();
    }
}
