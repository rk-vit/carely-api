package com.carely.doctors.dto;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.UUID;

public record DoctorResponse(
        UUID id,
        UUID userId,
        String email,
        String firstName,
        String lastName,
        String phoneNumber,
        String specialization,
        String medicalLicenseNumber,
        Integer yearsOfExperience,
        BigDecimal consultationFee,
        String biography,
        LocalTime workingStartTime,
        LocalTime workingEndTime,
        Integer slotDurationMinutes,
        Boolean active
) {}
