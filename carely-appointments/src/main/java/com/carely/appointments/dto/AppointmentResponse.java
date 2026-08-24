package com.carely.appointments.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AppointmentResponse(
        UUID id,
        UUID doctorId,
        UUID patientId,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        String status,
        String symptoms,
        OffsetDateTime holdExpiresAt,
        String patientName,
        String patientEmail
) {}
