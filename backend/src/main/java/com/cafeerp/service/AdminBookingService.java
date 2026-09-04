package com.cafeerp.service;

import com.cafeerp.dto.booking.BookingResponse;
import com.cafeerp.entity.Booking;
import com.cafeerp.entity.BookingStatus;
import com.cafeerp.entity.PaymentMode;
import com.cafeerp.exception.BadRequestException;
import com.cafeerp.exception.ConflictException;
import com.cafeerp.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminBookingService {

    private final BookingRepository bookingRepository;
    private final BookingService bookingService;

    public List<BookingResponse> listBookings(BookingStatus statusFilter) {
        List<Booking> bookings = statusFilter != null
                ? bookingRepository.findByStatusOrderByStartTimeDesc(statusFilter)
                : bookingRepository.findAllByOrderByStartTimeDesc();

        return bookings.stream().map(BookingResponse::from).toList();
    }

    public BookingResponse getBooking(Long bookingId) {
        return BookingResponse.from(bookingService.findEntity(bookingId));
    }

    @Transactional
    public BookingResponse markPaid(Long bookingId) {
        Booking booking = bookingService.findEntity(bookingId);

        if (booking.getPaymentMode() != PaymentMode.PREPAID_QR) {
            throw new BadRequestException("Only prepaid bookings can be marked as paid");
        }
        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new ConflictException("Only bookings pending payment can be marked as paid");
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        return BookingResponse.from(booking);
    }

    @Transactional
    public BookingResponse cancelBooking(Long bookingId) {
        Booking booking = bookingService.findEntity(bookingId);

        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.COMPLETED) {
            throw new ConflictException("This booking can no longer be cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        return BookingResponse.from(booking);
    }

    @Transactional
    public BookingResponse completeBooking(Long bookingId) {
        Booking booking = bookingService.findEntity(bookingId);

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new ConflictException("Only confirmed bookings can be marked as completed");
        }

        booking.setStatus(BookingStatus.COMPLETED);
        return BookingResponse.from(booking);
    }
}
