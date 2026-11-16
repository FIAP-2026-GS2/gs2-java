package br.com.sampatech.notification.listener;

import br.com.sampatech.notification.config.RabbitConfig;
import br.com.sampatech.notification.entity.NotificationLog;
import br.com.sampatech.notification.messaging.UserRegisteredEvent;
import br.com.sampatech.notification.repository.NotificationLogRepository;
import br.com.sampatech.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class UserRegisteredListener {

    private static final Logger log = LoggerFactory.getLogger(UserRegisteredListener.class);

    private final EmailService emailService;
    private final NotificationLogRepository notificationLogRepository;

    @RabbitListener(queues = RabbitConfig.QUEUE_USER_REGISTERED)
    public void onUserRegistered(UserRegisteredEvent event) {
        log.info("Recebido usuario.cadastrado: {}", event.email());

        String status;
        try {
            emailService.sendWelcome(event.email(), event.name());
            status = "ENVIADO";
        } catch (Exception e) {
            log.warn("Falha ao enviar e-mail para {}: {}", event.email(), e.getMessage());
            status = "ERRO";
        }

        notificationLogRepository.save(NotificationLog.builder()
                .userId(event.userId())
                .eventType("USUARIO_CADASTRADO")
                .channel("EMAIL")
                .status(status)
                .sentAt(LocalDateTime.now())
                .build());
    }
}