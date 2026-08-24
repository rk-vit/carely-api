package com.carely.appointments.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record ConsultationRequest(
        @NotBlank String clinicalNotes,
        String diagnosis,
        String prescription,
        String summary,
        LocalDate followUpDate
) {}
