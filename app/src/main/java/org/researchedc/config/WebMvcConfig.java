package org.researchedc.config;

import java.io.IOException;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Frontend build assets (JS/CSS/fonts) — served at root /assets/
        registry
            .addResourceHandler("/assets/**")
            .addResourceLocations(
                "file:./frontend/dist/assets/",
                "classpath:/static/assets/"
            )
            .setCachePeriod(31536000);

        // SPA routes — serve index.html for any /app/* path (client-side routing)
        registry
            .addResourceHandler("/app/**")
            .addResourceLocations(
                "file:./frontend/dist/",
                "classpath:/static/"
            )
            .resourceChain(true)
            .addResolver(new PathResourceResolver() {
                @Override
                protected Resource getResource(String resourcePath, Resource location) {
                    try {
                        Resource resource = location.createRelative(resourcePath);
                        if (resource.exists() && resource.isReadable()) {
                            return resource;
                        }
                    } catch (IOException e) {
                        // Fall through to index.html below
                    }
                    try {
                        Resource indexCandidate = location.createRelative("index.html");
                        if (indexCandidate.exists() && indexCandidate.isReadable()) {
                            return indexCandidate;
                        }
                    } catch (IOException e) {
                        // Fall through to classpath fallback below
                    }
                    return new ClassPathResource("/static/index.html");
                }
            });
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/app").setViewName("forward:/app/index.html");
        registry.addViewController("/app/").setViewName("forward:/app/index.html");
        registry.addRedirectViewController("/login", "/app/login");
        registry.addRedirectViewController("/", "/app/login");
        registry.addRedirectViewController("/ChangeStudy", "/app/change-study");
        registry.addRedirectViewController("/UpdateProfile", "/app/profile");
        registry.addRedirectViewController("/ManageStudy", "/app/dashboard");
        registry.addRedirectViewController("/ListStudySubjects", "/app/subjects");
        registry.addRedirectViewController("/ImportCRFData", "/app/admin/import");

        // Legacy OpenRosa → new Modulith module route bridge
        registry.addRedirectViewController("/openrosa", "/api/v1/openrosa");
        registry.addRedirectViewController("/rest2/openrosa", "/api/v1/openrosa");
    }
}
