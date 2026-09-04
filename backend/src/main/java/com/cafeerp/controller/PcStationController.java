package com.cafeerp.controller;

import com.cafeerp.dto.pc.PcStationRequest;
import com.cafeerp.dto.pc.PcStationResponse;
import com.cafeerp.dto.pc.PcStatusUpdateRequest;
import com.cafeerp.service.PcStationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pc-stations")
@RequiredArgsConstructor
@Tag(name = "PC Stations", description = "Browse PC stations (public) and manage them (admin)")
public class PcStationController {

    private final PcStationService pcStationService;

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
