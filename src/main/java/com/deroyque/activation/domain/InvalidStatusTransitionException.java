package com.deroyque.activation.domain;
public class InvalidStatusTransitionException extends RuntimeException {
    public InvalidStatusTransitionException(RequestStatus from, RequestStatus to) { super("Transição inválida: " + from + " para " + to); }
}
