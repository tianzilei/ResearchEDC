package org.researchedc.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openClinicaOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("OpenClinica API")
                .description("REST API for the OpenClinica Electronic Data Capture system. "
                    + "Provides endpoints for study management, clinical data capture, "
                    + "and system administration.")
                .version("3.18.0-SNAPSHOT")
                .contact(new Contact()
                    .name("OpenClinica Community")
                    .url("https://www.openclinica.com")));
    }
}
