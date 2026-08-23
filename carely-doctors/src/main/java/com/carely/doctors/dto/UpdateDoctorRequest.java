package com.carely.doctors.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalTime;

public record UpdateDoctorRequest(
        @Size(max = 100) String specialization,
        @Size(max = 100) String medicalLicenseNumber,
        @Min(0) Integer yearsOfExperience,
        @DecimalMin(value = "0.00") BigDecimal consultationFee,
        @Size(max = 5000) String biography,
        LocalTime workingStartTime,
        LocalTime workingEndTime,
        @Min(15) Integer slotDurationMinutes,
        Boolean active
) {}
