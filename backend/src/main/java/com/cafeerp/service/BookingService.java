package com.cafeerp.service;

import com.cafeerp.dto.booking.BookingCreatedResponse;
import com.cafeerp.dto.booking.BookingRequest;
import com.cafeerp.dto.booking.BookingResponse;
import com.cafeerp.dto.pc.BusySlotResponse;
import com.cafeerp.entity.Booking;
import com.cafeerp.entity.BookingStatus;
import com.cafeerp.entity.PaymentMode;
import com.cafeerp.entity.PcStation;
import com.cafeerp.entity.PcStatus;
import com.cafeerp.entity.User;
import com.cafeerp.exception.BadRequestException;
import com.cafeerp.exception.ConflictException;
import com.cafeerp.exception.ResourceNotFoundException;
import com.cafeerp.repository.BookingRepository;
import com.cafeerp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingService {

    private static final List<BookingStatus> ACTIVE_STATUSES = List.of(
            BookingStatus.PENDING_PAYMENT, BookingStatus.CONFIRMED);

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final PcStationService pcStationService;
    private final QrCodeService qrCodeService;

    @Transactional
    public BookingCreatedResponse createBooking(Long userId, BookingRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No user found with id " + userId));

        // Locks the PC station row so a concurrent booking request for the same PC can't pass
        // the overlap check below before this transaction commits (see findEntityForUpdate).
        PcStation pcStation = pcStationService.findEntityForUpdate(request.pcStationId());

        if (!pcStation.isActive()) {
            throw new BadRequestException("This PC station is not currently bookable");
        }
        if (pcStation.getStatus() == PcStatus.OFFLINE || pcStation.getStatus() == PcStatus.MAINTENANCE) {
            throw new BadRequestException("This PC station is offline or under maintenance");
        }

        Instant startTime = request.startTime();
        Instant endTime = startTime.plus(Duration.ofMinutes(request.durationMinutes()));

        List<Booking> overlaps = bookingRepository.findOverlappingBookings(
                pcStation.getId(), startTime, endTime, ACTIVE_STATUSES);
        if (!overlaps.isEmpty()) {
            throw new ConflictException("This PC is already booked for the requested time slot");
        }

        BigDecimal hours = BigDecimal.valueOf(request.durationMinutes())
                .divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);
        BigDecimal amount = pcStation.getHourlyRate().multiply(hours).setScale(2, RoundingMode.HALF_UP);

        BookingStatus initialStatus = request.paymentMode() == PaymentMode.POSTPAID
                ? BookingStatus.CONFIRMED
                : BookingStatus.PENDING_PAYMENT;

        Booking booking = Booking.builder()
                .user(user)
                .pcStation(pcStation)
                .startTime(startTime)
                .endTime(endTime)
                .durationMinutes(request.durationMinutes())
                .amount(amount)
                .paymentMode(request.paymentMode())
                .status(initialStatus)
                .build();

        booking = bookingRepository.save(booking);

        String qrBase64 = null;
        String upiLink = null;
        if (request.paymentMode() == PaymentMode.PREPAID_QR) {
            upiLink = qrCodeService.buildUpiPaymentLink(amount, "Booking #" + booking.getId() + " " + pcStation.getLabel());
            qrBase64 = qrCodeService.generateQrCodeBase64Png(upiLink);
        }

        return new BookingCreatedResponse(BookingResponse.from(booking), qrBase64, upiLink);
    }

    /**
     * Public read model backing the mobile app's hour-grid: which time ranges on this PC are
     * already reserved for the given day. Interpreted in UTC, consistent with how every other
     * timestamp in this service is handled (no per-cafe timezone concept exists yet).
     */
    public List<BusySlotResponse> listBusySlots(Long pcStationId, LocalDate date) {
        pcStationService.findEntity(pcStationId);

        Instant dayStart = date.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant dayEnd = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        return bookingRepository.findOverlappingBookings(pcStationId, dayStart, dayEnd, ACTIVE_STATUSES).stream()
                .map(b -> new BusySlotResponse(b.getStartTime(), b.getEndTime()))
                .toList();
    }

    public List<BookingResponse> listMyBookings(Long userId) {
        return bookingRepository.findByUserIdOrderByStartTimeDesc(userId).stream()
                .map(BookingResponse::from)
                .toList();
    }

    public BookingResponse getMyBooking(Long userId, Long bookingId) {
        Booking booking = findEntity(bookingId);
        assertOwner(booking, userId);
        return BookingResponse.from(booking);
    }

    @Transactional
    public BookingResponse cancelMyBooking(Long userId, Long bookingId) {
        Booking booking = findEntity(bookingId);
        assertOwner(booking, userId);

        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.COMPLETED) {
            throw new ConflictException("This booking can no longer be cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        return BookingResponse.from(booking);
    }

    Booking findEntity(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No booking found with id " + id));
    }

    private void assertOwner(Booking booking, Long userId) {
        if (!booking.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You do not have permission to access this booking");
        }
    }
}
