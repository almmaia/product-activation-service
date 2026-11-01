package com.deroyque.activation.integration;

/** Erro de rede, conexão ou timeout ao chamar o provedor. */
public final class ProviderCommunicationException extends ProviderIntegrationException {
    public ProviderCommunicationException(Throwable cause) {
        super("Não foi possível comunicar com o provedor de ativação", cause);
    }
}
