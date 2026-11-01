package com.deroyque.activation.service;
import com.deroyque.activation.api.CreateRequest;
import com.deroyque.activation.integration.ActivationProviderClient;
import com.deroyque.activation.integration.ProviderIntegrationException;
import com.deroyque.activation.domain.ActivationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class ActivationOrchestrator {
    private static final Logger log = LoggerFactory.getLogger(ActivationOrchestrator.class);
    private final ActivationRequestService service; private final ActivationProviderClient provider;
    public ActivationOrchestrator(ActivationRequestService service, ActivationProviderClient provider) { this.service = service; this.provider = provider; }
    public ActivationRequestService.Creation createAndActivate(CreateRequest input) {
        var result = service.create(input);
        if (!result.created()) return result;
        return new ActivationRequestService.Creation(activate(result.request().getId()), true);
    }
    private ActivationRequest activate(UUID id) {
        var processing = service.markProcessing(id);
        try {
            provider.activate(processing);
            return service.markCompleted(id);
        } catch (ProviderIntegrationException exception) {
            log.warn("Activation provider failed for requestId={} ({})", id, exception.getMessage());
            return service.markFailed(id);
        }
    }
}
