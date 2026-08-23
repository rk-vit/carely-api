package com.carely.doctors.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalTime;

public record CreateDoctorRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 100) String temporaryPassword,
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @Size(max = 20) String phoneNumber,
        @NotBlank @Size(max = 100) String specialization,
        @NotBlank @Size(max = 100) String medicalLicenseNumber,
        @Min(0) Integer yearsOfExperience,
        @DecimalMin(value = "0.00") BigDecimal consultationFee,
        @Size(max = 5000) String biography,
        LocalTime workingStartTime,
        LocalTime workingEndTime,
        @Min(15) Integer slotDurationMinutes
) {}
