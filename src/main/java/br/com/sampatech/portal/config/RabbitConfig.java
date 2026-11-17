package br.com.sampatech.portal.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "sampatech.events";
    public static final String USER_REGISTERED = "usuario.cadastrado";
    public static final String SEARCH_PERFORMED = "consulta.realizada";
    public static final String CHAT_MESSAGE = "chat.mensagem";

    @Bean
    public TopicExchange sampatechExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}