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

            log.info("Legacy servlet registration complete (import servlets decommissioned)");
        };
    }

    private void registerServlet(jakarta.servlet.ServletContext ctx,
                                  String name, String className, String... mappings) {
        try {
            Class<?> clazz = Class.forName(className);
            jakarta.servlet.ServletRegistration.Dynamic reg =
                    ctx.addServlet(name, (Class<? extends jakarta.servlet.Servlet>) clazz);
            if (reg != null) {
                reg.addMapping(mappings);
                log.debug("Registered servlet: {} -> {}", name, String.join(", ", mappings));
            }
        } catch (ClassNotFoundException e) {
            log.warn("Servlet class not found, skipping: {} ({})", name, className);
        } catch (Exception e) {
            log.error("Failed to register servlet {}: {}", name, e.getMessage());
        }
    }
}
