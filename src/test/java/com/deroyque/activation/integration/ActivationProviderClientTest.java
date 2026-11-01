package com.deroyque.activation.integration;

import com.deroyque.activation.domain.ActivationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ActivationProviderClientTest {
    @Test
    void mapsConnectionFailureToProviderCommunicationException() {
        var client = new ActivationProviderClient(RestClient.builder().baseUrl("http://127.0.0.1:1").build());
        var request = ActivationRequest.pending("EXT-1", "CUST-1", "PLAN-1");

        assertThatThrownBy(() -> client.activate(request))
            .isInstanceOf(ProviderCommunicationException.class);
    }
}
