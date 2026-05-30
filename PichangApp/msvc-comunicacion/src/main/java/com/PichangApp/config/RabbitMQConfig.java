package com.PichangApp.config;

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

    public static final String QUEUE_MATCH_CREATED = "match.creado.queue";
    public static final String EXCHANGE_MATCH = "match.exchange";
    public static final String ROUTING_KEY_MATCH = "match.created";

    @Bean
    public Queue matchCreatedQueue() {
        /* durable = true asegura que la cola sobreviva a reinicios */
        Queue queue = new Queue(QUEUE_MATCH_CREATED, true);
        return queue;
    }

    @Bean
    public TopicExchange matchExchange() {
        return new TopicExchange(EXCHANGE_MATCH);
    }

    @Bean
    public Binding matchBinding(Queue matchCreatedQueue, TopicExchange matchExchange) {
        return BindingBuilder.bind(matchCreatedQueue).to(matchExchange).with(ROUTING_KEY_MATCH);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
