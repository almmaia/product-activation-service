package com.deroyque.activation.repository;
import com.deroyque.activation.domain.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface ActivationRequestRepository extends JpaRepository<ActivationRequest, UUID> {
    Optional<ActivationRequest> findByExternalId(String externalId);
    Page<ActivationRequest> findByStatusAndCustomerId(RequestStatus status, String customerId, Pageable pageable);
    Page<ActivationRequest> findByStatus(RequestStatus status, Pageable pageable);
    Page<ActivationRequest> findByCustomerId(String customerId, Pageable pageable);
}
