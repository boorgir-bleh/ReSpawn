package com.cafeerp.controller;

import com.cafeerp.dto.booking.BookingResponse;
import com.cafeerp.entity.BookingStatus;
import com.cafeerp.service.AdminBookingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/bookings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Bookings", description = "Admin management of all bookings")
public class AdminBookingController {

    private final AdminBookingService adminBookingService;

    @GetMapping
    public List<BookingResponse> listBookings(@RequestParam(required = false) BookingStatus status) {
        return adminBookingService.listBookings(status);
    }

    @GetMapping("/{id}")
    public BookingResponse getBooking(@PathVariable Long id) {
        return adminBookingService.getBooking(id);
    }

    @PostMapping("/{id}/mark-paid")
    public BookingResponse markPaid(@PathVariable Long id) {
        return adminBookingService.markPaid(id);
    }

    @PostMapping("/{id}/cancel")
    public BookingResponse cancelBooking(@PathVariable Long id) {
        return adminBookingService.cancelBooking(id);
    }

    @PostMapping("/{id}/complete")
    public BookingResponse completeBooking(@PathVariable Long id) {
        return adminBookingService.completeBooking(id);
    }
}
