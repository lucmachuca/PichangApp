package com.PichangApp.msvc_notificacion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

@SpringBootApplication
@EnableFeignClients
public class MsvcNotificacionApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsvcNotificacionApplication.class, args);
	}

	// Permite leer mensajes JSON en RabbitMQ sin archivos extra de configuración
	@Bean
	public MessageConverter jsonMessageConverter() {
		return new JacksonJsonMessageConverter();
	}
}
