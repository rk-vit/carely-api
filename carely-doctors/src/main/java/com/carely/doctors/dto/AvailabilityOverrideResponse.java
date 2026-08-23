package com.carely.doctors.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record AvailabilityOverrideResponse(UUID id, UUID doctorId, LocalDate date,
                                           LocalTime startTime, LocalTime endTime,
                                           String type, String reason) {}
