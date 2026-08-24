package com.carely.appointments.dto;

import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public record RescheduleAppointmentRequest(
        @NotNull OffsetDateTime startAt,
        @NotNull OffsetDateTime endAt
) {}
