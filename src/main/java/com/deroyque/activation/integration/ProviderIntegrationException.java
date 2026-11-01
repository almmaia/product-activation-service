package com.deroyque.activation.integration;

/** Falha conhecida ao comunicar com o provedor de ativação. */
public abstract class ProviderIntegrationException extends RuntimeException {
    protected ProviderIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
