package com.iot.lights.lights_iot.config;

import com.iot.lights.lights_iot.handler.LightStatusWebSocketHandler; // Ajusta el paquete
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final LightStatusWebSocketHandler lightStatusWebSocketHandler;

    public WebSocketConfig(LightStatusWebSocketHandler lightStatusWebSocketHandler) {
        this.lightStatusWebSocketHandler = lightStatusWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(lightStatusWebSocketHandler, "/ws/lights").setAllowedOrigins("*");
    }
}