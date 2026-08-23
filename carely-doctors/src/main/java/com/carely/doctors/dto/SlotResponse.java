package com.carely.doctors.dto;

import java.time.OffsetDateTime;

public record SlotResponse(OffsetDateTime startAt, OffsetDateTime endAt, String status) {}
