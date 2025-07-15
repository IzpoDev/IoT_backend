package com.iot.lights.lights_iot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
// Con este atributo, le decimos a Spring que use nuestro bean personalizado
@EnableJpaAuditing(auditorAwareRef = "auditAwareImpl")
public class JpaAuditingConfig {

    // También es una buena práctica definir el bean aquí mismo
    // para mantener toda la configuración de auditoría en un solo lugar.
    @Bean
    public AuditorAware<String> auditAwareImpl() {
        return new AuditAwareImpl();
    }
}