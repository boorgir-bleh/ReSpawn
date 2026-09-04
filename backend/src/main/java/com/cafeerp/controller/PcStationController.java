package com.cafeerp.controller;

import com.cafeerp.dto.pc.BusySlotResponse;
import com.cafeerp.dto.pc.PcStationRequest;
import com.cafeerp.dto.pc.PcStationResponse;
import com.cafeerp.dto.pc.PcStatusUpdateRequest;
import com.cafeerp.service.BookingService;
import com.cafeerp.service.PcStationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/pc-stations")
@RequiredArgsConstructor
@Tag(name = "PC Stations", description = "Browse PC stations (public) and manage them (admin)")
public class PcStationController {

    private final PcStationService pcStationService;
    private final BookingService bookingService;

    @GetMapping
    @SecurityRequirements
    public ResponseEntity<List<PcStationResponse>> listAll() {
        return ResponseEntity.ok(pcStationService.listAll());
    }

    @GetMapping("/{id}")
    @SecurityRequirements
    public ResponseEntity<PcStationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(pcStationService.getById(id));
    }

    @GetMapping("/{id}/busy-slots")
    @SecurityRequirements
    public ResponseEntity<List<BusySlotResponse>> getBusySlots(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(bookingService.listBusySlots(id, date != null ? date : LocalDate.now()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PcStationResponse> create(@Valid @RequestBody PcStationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pcStationService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PcStationResponse> update(@PathVariable Long id, @Valid @RequestBody PcStationRequest request) {
        return ResponseEntity.ok(pcStationService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PcStationResponse> updateStatus(@PathVariable Long id, @Valid @RequestBody PcStatusUpdateRequest request) {
        return ResponseEntity.ok(pcStationService.updateStatus(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        pcStationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
