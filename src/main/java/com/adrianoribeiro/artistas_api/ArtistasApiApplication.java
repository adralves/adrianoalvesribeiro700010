package com.adrianoribeiro.artistas_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = "com.adrianoribeiro.artistas_api.client")
@SpringBootApplication
public class ArtistasApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ArtistasApiApplication.class, args);
	}

}
