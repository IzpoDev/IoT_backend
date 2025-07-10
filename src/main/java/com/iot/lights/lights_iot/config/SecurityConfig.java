package com.iot.lights.lights_iot.config;

import com.iot.lights.lights_iot.exception.CustomAccessDeniedHandler;
import com.iot.lights.lights_iot.exception.CustomBasicAuthenticationEntryPoint;
import com.iot.lights.lights_iot.filter.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 1. Deshabilitar CSRF y añadir nuestro filtro JWT al principio de la cadena.
        http.csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        // 2. Configurar las reglas de autorización de las peticiones HTTP.
        http.authorizeHttpRequests(requests -> requests
                // --- A. ENDPOINTS PÚBLICOS (No requieren autenticación) ---
                .requestMatchers(
                        "/swagger-ui/**",
                        "/v3/api-docs*/**",
                        "/ws/lights" // El WebSocket es público
                ).permitAll()
                .requestMatchers(HttpMethod.POST,
                        "/user/create", // Permitir que cualquiera cree una cuenta de usuario.
                        "/user/auth"    // Permitir que cualquiera intente autenticarse.
                ).permitAll()

                // --- B. ENDPOINTS DE ADMINISTRADOR (Requieren rol 'ADMIN') ---
                // CORRECCIÓN: Se protegen todos los endpoints de roles y de admin.
                .requestMatchers(// Toda la gestión de roles es solo para administradores.
                        "/admin/**",
                        "/role/**"
                ).hasRole("ADMIN") // MEJORA: Usar mayúsculas es la convención.

                // --- C. CUALQUIER OTRA PETICIÓN (Requiere autenticación) ---
                // Si la petición no coincide con ninguna regla anterior, debe estar autenticada.
                .anyRequest().authenticated()
        );

        // 3. Deshabilitar el formulario de login por defecto de Spring.
        http.formLogin(AbstractHttpConfigurer::disable);

        // 4. Configurar manejadores de errores de autenticación y autorización personalizados.
        http.httpBasic(hbc -> hbc.authenticationEntryPoint(new CustomBasicAuthenticationEntryPoint()));
        http.exceptionHandling(e -> e.accessDeniedHandler(new CustomAccessDeniedHandler()));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}