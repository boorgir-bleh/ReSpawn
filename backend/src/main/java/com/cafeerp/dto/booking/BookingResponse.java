package com.cafeerp.dto.booking;

import com.cafeerp.entity.Booking;
import com.cafeerp.entity.BookingStatus;
import com.cafeerp.entity.PaymentMode;

import java.math.BigDecimal;
import java.time.Instant;

public record BookingResponse(
        Long id,
        Long userId,
        String userFullName,
        Long pcStationId,
        String pcStationLabel,
        Instant startTime,
        Instant endTime,
        int durationMinutes,
        BigDecimal amount,
        PaymentMode paymentMode,
        BookingStatus status,
        Instant createdAt
) {
    public static BookingResponse from(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getUser().getId(),
                booking.getUser().getFullName(),
                booking.getPcStation().getId(),
                booking.getPcStation().getLabel(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getDurationMinutes(),
                booking.getAmount(),
                booking.getPaymentMode(),
                booking.getStatus(),
                booking.getCreatedAt()
        );
    }
}
