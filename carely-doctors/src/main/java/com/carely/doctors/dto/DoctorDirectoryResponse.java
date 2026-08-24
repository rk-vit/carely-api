package com.carely.doctors.dto;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.UUID;

public record DoctorDirectoryResponse(
        UUID id,
        String firstName,
        String lastName,
        String specialization,
        Integer yearsOfExperience,
        BigDecimal consultationFee,
        String biography,
        LocalTime workingStartTime,
        LocalTime workingEndTime,
        Integer slotDurationMinutes,
        Boolean active
) {}
