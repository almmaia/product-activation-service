package com.deroyque.activation.service;

import com.deroyque.activation.api.CreateRequest;
import com.deroyque.activation.domain.ActivationRequest;
import com.deroyque.activation.integration.ActivationProviderClient;
import com.deroyque.activation.integration.ProviderCommunicationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivationOrchestratorTest {
    @Mock ActivationRequestService service;
    @Mock ActivationProviderClient provider;
    @InjectMocks ActivationOrchestrator orchestrator;

    @Test void marksRequestFailedForKnownProviderCommunicationFailure() {
        var request = ActivationRequest.pending("EXT-1", "CUST-1", "P-1");
        when(service.create(any())).thenReturn(new ActivationRequestService.Creation(request, true));
        when(service.markProcessing(request.getId())).thenReturn(request);
        when(service.markFailed(request.getId())).thenReturn(request);
        doThrow(new ProviderCommunicationException(new RuntimeException("timeout"))).when(provider).activate(request);

        orchestrator.createAndActivate(new CreateRequest("EXT-1", "CUST-1", "P-1"));

        verify(service).markFailed(request.getId());
        verify(service, never()).markCompleted(request.getId());
    }

    @Test void doesNotConvertPersistenceFailureAfterSuccessIntoProviderFailure() {
        var request = ActivationRequest.pending("EXT-1", "CUST-1", "P-1");
        when(service.create(any())).thenReturn(new ActivationRequestService.Creation(request, true));
        when(service.markProcessing(request.getId())).thenReturn(request);
        doThrow(new DataIntegrityViolationException("database unavailable")).when(service).markCompleted(request.getId());

        assertThatThrownBy(() -> orchestrator.createAndActivate(new CreateRequest("EXT-1", "CUST-1", "P-1")))
            .isInstanceOf(DataIntegrityViolationException.class);

        verify(service, never()).markFailed(request.getId());
    }
}
