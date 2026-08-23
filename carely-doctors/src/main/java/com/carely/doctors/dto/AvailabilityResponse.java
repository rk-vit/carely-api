package com.carely.doctors.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

public record AvailabilityResponse(UUID id, UUID doctorId, DayOfWeek dayOfWeek,
                                   LocalTime startTime, LocalTime endTime, String timezone) {}
