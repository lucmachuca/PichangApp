package com.PichangApp.msvc_seguridad.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_SEGURIDAD = "seguridad.exchange";
    public static final String ROUTING_KEY_BLOQUEO_CREADO = "seguridad.bloqueo.creado";

    @Bean
    public TopicExchange seguridadExchange() {
        return new TopicExchange(EXCHANGE_SEGURIDAD);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}