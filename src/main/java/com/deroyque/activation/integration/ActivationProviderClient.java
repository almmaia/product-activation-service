package com.deroyque.activation.integration;
import com.deroyque.activation.domain.ActivationRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;

@Component
public class ActivationProviderClient {
    private final RestClient client;
    public ActivationProviderClient(RestClient providerRestClient) { this.client = providerRestClient; }
    public void activate(ActivationRequest request) {
        client.post().uri("/provider/v1/activate")
            .header("Idempotency-Key", request.getId().toString())
            .body(new ProviderActivation(request.getId().toString(), request.getCustomerId(), request.getProductCode()))
            .retrieve().toBodilessEntity();
    }
    private record ProviderActivation(String requestId, String customerId, String productCode) { }
    
}
