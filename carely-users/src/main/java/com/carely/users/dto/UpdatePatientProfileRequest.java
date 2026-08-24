package com.carely.users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record UpdatePatientProfileRequest(
        @NotNull LocalDate dateOfBirth,
        @NotBlank String gender,
        @NotBlank String address,
        @NotBlank String emergencyContactName,
        @NotBlank String emergencyContactPhone,
        String allergies
) {}
