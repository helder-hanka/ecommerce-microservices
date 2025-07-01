package com.ff.paiements_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class Swagger {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("FF Client Service API").version("1.0.0")
                        .description("FF Commande Service API")
                        .contact(new Contact().name("HELDER").email("hfernandes@238gmail.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(Arrays.asList(new Server().url("http://localhost:8082").description("Serveur development"),
                        new Server().url("https://api.ff.com").description("Serveur production")));
    }
}
