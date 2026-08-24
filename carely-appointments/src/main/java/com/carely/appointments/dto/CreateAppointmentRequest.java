package com.carely.appointments.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CreateAppointmentRequest(
        @NotNull UUID doctorId,
        @NotNull OffsetDateTime startAt,
        @NotNull OffsetDateTime endAt,
        @NotBlank String symptoms
) {}
