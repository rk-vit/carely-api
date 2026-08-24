package com.carely.users.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PatientProfileResponse(
        UUID patientId,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        LocalDate dateOfBirth,
        String gender,
        String address,
        String emergencyContactName,
        String emergencyContactPhone,
        String allergies,
        boolean completed,
        OffsetDateTime updatedAt
) {}
