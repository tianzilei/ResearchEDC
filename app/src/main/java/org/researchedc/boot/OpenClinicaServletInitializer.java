package org.researchedc.boot;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.web.context.WebApplicationContext;

public class OpenClinicaServletInitializer extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(OpenClinicaApplication.class);
    }

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        Object existing = servletContext.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        if (existing != null) {
            return;
        }
        super.onStartup(servletContext);
    }
}
