package com.deroyque.activation.api;
import com.deroyque.activation.domain.*;
import java.time.Instant;
import java.util.UUID;
public record RequestResponse(UUID id, String externalId, String customerId, String productCode, RequestStatus status, Instant createdAt, Instant updatedAt) {
    public static RequestResponse from(ActivationRequest r) { return new RequestResponse(r.getId(), r.getExternalId(), r.getCustomerId(), r.getProductCode(), r.getStatus(), r.getCreatedAt(), r.getUpdatedAt()); }
}
