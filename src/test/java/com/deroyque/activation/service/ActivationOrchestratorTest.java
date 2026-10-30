package com.deroyque.activation.service;
import com.deroyque.activation.api.CreateRequest;
import com.deroyque.activation.domain.ActivationRequest;
import com.deroyque.activation.integration.ActivationProviderClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivationOrchestratorTest {
    @Mock ActivationRequestService service; @Mock ActivationProviderClient provider;
    @InjectMocks ActivationOrchestrator orchestrator;
    @Test void completesRequestWhenProviderSucceeds() {
        var request = ActivationRequest.pending("EXT-1", "CUST-1", "P-1");
        when(service.create(any())).thenReturn(new ActivationRequestService.Creation(request, true)); when(service.markProcessing(request.getId())).thenReturn(request);
        orchestrator.createAndActivate(new CreateRequest("EXT-1", "CUST-1", "P-1"));
        verify(provider).activate(request); verify(service).markCompleted(request.getId()); verify(service, never()).markFailed(request.getId());
    }
    @Test void failsRequestWhenProviderTimesOutOrErrors() {
        var request = ActivationRequest.pending("EXT-1", "CUST-1", "P-1");
        when(service.create(any())).thenReturn(new ActivationRequestService.Creation(request, true)); when(service.markProcessing(request.getId())).thenReturn(request);
        doThrow(new RuntimeException("timeout")).when(provider).activate(request);
        orchestrator.createAndActivate(new CreateRequest("EXT-1", "CUST-1", "P-1"));
        verify(service).markFailed(request.getId()); verify(service, never()).markCompleted(request.getId());
    }
}
