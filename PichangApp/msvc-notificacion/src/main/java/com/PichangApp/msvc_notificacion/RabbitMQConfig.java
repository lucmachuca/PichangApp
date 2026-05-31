package com.PichangApp.msvc_notificacion;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_NOTIFICACIONES_MATCH = "queue_notificaciones_match";
    public static final String EXCHANGE_MATCH = "match.exchange";
    public static final String ROUTING_KEY_MATCH = "match.created";

    @Bean
    public Queue queueNotificacionesMatch() {
        return new Queue(QUEUE_NOTIFICACIONES_MATCH, true);
    }

    @Bean
    public TopicExchange matchExchange() {
        return new TopicExchange(EXCHANGE_MATCH);
    }

    @Bean
    public Binding matchNotificacionBinding(
            Queue queueNotificacionesMatch,
            TopicExchange matchExchange
    ) {
        return BindingBuilder
                .bind(queueNotificacionesMatch)
                .to(matchExchange)
                .with(ROUTING_KEY_MATCH);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}