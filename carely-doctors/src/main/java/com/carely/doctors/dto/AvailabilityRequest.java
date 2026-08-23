package com.carely.doctors.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

public record AvailabilityRequest(
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime,
        String timezone
) {}
