package com.cafeerp.dto.pc;

import java.time.Instant;

public record BusySlotResponse(
        Instant startTime,
        Instant endTime
) {
}
