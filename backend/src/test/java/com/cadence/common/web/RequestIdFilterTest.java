package com.cadence.common.web;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class RequestIdFilterTest {

    final RequestIdFilter filter = new RequestIdFilter();

    @Test
    void doFilter_keepsASafeIncomingIdAndPutsItInTheLogContext() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(RequestIdFilter.HEADER, "nginx-abc-123");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> seenInChain = new AtomicReference<>();

        filter.doFilter(request, response, new MockFilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res) {
                seenInChain.set(MDC.get(RequestIdFilter.MDC_KEY));
            }
        });

        assertThat(seenInChain.get()).isEqualTo("nginx-abc-123");
        assertThat(response.getHeader(RequestIdFilter.HEADER)).isEqualTo("nginx-abc-123");
        assertThat(MDC.get(RequestIdFilter.MDC_KEY)).isNull();   // cleaned up after the request
    }

    @Test
    void doFilter_replacesAnUnsafeIdSoClientsCannotInjectIntoLogs() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(RequestIdFilter.HEADER, "bad\nINFO forged log line");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getHeader(RequestIdFilter.HEADER))
                .isNotBlank()
                .doesNotContain("\n")
                .doesNotContain("forged");
    }

    @Test
    void doFilter_generatesAnIdWhenNoneIsSent() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(new MockHttpServletRequest(), response, new MockFilterChain());

        assertThat(response.getHeader(RequestIdFilter.HEADER)).hasSize(36);   // UUID
    }
}
