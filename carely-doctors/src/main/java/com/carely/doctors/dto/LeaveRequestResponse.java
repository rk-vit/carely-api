package com.carely.doctors.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record LeaveRequestResponse(
        UUID id,
        UUID doctorId,
        String doctorName,
        String doctorEmail,
        LocalDate startDate,
        LocalDate endDate,
        String reason,
        String status,
        UUID reviewedBy,
        OffsetDateTime reviewedAt,
        String reviewerNote,
        OffsetDateTime createdAt
) {}
