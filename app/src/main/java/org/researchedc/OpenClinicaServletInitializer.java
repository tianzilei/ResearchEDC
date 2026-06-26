package org.researchedc;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Servlet initializer for traditional WAR deployment to Tomcat.
 * Enables the same Spring Boot application to be deployed as a WAR
 * file when not using the embedded container.
 */
public class OpenClinicaServletInitializer extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(OpenClinicaApplication.class);
    }
}
