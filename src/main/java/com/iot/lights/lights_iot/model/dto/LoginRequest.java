package com.iot.lights.lights_iot.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@Setter
@NoArgsConstructor
public class LoginRequest {
    @NotBlank(message = "El campo username no puede estar en blanco")
    private String username;
    @NotBlank(message = "El campo password no puede estar en blanco")
    private String password;
}
