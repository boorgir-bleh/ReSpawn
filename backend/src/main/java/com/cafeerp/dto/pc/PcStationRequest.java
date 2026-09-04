package com.cafeerp.dto.pc;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PcStationRequest(

        @NotBlank(message = "Label is required")
        String label,

        @NotBlank(message = "CPU is required")
        String cpu,

        @NotBlank(message = "GPU is required")
        String gpu,

        @NotBlank(message = "RAM is required")
        String ram,

        @NotBlank(message = "Monitor is required")
        String monitor,

        String peripherals,

        @NotNull(message = "Hourly rate is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Hourly rate must be greater than 0")
        @Digits(integer = 8, fraction = 2, message = "Hourly rate must have at most 2 decimal places")
        BigDecimal hourlyRate,

        boolean active
) {
}
