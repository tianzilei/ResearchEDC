package org.researchedc.config;

import jakarta.servlet.http.HttpServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LegacyServletConfig {

    private static final Logger log = LoggerFactory.getLogger(LegacyServletConfig.class);

    @Bean
    public ServletContextInitializer legacyServlets() {
        return servletContext -> {
            // Test servlet
            HttpServlet testServlet = new HttpServlet() {
                @Override
                protected void doGet(jakarta.servlet.http.HttpServletRequest req,
                                     jakarta.servlet.http.HttpServletResponse resp) throws java.io.IOException {
                    resp.setContentType("text/plain");
                    resp.getWriter().write("Hello from test!");
                }
            };
            jakarta.servlet.ServletRegistration.Dynamic tr = servletContext.addServlet("testServlet", testServlet);
            if (tr != null) tr.addMapping("/test");
            // MainMenu removed - handled via WebMvcConfig redirect
            log.info("testServlet registered");
        };
    }
}
