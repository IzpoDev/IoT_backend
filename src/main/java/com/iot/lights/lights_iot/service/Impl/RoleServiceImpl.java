package com.iot.lights.lights_iot.service.Impl;

import com.iot.lights.lights_iot.mapper.RoleMapper;
import com.iot.lights.lights_iot.model.dto.RoleRequestDto;
import com.iot.lights.lights_iot.model.dto.RoleResponseDto;
import com.iot.lights.lights_iot.model.entity.RoleEntity;
import com.iot.lights.lights_iot.repository.RoleRepository;
import com.iot.lights.lights_iot.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;
    @Override
    public RoleResponseDto createRole(RoleRequestDto roleRequestDto) {
        if(roleRepository.findByName(roleRequestDto.getName()) != null) {
            throw new IllegalArgumentException("Role con el nombre " + roleRequestDto.getName() + " ya existe.");
        }
        RoleEntity roleEntity1 = RoleMapper.toEntity(roleRequestDto);
        RoleEntity roleEntity = roleRepository.save(roleEntity1);
        return RoleMapper.toDto(roleEntity);
    }

    @Override
    public RoleResponseDto updateRole(RoleRequestDto roleRequestDto, Long id) {
        if (!roleRepository.existsById(id)) {
            throw new IllegalArgumentException("Role con el id " + id + " no existe.");
        }
        RoleEntity existingRole = roleRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Role con el id " + id + " no existe."));
        existingRole.setName(roleRequestDto.getName());
        existingRole.setDescription(roleRequestDto.getDescription());
        RoleEntity updatedRole = roleRepository.save(existingRole);
        return RoleMapper.toDto(updatedRole);
    }

    @Override
    public RoleResponseDto getRole(Long id) {
        RoleEntity roleEntity = roleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Role con el id " + id + " no existe."));
        return RoleMapper.toDto(roleEntity);
    }

    @Override
    public void deleteRole(Long id) {
        if (!roleRepository.existsById(id)) {
            throw new IllegalArgumentException("Role con el id " + id + " no existe.");
        }
        RoleEntity roleEntity = roleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Role con el id " + id + " no existe."));
        roleEntity.setActive(false);
        roleRepository.save(roleEntity);
    }

    @Override
    public List<RoleResponseDto> getAllRoles() {
        if (roleRepository.findAll().isEmpty()) {
            throw new IllegalArgumentException("No existen roles en la base de datos.");
        }
        List<RoleEntity> roleEntities = roleRepository.findAll();
        return RoleMapper.toListDto(roleEntities);
    }
}
