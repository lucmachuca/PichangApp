package com.PichangApp.msvc_notificacion;

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

    public static final String EXCHANGE_MATCH = "match.exchange";
    public static final String ROUTING_KEY_MATCH_CREATED = "match.created";
    public static final String QUEUE_NOTIFICACIONES_MATCH = "notificaciones.match.queue";

    public static final String EXCHANGE_COMUNICACION = "comunicacion.exchange";
    public static final String ROUTING_KEY_MENSAJE_CREADO = "comunicacion.mensaje.creado";
    public static final String QUEUE_NOTIFICACIONES_MENSAJE = "notificaciones.mensaje.queue";

    public static final String ROUTING_KEY_SQUAD_SOLICITUD_CREADA = "comunicacion.squad.solicitud.creada";
    public static final String QUEUE_NOTIFICACIONES_SQUAD_SOLICITUD_CREADA = "notificaciones.squad.solicitud.creada.queue";

    public static final String ROUTING_KEY_SQUAD_SOLICITUD_ACEPTADA = "comunicacion.squad.solicitud.aceptada";
    public static final String QUEUE_NOTIFICACIONES_SQUAD_SOLICITUD_ACEPTADA = "notificaciones.squad.solicitud.aceptada.queue";

    @Bean
    public TopicExchange matchExchange() {
        return new TopicExchange(EXCHANGE_MATCH);
    }

    @Bean
    public Queue notificacionesMatchQueue() {
        return new Queue(QUEUE_NOTIFICACIONES_MATCH, true);
    }

    @Bean
    public Binding notificacionesMatchBinding(
            @Qualifier("notificacionesMatchQueue") Queue notificacionesMatchQueue,
            @Qualifier("matchExchange") TopicExchange matchExchange
    ) {
        return BindingBuilder
                .bind(notificacionesMatchQueue)
                .to(matchExchange)
                .with(ROUTING_KEY_MATCH_CREATED);
    }

    @Bean
    public TopicExchange comunicacionExchange() {
        return new TopicExchange(EXCHANGE_COMUNICACION);
    }

    @Bean
    public Queue notificacionesMensajeQueue() {
        return new Queue(QUEUE_NOTIFICACIONES_MENSAJE, true);
    }

    @Bean
    public Binding notificacionesMensajeBinding(
            @Qualifier("notificacionesMensajeQueue") Queue notificacionesMensajeQueue,
            @Qualifier("comunicacionExchange") TopicExchange comunicacionExchange
    ) {
        return BindingBuilder
                .bind(notificacionesMensajeQueue)
                .to(comunicacionExchange)
                .with(ROUTING_KEY_MENSAJE_CREADO);
    }


    @Bean
    public Queue notificacionesSquadSolicitudCreadaQueue() {
        return new Queue(QUEUE_NOTIFICACIONES_SQUAD_SOLICITUD_CREADA, true);
    }

    @Bean
    public Binding notificacionesSquadSolicitudCreadaBinding(
            @Qualifier("notificacionesSquadSolicitudCreadaQueue") Queue notificacionesSquadSolicitudCreadaQueue,
            @Qualifier("comunicacionExchange") TopicExchange comunicacionExchange
    ) {
        return BindingBuilder
                .bind(notificacionesSquadSolicitudCreadaQueue)
                .to(comunicacionExchange)
                .with(ROUTING_KEY_SQUAD_SOLICITUD_CREADA);
    }

    @Bean
    public Queue notificacionesSquadSolicitudAceptadaQueue() {
        return new Queue(QUEUE_NOTIFICACIONES_SQUAD_SOLICITUD_ACEPTADA, true);
    }

    @Bean
    public Binding notificacionesSquadSolicitudAceptadaBinding(
            @Qualifier("notificacionesSquadSolicitudAceptadaQueue") Queue notificacionesSquadSolicitudAceptadaQueue,
            @Qualifier("comunicacionExchange") TopicExchange comunicacionExchange
    ) {
        return BindingBuilder
                .bind(notificacionesSquadSolicitudAceptadaQueue)
                .to(comunicacionExchange)
                .with(ROUTING_KEY_SQUAD_SOLICITUD_ACEPTADA);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}