package com.carely.appointments.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateAppointmentStatusRequest(@NotBlank String status) {}
