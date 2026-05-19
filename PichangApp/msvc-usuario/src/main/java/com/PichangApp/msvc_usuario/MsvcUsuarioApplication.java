package com.PichangApp.msvc_usuario;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.PichangApp.msvc_usuario.repositories")
public class MsvcUsuarioApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsvcUsuarioApplication.class, args);
	}

}
