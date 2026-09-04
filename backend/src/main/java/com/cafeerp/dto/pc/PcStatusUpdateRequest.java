package com.cafeerp.dto.pc;

import com.cafeerp.entity.PcStatus;
import jakarta.validation.constraints.NotNull;

public record PcStatusUpdateRequest(

        @NotNull(message = "Status is required")
        PcStatus status
) {
}
