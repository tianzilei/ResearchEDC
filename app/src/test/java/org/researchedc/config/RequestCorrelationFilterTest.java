package org.researchedc.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.concurrent.atomic.AtomicReference;

import jakarta.servlet.FilterChain;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RequestCorrelationFilterTest {

    private final RequestCorrelationFilter filter = new RequestCorrelationFilter();

    @Test
    void doFilterInternal_usesSafeIncomingRequestId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/dashboard/status");
        request.addHeader(RequestCorrelationFilter.HEADER_NAME, "operator-123");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> mdcValue = new AtomicReference<>();

        filter.doFilter(request, response, captureMdc(mdcValue));

        assertEquals("operator-123", response.getHeader(RequestCorrelationFilter.HEADER_NAME));
        assertEquals("operator-123", mdcValue.get());
        assertNull(MDC.get(RequestCorrelationFilter.MDC_KEY));
    }

    @Test
    void doFilterInternal_generatesRequestIdWhenMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/dashboard/status");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> mdcValue = new AtomicReference<>();

        filter.doFilter(request, response, captureMdc(mdcValue));

        String responseRequestId = response.getHeader(RequestCorrelationFilter.HEADER_NAME);
        assertNotNull(responseRequestId);
        assertEquals(responseRequestId, mdcValue.get());
        assertNull(MDC.get(RequestCorrelationFilter.MDC_KEY));
    }

    @Test
    void doFilterInternal_replacesUnsafeIncomingRequestId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/dashboard/status");
        request.addHeader(RequestCorrelationFilter.HEADER_NAME, "bad id with spaces");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> mdcValue = new AtomicReference<>();

        filter.doFilter(request, response, captureMdc(mdcValue));

        String responseRequestId = response.getHeader(RequestCorrelationFilter.HEADER_NAME);
        assertNotNull(responseRequestId);
        assertNotEquals("bad id with spaces", responseRequestId);
        assertEquals(responseRequestId, mdcValue.get());
        assertNull(MDC.get(RequestCorrelationFilter.MDC_KEY));
    }

    private static FilterChain captureMdc(AtomicReference<String> mdcValue) {
        return (request, response) -> mdcValue.set(MDC.get(RequestCorrelationFilter.MDC_KEY));
    }
}
