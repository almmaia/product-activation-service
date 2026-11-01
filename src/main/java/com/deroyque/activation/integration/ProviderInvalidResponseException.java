package com.deroyque.activation.integration;

/** Resposta do provedor que não pôde ser interpretada pelo cliente HTTP. */
public final class ProviderInvalidResponseException extends ProviderIntegrationException {
    public ProviderInvalidResponseException(Throwable cause) {
        super("O provedor retornou uma resposta inválida", cause);
    }
}
