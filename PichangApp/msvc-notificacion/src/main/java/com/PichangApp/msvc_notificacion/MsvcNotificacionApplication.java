package com.PichangApp.msvc_notificacion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MsvcNotificacionApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsvcNotificacionApplication.class, args);
	}

}
