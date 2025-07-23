package com.ff.clients_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
@EnableDiscoveryClient
public class ClientsServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClientsServiceApplication.class, args);
		//BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
		//String password = "admin123";
		// Password: $2a$10$9tHkO9ppnjlijYq2TTQ3v.sWa8O5areo8u2Hod8nJ73SaTll.onsO
		//String encodedPassword = bCryptPasswordEncoder.encode(password);
		//System.out.println("Encoded password: " + encodedPassword);
	}

}
