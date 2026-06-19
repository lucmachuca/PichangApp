package com.PichangApp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ComunicacionServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ComunicacionServiceApplication.class, args);
	}
}