package com.iot.lights.lights_iot.service.Impl;

import com.iot.lights.lights_iot.exception.EntityNotFoundException;
import com.iot.lights.lights_iot.mapper.UserMapper;
import com.iot.lights.lights_iot.model.dto.LoginRequest;
import com.iot.lights.lights_iot.model.dto.LoginResponse;
import com.iot.lights.lights_iot.model.dto.UserRequestDto;
import com.iot.lights.lights_iot.model.dto.UserResponseDto;
import com.iot.lights.lights_iot.model.entity.UserEntity;
import com.iot.lights.lights_iot.repository.RoleRepository;
import com.iot.lights.lights_iot.repository.UserRepository;
import com.iot.lights.lights_iot.service.UserService;
import com.iot.lights.lights_iot.utils.JwtUtil;
import lombok.AllArgsConstructor;

import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Override
    public UserResponseDto createUser(UserRequestDto userDto) {
        UserEntity userEntity = UserMapper.toEntity(userDto);
        if(userRepository.existsByUsername(userEntity.getUsername())) {
            throw new IllegalArgumentException("El Username ya se encuentra registrado");
        }
        if(userRepository.existsByEmail(userEntity.getEmail())) {
            throw new IllegalArgumentException("El Email ya se encuentra registrado");
        }
        userEntity.setPassword(passwordEncoder.encode(userDto.getPassword()));
        userEntity.setActive(true);
        userEntity.setRoleUser(roleRepository.findByName("USER"));
        UserEntity savedUser = userRepository.save(userEntity);
        return UserMapper.toDto(savedUser);
    }

    @Override
    public UserResponseDto createAdmin(UserRequestDto userAdmin) {
        UserEntity userEntity = UserMapper.toEntity(userAdmin);
        if(userRepository.existsByUsername(userEntity.getUsername())) {
            throw new IllegalArgumentException("El Username ya se encuentra registrado como administrador");
        }
        if(userRepository.existsByEmail(userEntity.getEmail())) {
            throw new IllegalArgumentException("El Email ya se encuentra registrado como administrador");
        }
        userEntity.setActive(true);
        userEntity.setRoleUser(roleRepository.findByName("ADMIN"));
        userEntity.setPassword(passwordEncoder.encode(userAdmin.getPassword()));
        UserEntity savedUser = userRepository.save(userEntity);
        return UserMapper.toDto(savedUser);
    }

    @Override
    public LoginResponse authenticate(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            if (authentication.isAuthenticated()) {
                UserEntity userEntity = userRepository.findByUsernameAndActive(loginRequest.getUsername(), Boolean.TRUE)
                        .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado o inactivo"));

                String token = jwtUtil.generateToken(userEntity.getUsername(), userEntity.getRoleUser().getName());
                return new LoginResponse(token,"Haz iniciado sesion correctamen", UserMapper.toDto(userEntity));
            } else {
                throw new BadRequestException("Las credenciales son incorrectas o el usuario no está activo.");
            }
        } catch (Exception e) {
            return new LoginResponse("token no generado","Error durante la autenticación: " + e.getMessage(), null);
        }
    }

    @Override
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("El usuario con el id " + userId + " no existe");
        }
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("El usuario con el id " + userId + " no existe"));
        userEntity.setActive(false);
        userRepository.save(userEntity);
    }

    @Override
    public UserResponseDto updateUser(UserRequestDto userDto, Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("El usuario con el id " + id + " no existe");
        }
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("El usuario con el id " + id + " no existe"));
        userEntity.setUsername(userDto.getUsername());
        userEntity.setEmail(userDto.getEmail());
        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            userEntity.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }
        return UserMapper.toDto(userRepository.save(userEntity));
    }

    @Override
    public UserResponseDto getUser(Long id) {
        if (!userRepository.existsById(id)) {
        throw new EntityNotFoundException("El usuario con el id " + id + " no existe");
        }
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("El usuario con el id " + id + " no existe"));
        return UserMapper.toDto(userEntity);
    }

    @Override
    public List<UserResponseDto> getUsers() {
        if (userRepository.findAll().isEmpty()) {
            throw new EntityNotFoundException("No hay usuarios registrados");
        }
        List<UserEntity> userEntities = userRepository.findAll();
        return UserMapper.toListDto(userEntities);
    }
}
