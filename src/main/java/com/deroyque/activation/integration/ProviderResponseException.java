package com.deroyque.activation.integration;

import org.springframework.http.HttpStatusCode;

/** Resposta HTTP não bem-sucedida retornada pelo provedor. */
public final class ProviderResponseException extends ProviderIntegrationException {
    private final HttpStatusCode statusCode;

    public ProviderResponseException(HttpStatusCode statusCode, Throwable cause) {
        super("O provedor de ativação respondeu com HTTP " + statusCode.value(), cause);
        this.statusCode = statusCode;
    }

    public HttpStatusCode getStatusCode() {
        return statusCode;
    }
}
