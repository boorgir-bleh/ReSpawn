package com.cafeerp.controller;

import com.cafeerp.dto.booking.BookingCreatedResponse;
import com.cafeerp.dto.booking.BookingRequest;
import com.cafeerp.dto.booking.BookingResponse;
import com.cafeerp.security.UserPrincipal;
import com.cafeerp.service.BookingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Create and manage your own bookings")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingCreatedResponse> createBooking(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody BookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingService.createBooking(principal.getId(), request));
    }

    @GetMapping("/me")
    public ResponseEntity<List<BookingResponse>> listMyBookings(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(bookingService.listMyBookings(principal.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getMyBooking(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getMyBooking(principal.getId(), id));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancelMyBooking(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        return ResponseEntity.ok(bookingService.cancelMyBooking(principal.getId(), id));
    }
}
