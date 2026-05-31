package com.PichangApp.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_MATCH_CREATED = "match.creado.queue";
    public static final String EXCHANGE_MATCH = "match.exchange";
    public static final String ROUTING_KEY_MATCH = "match.created";

    public static final String QUEUE_BLOQUEO_CREADO = "seguridad.bloqueo.creado.queue";
    public static final String EXCHANGE_SEGURIDAD = "seguridad.exchange";
    public static final String ROUTING_KEY_BLOQUEO_CREADO = "seguridad.bloqueo.creado";

    @Bean
    public Queue matchCreatedQueue() {
        return new Queue(QUEUE_MATCH_CREATED, true);
    }

    @Bean
    public TopicExchange matchExchange() {
        return new TopicExchange(EXCHANGE_MATCH);
    }

    @Bean
    public Binding matchBinding(
            @Qualifier("matchCreatedQueue") Queue matchCreatedQueue,
            @Qualifier("matchExchange") TopicExchange matchExchange
    ) {
        return BindingBuilder
                .bind(matchCreatedQueue)
                .to(matchExchange)
                .with(ROUTING_KEY_MATCH);
    }

    @Bean
    public Queue bloqueoCreadoQueue() {
        return new Queue(QUEUE_BLOQUEO_CREADO, true);
    }

    @Bean
    public TopicExchange seguridadExchange() {
        return new TopicExchange(EXCHANGE_SEGURIDAD);
    }

    @Bean
    public Binding bloqueoCreadoBinding(
            @Qualifier("bloqueoCreadoQueue") Queue bloqueoCreadoQueue,
            @Qualifier("seguridadExchange") TopicExchange seguridadExchange
    ) {
        return BindingBuilder
                .bind(bloqueoCreadoQueue)
                .to(seguridadExchange)
                .with(ROUTING_KEY_BLOQUEO_CREADO);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}