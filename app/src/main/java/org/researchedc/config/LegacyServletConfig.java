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

            // ── Import servlets ─────────────────────────────────
            registerServlet(servletContext, "ImportCRFData",
                    "org.researchedc.control.submit.ImportCRFDataServlet",
                    "/ImportCRFData");
            registerServlet(servletContext, "ImportCRFInfo",
                    "org.researchedc.control.submit.ImportCRFInfo",
                    "/ImportCRFInfo");

            // ── Export servlets ─────────────────────────────────
            registerServlet(servletContext, "CreateJobImport",
                    "org.researchedc.control.admin.CreateJobImportServlet",
                    "/CreateJobImport");
            registerServlet(servletContext, "ViewImportJob",
                    "org.researchedc.control.admin.ViewImportJobServlet",
                    "/ViewImportJob");
            // ShowFile, ChooseDownloadFormat removed — extract servlets deleted in Phase 1

            log.info("Legacy import/export servlets registered");
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
