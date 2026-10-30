package com.deroyque.activation.service;

import com.deroyque.activation.domain.ActivationRequest;
import com.deroyque.activation.repository.ActivationRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
class RequestCreationTransaction {
    private final ActivationRequestRepository repository;
    RequestCreationTransaction(ActivationRequestRepository repository) { this.repository = repository; }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    ActivationRequest insert(ActivationRequest request) { return repository.saveAndFlush(request); }
}
