package org.researchedc.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

class ApiExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ThrowingController())
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void accessDenied_returnsForbiddenWithRequestId() throws Exception {
        MDC.put(RequestCorrelationFilter.MDC_KEY, "req-403");
        try {
            mockMvc.perform(get("/denied"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403))
                    .andExpect(jsonPath("$.error").value("Forbidden"))
                    .andExpect(jsonPath("$.message").value("No access"))
                    .andExpect(jsonPath("$.path").value("/denied"))
                    .andExpect(jsonPath("$.requestId").value("req-403"));
        } finally {
            MDC.remove(RequestCorrelationFilter.MDC_KEY);
        }
    }

    @Test
    void notFound_returnsNotFoundWithRequestId() throws Exception {
        MDC.put(RequestCorrelationFilter.MDC_KEY, "req-404");
        try {
            mockMvc.perform(get("/missing"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").value("Missing record"))
                    .andExpect(jsonPath("$.path").value("/missing"))
                    .andExpect(jsonPath("$.requestId").value("req-404"));
        } finally {
            MDC.remove(RequestCorrelationFilter.MDC_KEY);
        }
    }

    @Test
    void illegalArgument_returnsBadRequestWithRequestId() throws Exception {
        MDC.put(RequestCorrelationFilter.MDC_KEY, "req-400");
        try {
            mockMvc.perform(get("/bad-request"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message").value("Invalid input"))
                    .andExpect(jsonPath("$.path").value("/bad-request"))
                    .andExpect(jsonPath("$.requestId").value("req-400"));
        } finally {
            MDC.remove(RequestCorrelationFilter.MDC_KEY);
        }
    }

    @RestController
    private static class ThrowingController {
        @GetMapping("/denied")
        String denied() {
            throw new AccessDeniedException("No access");
        }

        @GetMapping("/missing")
        String missing() {
            throw new NoSuchElementException("Missing record");
        }

        @GetMapping("/bad-request")
        String badRequest() {
            throw new IllegalArgumentException("Invalid input");
        }
    }
}
