package com.deroyque.activation.api;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ActivationFlowIntegrationTest {
    private static final HttpServer provider = createProvider();
    @Autowired MockMvc mvc;

    @DynamicPropertySource
    static void providerProperties(DynamicPropertyRegistry registry) {
        registry.add("provider.base-url", () -> "http://localhost:" + provider.getAddress().getPort());
        registry.add("provider.connect-timeout", () -> "5s");
        registry.add("provider.read-timeout", () -> "5s");
    }
    @BeforeAll static void start() { provider.start(); }
    @AfterAll static void stop() { provider.stop(0); }

    @Test void returnsCompletedWhenProviderAcceptsActivation() throws Exception {
        mvc.perform(post("/api/v1/requests").contentType(MediaType.APPLICATION_JSON).content(payload("SUCCESS-" + UUID.randomUUID(), "PLAN-OK")))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("COMPLETED"));
    }
    @Test void returnsFailedWhenProviderReturnsTreatableError() throws Exception {
        mvc.perform(post("/api/v1/requests").contentType(MediaType.APPLICATION_JSON).content(payload("FAIL-" + UUID.randomUUID(), "PLAN-FAIL")))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("FAILED"));
    }
    @Test void returnsExistingCompletedRequestForSamePayload() throws Exception {
        var externalId = "RETRY-" + UUID.randomUUID();
        mvc.perform(post("/api/v1/requests").contentType(MediaType.APPLICATION_JSON).content(payload(externalId, "PLAN-OK")))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("COMPLETED"));
        mvc.perform(post("/api/v1/requests").contentType(MediaType.APPLICATION_JSON).content(payload(externalId, "PLAN-OK")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("COMPLETED"));
    }
    @Test void rejectsSameExternalIdWithDifferentPayload() throws Exception {
        var externalId = "CONFLICT-" + UUID.randomUUID();
        mvc.perform(post("/api/v1/requests").contentType(MediaType.APPLICATION_JSON).content(payload(externalId, "PLAN-OK"))).andExpect(status().isCreated());
        mvc.perform(post("/api/v1/requests").contentType(MediaType.APPLICATION_JSON).content(payload(externalId, "PLAN-OTHER")))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.title").value("Solicitação duplicada"));
    }
    private static String payload(String externalId, String productCode) { return "{\"externalId\":\"%s\",\"customerId\":\"CUST-1\",\"productCode\":\"%s\"}".formatted(externalId, productCode); }
    private static HttpServer createProvider() {
        try {
            var server = HttpServer.create(new InetSocketAddress(0), 0);
            server.createContext("/provider/v1/activate", exchange -> {
                var body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(body.contains("PLAN-FAIL") ? 500 : 204, -1); exchange.close();
            });
            return server;
        } catch (IOException exception) { throw new IllegalStateException("Could not start provider test server", exception); }
    }
}
