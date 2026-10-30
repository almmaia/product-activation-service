package com.deroyque.activation.service;
import com.deroyque.activation.api.CreateRequest;
import com.deroyque.activation.domain.*;
import com.deroyque.activation.repository.ActivationRequestRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class ActivationRequestService {
    private final ActivationRequestRepository repository;
    private final RequestCreationTransaction creationTransaction;
    public ActivationRequestService(ActivationRequestRepository repository, RequestCreationTransaction creationTransaction) { this.repository = repository; this.creationTransaction = creationTransaction; }
    public Creation create(CreateRequest input) {
        var candidate = ActivationRequest.pending(input.externalId(), input.customerId(), input.productCode());
        var existing = repository.findByExternalId(input.externalId());
        if (existing.isPresent()) return existingResult(existing.get(), input);
        try { return new Creation(creationTransaction.insert(candidate), true); }
        catch (DataIntegrityViolationException exception) {
            return existingResult(repository.findByExternalId(input.externalId()).orElseThrow(() -> exception), input);
        }
    }
    private Creation existingResult(ActivationRequest existing, CreateRequest input) {
        if (!existing.hasSamePayload(input.customerId(), input.productCode())) throw new ConflictingExternalIdException(input.externalId());
        return new Creation(existing, false);
    }
    @Transactional(readOnly = true)
    public ActivationRequest find(UUID id) { return repository.findById(id).orElseThrow(() -> new RequestNotFoundException(id)); }
    @Transactional
    public ActivationRequest markProcessing(UUID id) { var request = find(id); request.startProcessing(); return request; }
    @Transactional
    public ActivationRequest markCompleted(UUID id) { var request = find(id); request.complete(); return request; }
    @Transactional
    public ActivationRequest markFailed(UUID id) { var request = find(id); request.fail(); return request; }
    public record Creation(ActivationRequest request, boolean created) { }
}
