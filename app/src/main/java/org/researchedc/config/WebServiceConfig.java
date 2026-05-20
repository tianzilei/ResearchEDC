package org.researchedc.config;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ws.transport.http.MessageDispatcherServlet;

/**
 * Registers the Spring WS MessageDispatcherServlet for SOAP web service endpoints.
 * This replaces the ws-servlet-config.xml declaration from the legacy ws module.
 */
@Configuration
public class WebServiceConfig {

    @Bean
    public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet() {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setTransformWsdlLocations(true);
        ServletRegistrationBean<MessageDispatcherServlet> registration =
            new ServletRegistrationBean<>(servlet, "/ws/*");
        registration.setName("ws");
        registration.setLoadOnStartup(1);
        return registration;
    }
}
