package com.carely.doctors.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;

public record AvailabilityOverrideRequest(
        @NotNull LocalDate date,
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime,
        @NotNull OverrideType type,
        @Size(max = 500) String reason
) {
    public enum OverrideType { BLOCKED, EXTRA }
}
