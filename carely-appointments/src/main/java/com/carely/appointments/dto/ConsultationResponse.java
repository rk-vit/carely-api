package com.carely.appointments.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ConsultationResponse(
        UUID appointmentId,
        String clinicalNotes,
        String diagnosis,
        String prescription,
        String summary,
        LocalDate followUpDate,
        OffsetDateTime updatedAt
) {}
