package com.cafeerp.service;

import com.cafeerp.dto.pc.PcStationRequest;
import com.cafeerp.dto.pc.PcStationResponse;
import com.cafeerp.dto.pc.PcStatusUpdateRequest;
import com.cafeerp.entity.PcStation;
import com.cafeerp.exception.ConflictException;
import com.cafeerp.exception.ResourceNotFoundException;
import com.cafeerp.repository.BookingRepository;
import com.cafeerp.repository.PcStationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PcStationService {

    private final PcStationRepository pcStationRepository;
    private final BookingRepository bookingRepository;

    public List<PcStationResponse> listAll() {
        return pcStationRepository.findAll().stream()
                .map(PcStationResponse::from)
                .toList();
    }

    public PcStationResponse getById(Long id) {
        return PcStationResponse.from(findEntity(id));
    }

    PcStation findEntity(Long id) {
        return pcStationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No PC station found with id " + id));
    }

    /**
     * Locks the PC station row for the duration of the caller's transaction. Booking creation uses
     * this instead of {@link #findEntity} so two concurrent booking requests for the same PC can't
     * both pass the overlap check before either has committed (a classic check-then-act race).
     */
    @Transactional
    PcStation findEntityForUpdate(Long id) {
        return pcStationRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("No PC station found with id " + id));
    }

    @Transactional
    public PcStationResponse create(PcStationRequest request) {
        if (pcStationRepository.existsByLabelIgnoreCase(request.label())) {
            throw new ConflictException("A PC station with label '" + request.label() + "' already exists");
        }

        PcStation pcStation = PcStation.builder()
                .label(request.label())
                .cpu(request.cpu())
                .gpu(request.gpu())
                .ram(request.ram())
                .monitor(request.monitor())
                .peripherals(request.peripherals())
                .hourlyRate(request.hourlyRate())
                .active(request.active())
                .build();

        return PcStationResponse.from(pcStationRepository.save(pcStation));
    }

    @Transactional
    public PcStationResponse update(Long id, PcStationRequest request) {
        PcStation pcStation = findEntity(id);

        if (!pcStation.getLabel().equalsIgnoreCase(request.label())
                && pcStationRepository.existsByLabelIgnoreCase(request.label())) {
            throw new ConflictException("A PC station with label '" + request.label() + "' already exists");
        }

        pcStation.setLabel(request.label());
        pcStation.setCpu(request.cpu());
        pcStation.setGpu(request.gpu());
        pcStation.setRam(request.ram());
        pcStation.setMonitor(request.monitor());
        pcStation.setPeripherals(request.peripherals());
        pcStation.setHourlyRate(request.hourlyRate());
        pcStation.setActive(request.active());

        return PcStationResponse.from(pcStation);
    }

    @Transactional
    public PcStationResponse updateStatus(Long id, PcStatusUpdateRequest request) {
        PcStation pcStation = findEntity(id);
        pcStation.setStatus(request.status());
        return PcStationResponse.from(pcStation);
    }

    @Transactional
    public void delete(Long id) {
        PcStation pcStation = findEntity(id);

        if (bookingRepository.existsByPcStationId(id)) {
            throw new ConflictException(
                    "Cannot delete a PC station that has booking history; set it inactive instead");
        }

        pcStationRepository.delete(pcStation);
    }
}
