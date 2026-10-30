package com.deroyque.activation.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "activation_requests")
public class ActivationRequest {
    @Id private UUID id;
    @Column(name = "external_id", nullable = false, unique = true, length = 100) private String externalId;
    @Column(name = "customer_id", nullable = false, length = 100) private String customerId;
    @Column(name = "product_code", nullable = false, length = 100) private String productCode;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private RequestStatus status;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @Version private long version;

    protected ActivationRequest() { }
    private ActivationRequest(String externalId, String customerId, String productCode) {
        id = UUID.randomUUID(); this.externalId = externalId; this.customerId = customerId; this.productCode = productCode;
        status = RequestStatus.PENDING; createdAt = Instant.now(); updatedAt = createdAt;
    }
    public static ActivationRequest pending(String externalId, String customerId, String productCode) {
        return new ActivationRequest(externalId, customerId, productCode);
    }
    public void startProcessing() { transition(RequestStatus.PENDING, RequestStatus.PROCESSING); }
    public void complete() { transition(RequestStatus.PROCESSING, RequestStatus.COMPLETED); }
    public void fail() { transition(RequestStatus.PROCESSING, RequestStatus.FAILED); }
    private void transition(RequestStatus expected, RequestStatus target) {
        if (status != expected) throw new InvalidStatusTransitionException(status, target);
        status = target; updatedAt = Instant.now();
    }
    public boolean hasSamePayload(String customerId, String productCode) { return this.customerId.equals(customerId) && this.productCode.equals(productCode); }
    public UUID getId() { return id; } public String getExternalId() { return externalId; } public String getCustomerId() { return customerId; }
    public String getProductCode() { return productCode; } public RequestStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; } public Instant getUpdatedAt() { return updatedAt; }
}
