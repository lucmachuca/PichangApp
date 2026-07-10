package com.PichangApp.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMQConfig {

    public static final String QUEUE_MATCH_CREATED = "match.creado.queue";
    public static final String EXCHANGE_MATCH = "match.exchange";
    public static final String ROUTING_KEY_MATCH = "match.created";

    public static final String EXCHANGE_SEGURIDAD = "seguridad.exchange";

    public static final String QUEUE_BLOQUEO_CREADO = "seguridad.bloqueo.creado.queue";
    public static final String ROUTING_KEY_BLOQUEO_CREADO = "seguridad.bloqueo.creado";

    public static final String QUEUE_BLOQUEO_ELIMINADO = "seguridad.bloqueo.eliminado.queue";
    public static final String ROUTING_KEY_BLOQUEO_ELIMINADO = "seguridad.bloqueo.eliminado";

    public static final String EXCHANGE_COMUNICACION = "comunicacion.exchange";
    public static final String ROUTING_KEY_MENSAJE_CREADO = "comunicacion.mensaje.creado";
    public static final String ROUTING_KEY_SQUAD_SOLICITUD_CREADA = "comunicacion.squad.solicitud.creada";
    public static final String ROUTING_KEY_SQUAD_SOLICITUD_ACEPTADA = "comunicacion.squad.solicitud.aceptada";

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
    public TopicExchange seguridadExchange() {
        return new TopicExchange(EXCHANGE_SEGURIDAD);
    }

    @Bean
    public Queue bloqueoCreadoQueue() {
        return new Queue(QUEUE_BLOQUEO_CREADO, true);
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
    public Queue bloqueoEliminadoQueue() {
        return new Queue(QUEUE_BLOQUEO_ELIMINADO, true);
    }

    @Bean
    public Binding bloqueoEliminadoBinding(
            @Qualifier("bloqueoEliminadoQueue") Queue bloqueoEliminadoQueue,
            @Qualifier("seguridadExchange") TopicExchange seguridadExchange
    ) {
        return BindingBuilder
                .bind(bloqueoEliminadoQueue)
                .to(seguridadExchange)
                .with(ROUTING_KEY_BLOQUEO_ELIMINADO);
    }

    @Bean
    public TopicExchange comunicacionExchange() {
        return new TopicExchange(EXCHANGE_COMUNICACION);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }


}