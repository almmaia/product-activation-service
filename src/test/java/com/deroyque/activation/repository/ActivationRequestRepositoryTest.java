package com.deroyque.activation.repository;
import com.deroyque.activation.domain.ActivationRequest;
import com.deroyque.activation.ProductActivationApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.dao.DataIntegrityViolationException;
import static org.assertj.core.api.Assertions.*;
@DataJpaTest @ContextConfiguration(classes = ProductActivationApplication.class)
class ActivationRequestRepositoryTest {
    @Autowired ActivationRequestRepository repository;
    @Test void databaseRejectsDuplicateExternalId() {
        repository.saveAndFlush(ActivationRequest.pending("EXT-UNIQUE", "C1", "P1"));
        assertThatThrownBy(() -> repository.saveAndFlush(ActivationRequest.pending("EXT-UNIQUE", "C1", "P1"))).isInstanceOf(DataIntegrityViolationException.class);
    }
}
