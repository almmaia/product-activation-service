package com.deroyque.activation.api;
import com.deroyque.activation.domain.ActivationRequest;
import com.deroyque.activation.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.util.UUID;

@WebMvcTest(RequestController.class) @Import(ApiExceptionHandler.class)
class RequestControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean ActivationOrchestrator orchestrator;
    @MockitoBean ActivationRequestService service;
    @Test void createsAValidRequest() throws Exception {
        var request = ActivationRequest.pending("EXT-123", "CUST-100", "INTERNET-500");
        request.startProcessing(); request.complete();
        when(orchestrator.createAndActivate(any())).thenReturn(new ActivationRequestService.Creation(request, true));
        mvc.perform(post("/api/v1/requests").contentType("application/json").content("{" + "\"externalId\":\"EXT-123\",\"customerId\":\"CUST-100\",\"productCode\":\"INTERNET-500\"}"))
            .andExpect(status().isCreated()).andExpect(header().string("Location", org.hamcrest.Matchers.containsString(request.getId().toString())))
            .andExpect(jsonPath("$.status").value("COMPLETED"));
    }
    @Test void rejectsInvalidInput() throws Exception {
        mvc.perform(post("/api/v1/requests").contentType("application/json").content("{}"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.title").value("Dados inválidos"));
    }
    @Test void returns404WhenRequestDoesNotExist() throws Exception {
        var id = UUID.randomUUID(); when(service.find(id)).thenThrow(new RequestNotFoundException(id));
        mvc.perform(get("/api/v1/requests/{id}", id)).andExpect(status().isNotFound());
    }
}
