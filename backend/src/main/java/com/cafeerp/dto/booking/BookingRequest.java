package com.cafeerp.dto.booking;

import com.cafeerp.entity.PaymentMode;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record BookingRequest(

        @NotNull(message = "PC station is required")
        Long pcStationId,

        @NotNull(message = "Start time is required")
        @Future(message = "Start time must be in the future")
        Instant startTime,

        @NotNull(message = "Duration is required")
        @Min(value = 15, message = "Duration must be at least 15 minutes")
        @Max(value = 720, message = "Duration must be at most 720 minutes")
        Integer durationMinutes,

        @NotNull(message = "Payment mode is required")
        PaymentMode paymentMode
) {
}
