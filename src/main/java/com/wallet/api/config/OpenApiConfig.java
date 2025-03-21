package com.wallet.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI digitalWalletOpenAPI() {
        Server localServer = new Server()
                .url("http://localhost:8080")
                .description("Local Development Server");

        Contact contact = new Contact()
                .name("Digital Wallet API Team")
                .email("support@wallet-api.com");

        License license = new License()
                .name("Apache 2.0")
                .url("https://www.apache.org/licenses/LICENSE-2.0");

        Info info = new Info()
                .title("Digital Wallet API")
                .description("REST API for Digital Wallet Service")
                .version("1.0.0")
                .contact(contact)
                .license(license)
                .termsOfService("https://wallet-api.com/terms");

        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer));
    }
} 