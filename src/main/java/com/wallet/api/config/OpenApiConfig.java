package com.wallet.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
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

        // Define security scheme
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");
                
        // Add the security scheme to components
        Components components = new Components()
                .addSecuritySchemes("bearerAuth", securityScheme);
                
        // Create a security requirement
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("bearerAuth");
                
        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer))
                .components(components)
                .addSecurityItem(securityRequirement);
    }
} 