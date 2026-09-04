package com.cafeerp.dto.booking;

public record BookingCreatedResponse(
        BookingResponse booking,
        String qrCodeBase64PNG,
        String upiPaymentLink
) {
}
