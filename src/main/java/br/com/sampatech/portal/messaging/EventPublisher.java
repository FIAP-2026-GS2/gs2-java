package br.com.sampatech.portal.messaging;

import br.com.sampatech.portal.config.RabbitConfig;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public void publishUserRegistered(UserRegisteredEvent event) {
        send(RabbitConfig.USER_REGISTERED, event);
    }

    public void publishSearchPerformed(SearchPerformedEvent event) {
        send(RabbitConfig.SEARCH_PERFORMED, event);
    }

    private void send(String routingKey, Object event) {
        try {
            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, routingKey, event);
            log.info("Evento publicado: {} -> {}", routingKey, event);
        } catch (AmqpException e) {
            log.warn("Falha ao publicar {}: {}", routingKey, e.getMessage());
        }
    }
    public void publishChatMessage(ChatMessageEvent event) {
        send(RabbitConfig.CHAT_MESSAGE, event);
    }
}