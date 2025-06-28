package com.iot.lights.lights_iot.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.email.from}")
    private String fromEmail;

    @Value("${app.email.admin}")
    private String adminEmail;

    @Async
    public void sendLightOutageAlert(String cuadra, LocalDateTime timestamp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(adminEmail);
            helper.setFrom(fromEmail);
            helper.setSubject("🚨 ALERTA: Apagón en Cuadra " + cuadra);
            helper.setText(buildEmailContent(cuadra, timestamp), true);

            mailSender.send(message);
            log.info("Email de alerta enviado para cuadra: {}", cuadra);
        } catch (Exception e) {
            log.error("Error enviando email de alerta: {}", e.getMessage());
        }
    }

    private String buildEmailContent(String cuadra, LocalDateTime timestamp) {
        return """
                <html>
                <body style="font-family: Arial, sans-serif; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                        <h2 style="color: #d32f2f; text-align: center;">
                            🚨 ALERTA DE APAGÓN DETECTADO
                        </h2>

                        <div style="background-color: #ffebee; padding: 20px; border-radius: 8px; border-left: 5px solid #d32f2f;">
                            <h3>Detalles del Incidente:</h3>
                            <p><strong>Ubicación:</strong> Cuadra %s</p>
                            <p><strong>Fecha y Hora:</strong> %s</p>
                            <p><strong>Estado:</strong> Luz APAGADA</p>
                        </div>

                        <div style="margin-top: 20px; padding: 15px; background-color: #f5f5f5; border-radius: 5px;">
                            <h4>Acciones Recomendadas:</h4>
                            <ul>
                                <li>Verificar el estado físico de la lámpara</li>
                                <li>Revisar la conectividad del sistema IoT</li>
                                <li>Contactar al equipo de mantenimiento si es necesario</li>
                            </ul>
                        </div>

                        <div style="margin-top: 20px; text-align: center; color: #666; font-size: 12px;">
                            <p>Este es un mensaje automático del Sistema IoT de Gestión de Luces</p>
                            <p>No responder a este correo</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(cuadra, timestamp);
    }
}