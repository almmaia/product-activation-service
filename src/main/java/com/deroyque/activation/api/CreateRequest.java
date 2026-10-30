package com.deroyque.activation.api;
import jakarta.validation.constraints.*;
public record CreateRequest(
    @NotBlank @Size(max = 100) @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "deve conter apenas letras, números, hífen ou sublinhado") String externalId,
    @NotBlank @Size(max = 100) @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "deve conter apenas letras, números, hífen ou sublinhado") String customerId,
    @NotBlank @Size(max = 100) @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "deve conter apenas letras, números, hífen ou sublinhado") String productCode) { }
