package com.deroyque.activation.service;

import com.deroyque.activation.api.CreateRequest;
import com.deroyque.activation.repository.ActivationRequestRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ActivationRequestServiceConcurrencyTest {
    @Autowired ActivationRequestService service;
    @Autowired ActivationRequestRepository repository;
    @Test void concurrentIdenticalCreatesProduceOneDatabaseRecord() throws Exception {
        var externalId = "CONCURRENT-" + UUID.randomUUID();
        var input = new CreateRequest(externalId, "CUST-1", "PLAN-1");
        try (var executor = Executors.newFixedThreadPool(6)) {
            var tasks = new ArrayList<Callable<ActivationRequestService.Creation>>();
            for (int i = 0; i < 6; i++) tasks.add(() -> service.create(input));
            var results = executor.invokeAll(tasks).stream().map(future -> {
                try { return future.get(); } catch (Exception exception) { throw new AssertionError(exception); }
            }).toList();
            assertThat(results.stream().filter(ActivationRequestService.Creation::created)).hasSize(1);
            assertThat(repository.findByExternalId(externalId)).isPresent();
        }
    }
}
