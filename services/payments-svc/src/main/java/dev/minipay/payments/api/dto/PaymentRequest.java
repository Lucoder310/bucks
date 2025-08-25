package dev.minipay.payments.api.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentRequest(
  @NotBlank String debtorName,
  @NotBlank String debtorAccountId,
  @NotBlank String creditorName,
  @NotBlank String creditorAccountId,
  @NotNull @Positive BigDecimal amount,
  @NotBlank String currency,
  @NotBlank String endToEndId,
  @NotNull LocalDate requestedExecutionDate
) {}
