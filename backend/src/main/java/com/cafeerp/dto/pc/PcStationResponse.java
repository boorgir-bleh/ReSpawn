package com.cafeerp.dto.pc;

import com.cafeerp.entity.PcStation;
import com.cafeerp.entity.PcStatus;

import java.math.BigDecimal;

public record PcStationResponse(
        Long id,
        String label,
        String cpu,
        String gpu,
        String ram,
        String monitor,
        String peripherals,
        BigDecimal hourlyRate,
        boolean active,
        PcStatus status
) {
    public static PcStationResponse from(PcStation pc) {
        return new PcStationResponse(
                pc.getId(),
                pc.getLabel(),
                pc.getCpu(),
                pc.getGpu(),
                pc.getRam(),
                pc.getMonitor(),
                pc.getPeripherals(),
                pc.getHourlyRate(),
                pc.isActive(),
                pc.getStatus()
        );
    }
}
