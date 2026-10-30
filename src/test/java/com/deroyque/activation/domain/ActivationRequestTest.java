package com.deroyque.activation.domain;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
class ActivationRequestTest {
    @Test void onlyAllowsTheDefinedStateTransitions() {
        var request = ActivationRequest.pending("EXT-1", "CUST-1", "PLAN-1");
        assertThatThrownBy(request::complete).isInstanceOf(InvalidStatusTransitionException.class);
        request.startProcessing(); request.complete();
        assertThat(request.getStatus()).isEqualTo(RequestStatus.COMPLETED);
        assertThatThrownBy(request::startProcessing).isInstanceOf(InvalidStatusTransitionException.class);
    }
}
