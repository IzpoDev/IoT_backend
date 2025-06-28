package com.iot.lights.lights_iot.service;

import com.iot.lights.lights_iot.model.dto.LoginRequest;
import com.iot.lights.lights_iot.model.dto.LoginResponse;
import com.iot.lights.lights_iot.model.dto.UserRequestDto;
import com.iot.lights.lights_iot.model.dto.UserResponseDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService {
    UserResponseDto createUser(UserRequestDto userDto);

    UserResponseDto createAdmin(UserRequestDto userAdmin);

    LoginResponse authenticate(LoginRequest loginResponse);

    void deleteUser(Long userId);

    UserResponseDto updateUser(UserRequestDto userDto, Long id);

    UserResponseDto getUser(Long id);

    List<UserResponseDto> getUsers();
}