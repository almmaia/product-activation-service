package com.deroyque.activation.service;
public class ConflictingExternalIdException extends RuntimeException { public ConflictingExternalIdException(String externalId) { super("externalId já foi usado com dados diferentes: " + externalId); } }
