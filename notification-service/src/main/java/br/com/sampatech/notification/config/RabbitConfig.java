package br.com.sampatech.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "sampatech.events";

    public static final String QUEUE_USER_REGISTERED = "notification.usuario-cadastrado";
    public static final String QUEUE_SEARCH_PERFORMED = "notification.consulta-realizada";
    public static final String QUEUE_CHAT_MESSAGE = "notification.chat-mensagem";

    @Bean
    public TopicExchange sampatechExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue userRegisteredQueue() {
        return QueueBuilder.durable(QUEUE_USER_REGISTERED).build();
    }

    @Bean
    public Queue searchPerformedQueue() {
        return QueueBuilder.durable(QUEUE_SEARCH_PERFORMED).build();
    }

    @Bean
    public Queue chatMessageQueue() {
        return QueueBuilder.durable(QUEUE_CHAT_MESSAGE).build();
    }

    @Bean
    public Binding userRegisteredBinding() {
        return BindingBuilder.bind(userRegisteredQueue()).to(sampatechExchange()).with("usuario.cadastrado");
    }

    @Bean
    public Binding searchPerformedBinding() {
        return BindingBuilder.bind(searchPerformedQueue()).to(sampatechExchange()).with("consulta.realizada");
    }

    @Bean
    public Binding chatMessageBinding() {
        return BindingBuilder.bind(chatMessageQueue()).to(sampatechExchange()).with("chat.mensagem");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter();
        converter.setAlwaysConvertToInferredType(true);
        return converter;
    }
}