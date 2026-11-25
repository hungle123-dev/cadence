package com.cadence.common.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cadence.TestcontainersConfiguration;

@SpringBootTest
@AutoConfigureMockMvc
@Import({TestcontainersConfiguration.class, ObservabilityIntegrationTest.LoggingProbe.class})
@ExtendWith(OutputCaptureExtension.class)
class ObservabilityIntegrationTest {

    @Autowired MockMvc mvc;

    @Test
    @DisplayName("TC-OPS-02 /actuator/health trả UP khi DB sống")
    void health_isUpWhenTheDatabaseIsReachable() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("TC-OPS-02 Endpoint actuator khác health không được mở ra ngoài")
    void actuator_exposesOnlyHealth() throws Exception {
        mvc.perform(get("/actuator/env")).andExpect(status().isNotFound());
        mvc.perform(get("/actuator/metrics")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("TC-OPS-01 Log JSON có requestId trùng với header X-Request-Id trả về")
    void logs_carryTheRequestIdReturnedToTheClient(CapturedOutput output) throws Exception {
        String requestId = "test-req-0001";

        mvc.perform(get("/test/log").header(RequestIdFilter.HEADER, requestId))
                .andExpect(status().isOk())
                .andExpect(header().string(RequestIdFilter.HEADER, requestId));

        String line =
                output.getOut()
                        .lines()
                        .filter(l -> l.contains("logging-probe-message"))
                        .findFirst()
                        .orElseThrow();
        assertThat(line).startsWith("{"); // structured JSON (ECS)
        assertThat(line).contains("\"requestId\":\"" + requestId + "\"");
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class LoggingProbe {

        @Bean
        ProbeController probeController() {
            return new ProbeController();
        }
    }

    @RestController
    static class ProbeController {

        private static final Logger LOG = LoggerFactory.getLogger(ProbeController.class);

        @GetMapping("/test/log")
        String log() {
            LOG.info("logging-probe-message");
            return "ok";
        }
    }
}
