package com.deroyque.activation.service;
import com.deroyque.activation.api.CreateRequest;
import com.deroyque.activation.integration.ActivationProviderClient;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class ActivationOrchestrator {
    private final ActivationRequestService service; private final ActivationProviderClient provider;
    public ActivationOrchestrator(ActivationRequestService service, ActivationProviderClient provider) { this.service = service; this.provider = provider; }
    public ActivationRequestService.Creation createAndActivate(CreateRequest input) {
        var result = service.create(input);
        if (result.created()) activate(result.request().getId());
        return result;
    }
    private void activate(UUID id) {
        var processing = service.markProcessing(id);
        try { provider.activate(processing); service.markCompleted(id); }
        catch (Exception exception) { service.markFailed(id); }
    }
}
