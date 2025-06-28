package com.iot.lights.lights_iot.controller;

import com.iot.lights.lights_iot.model.dto.LoginRequest;
import com.iot.lights.lights_iot.model.dto.LoginResponse;
import com.iot.lights.lights_iot.model.dto.UserRequestDto;
import com.iot.lights.lights_iot.model.dto.UserResponseDto;
import com.iot.lights.lights_iot.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/create")
    public ResponseEntity<UserResponseDto> createUser(@RequestBody @Valid UserRequestDto user) {
        UserResponseDto userDto = userService.createUser(user);
        return new ResponseEntity<>(userDto, HttpStatus.CREATED);
    }
    @GetMapping("/auth")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody @Valid LoginRequest loginRequest) {
        LoginResponse loginResponse = userService.authenticate(loginRequest);
        return new ResponseEntity<>(loginResponse, HttpStatus.ACCEPTED);
    }
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable("id") Long id){
        userService.deleteUser(id);
        return new ResponseEntity<>("Usuario con id(" + id +")  Borrado con exito! :C", HttpStatus.OK);
    }
    @PutMapping("/update/{id}")
    public ResponseEntity<UserResponseDto> updateUser(@RequestBody @Valid UserRequestDto userDto, @PathVariable("id") Long id){
        UserResponseDto userDto1 = userService.updateUser(userDto, id);
        return new ResponseEntity<>(userDto1, HttpStatus.OK);
    }
    @GetMapping("/get/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable("id") Long id){
        UserResponseDto userDto = userService.getUser(id);
        return new ResponseEntity<>(userDto, HttpStatus.OK);
    }
    @GetMapping("/get-list")
    public ResponseEntity<List<UserResponseDto>> getListUsers(){
        List<UserResponseDto> users = userService.getUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }
}
