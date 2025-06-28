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
        http.csrf(AbstractHttpConfigurer::disable);
        http
                .authorizeHttpRequests(requests ->
                        requests.requestMatchers( "/swagger-ui/**").permitAll()
                                .requestMatchers( "/v3/api-docs*/**").permitAll()

                                .requestMatchers( HttpMethod.POST,"/user/create").permitAll()
                                .requestMatchers( HttpMethod.GET,"/user/auth").permitAll()
                                .requestMatchers( "/user/**").authenticated()

                                .requestMatchers(HttpMethod.POST, "/admin/**").hasRole("admin")

                                .anyRequest().authenticated()
                );
        http.formLogin(AbstractHttpConfigurer::disable);
        http.httpBasic(hbc -> hbc.authenticationEntryPoint(new CustomBasicAuthenticationEntryPoint()));
        http.exceptionHandling(e -> e.accessDeniedHandler(new CustomAccessDeniedHandler()));


        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
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
