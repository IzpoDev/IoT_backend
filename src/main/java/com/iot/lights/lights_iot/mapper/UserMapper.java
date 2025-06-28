package com.iot.lights.lights_iot.mapper;

import com.iot.lights.lights_iot.model.dto.UserRequestDto;
import com.iot.lights.lights_iot.model.dto.UserResponseDto;
import com.iot.lights.lights_iot.model.entity.UserEntity;

import java.util.List;

public class UserMapper {

    public static UserResponseDto toDto(UserEntity userEntity){
        UserResponseDto userResponseDto = new UserResponseDto();
        userResponseDto.setId(userEntity.getId());
        userResponseDto.setUsername(userEntity.getUsername());
        userResponseDto.setEmail(userEntity.getEmail());
        return userResponseDto;
    }
    public static UserEntity toEntity(UserRequestDto userRequestDto) {
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(userRequestDto.getUsername());
        userEntity.setEmail(userRequestDto.getEmail());
        userEntity.setPassword(userRequestDto.getPassword());
        return userEntity;
    }
    public static List<UserResponseDto> toListDto(List<UserEntity> userEntities) {
        return userEntities.stream()
                .map(UserMapper::toDto)
                .toList();
    }
}
