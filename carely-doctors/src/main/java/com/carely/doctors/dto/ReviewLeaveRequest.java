package com.carely.doctors.dto;

import jakarta.validation.constraints.Size;

public record ReviewLeaveRequest(@Size(max = 1000) String reviewerNote) {}
