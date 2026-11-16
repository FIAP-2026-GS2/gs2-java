package br.com.sampatech.notification.listener;

import br.com.sampatech.notification.config.RabbitConfig;
import br.com.sampatech.notification.entity.ChatHistory;
import br.com.sampatech.notification.messaging.ChatMessageEvent;
import br.com.sampatech.notification.repository.ChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatMessageListener {

    private static final Logger log = LoggerFactory.getLogger(ChatMessageListener.class);

    private final ChatHistoryRepository chatHistoryRepository;

    @RabbitListener(queues = RabbitConfig.QUEUE_CHAT_MESSAGE)
    public void onChatMessage(ChatMessageEvent event) {
        log.info("Recebido chat.mensagem do usuário {}", event.userId());

        chatHistoryRepository.save(ChatHistory.builder()
                .userId(event.userId())
                .question(event.question())
                .answer(event.answer())
                .createdAt(event.createdAt())
                .build());
    }
}