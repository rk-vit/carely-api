package com.carely.doctors.service;

import com.carely.doctors.dto.AvailabilityOverrideRequest;
import com.carely.doctors.dto.AvailabilityOverrideResponse;
import com.carely.doctors.dto.AvailabilityRequest;
import com.carely.doctors.dto.AvailabilityResponse;
import com.carely.doctors.dto.SlotResponse;
import com.carely.doctors.repository.AvailabilityRepository;
import com.carely.doctors.repository.DoctorRepository;
import com.carely.jooq.generated.tables.records.DoctorAvailabilityOverrideRecord;
import com.carely.jooq.generated.tables.records.DoctorAvailabilityRecord;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class AvailabilityService {
    private static final int SLOT_MINUTES = 30;

    private final AvailabilityRepository availabilityRepository;
    private final DoctorRepository doctorRepository;

    public AvailabilityService(AvailabilityRepository availabilityRepository,
                               DoctorRepository doctorRepository) {
        this.availabilityRepository = availabilityRepository;
        this.doctorRepository = doctorRepository;
    }

    @Transactional
    public AvailabilityResponse saveAvailability(UUID doctorId, DayOfWeek day, AvailabilityRequest request) {
        requireDoctor(doctorId);
        validateRange(request.startTime(), request.endTime());
        String timezone = request.timezone() == null || request.timezone().isBlank()
                ? "UTC" : request.timezone().trim();
        validateTimezone(timezone);
        for (DoctorAvailabilityOverrideRecord override : availabilityRepository.findAllOverrides(doctorId)) {
            if (override.getOverrideDate().getDayOfWeek() != day) {
                continue;
            }
            if (override.getType().equals("BLOCKED")
                    && (override.getStartTime().isBefore(request.startTime())
                    || override.getEndTime().isAfter(request.endTime()))) {
                throw conflict("The new normal hours would invalidate an existing blocked override.");
            }
            if (override.getType().equals("EXTRA")
                    && overlaps(request.startTime(), request.endTime(),
                    override.getStartTime(), override.getEndTime())) {
                throw conflict("The new normal hours overlap an existing extra override.");
            }
        }
        return toAvailability(availabilityRepository.upsertAvailability(
                doctorId, day, request.startTime(), request.endTime(), timezone));
    }

    @Transactional(readOnly = true)
    public List<AvailabilityResponse> listAvailability(UUID doctorId) {
        requireDoctor(doctorId);
        return availabilityRepository.findAllAvailability(doctorId).stream()
                .map(this::toAvailability).toList();
    }

    @Transactional(readOnly = true)
    public List<AvailabilityOverrideResponse> listOverrides(UUID doctorId, LocalDate date) {
        requireDoctor(doctorId);
        return (date == null ? availabilityRepository.findAllOverrides(doctorId)
                : availabilityRepository.findOverrides(doctorId, date)).stream()
                .map(this::toOverride).toList();
    }

    @Transactional
    public void deleteAvailability(UUID doctorId, DayOfWeek day) {
        requireDoctor(doctorId);
        availabilityRepository.deleteAvailability(doctorId, day);
    }

    @Transactional
    public AvailabilityOverrideResponse addOverride(UUID doctorId, AvailabilityOverrideRequest request) {
        requireDoctor(doctorId);
        validateRange(request.startTime(), request.endTime());
        List<DoctorAvailabilityOverrideRecord> existing =
                availabilityRepository.findOverrides(doctorId, request.date());
        if (existing.stream().anyMatch(override -> overlaps(request.startTime(), request.endTime(),
                override.getStartTime(), override.getEndTime()))) {
            throw conflict("This override overlaps an existing override.");
        }

        DoctorAvailabilityRecord normal = availabilityRepository
                .findAvailability(doctorId, request.date().getDayOfWeek()).orElse(null);
        if (request.type() == AvailabilityOverrideRequest.OverrideType.BLOCKED) {
            if (normal == null || request.startTime().isBefore(normal.getStartTime())
                    || request.endTime().isAfter(normal.getEndTime())) {
                throw badRequest("A blocked interval must be inside the doctor's normal availability.");
            }
        } else if (normal != null && overlaps(request.startTime(), request.endTime(),
                normal.getStartTime(), normal.getEndTime())) {
            throw conflict("Extra availability cannot overlap normal working hours.");
        }

        try {
            return toOverride(availabilityRepository.insertOverride(doctorId, request.date(),
                    request.startTime(), request.endTime(), request.type().name(), request.reason()));
        } catch (DataIntegrityViolationException e) {
            throw conflict("This override overlaps an existing override.");
        }
    }

    @Transactional
    public void deleteOverride(UUID doctorId, UUID overrideId) {
        requireDoctor(doctorId);
        if (availabilityRepository.deleteOverride(doctorId, overrideId) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Availability override not found");
        }
    }

    @Transactional(readOnly = true)
    public List<SlotResponse> slots(UUID doctorId, LocalDate date) {
        requireDoctor(doctorId);
        DoctorAvailabilityRecord normal = availabilityRepository
                .findAvailability(doctorId, date.getDayOfWeek()).orElse(null);
        List<DoctorAvailabilityOverrideRecord> overrides = availabilityRepository.findOverrides(doctorId, date);
        List<TimeRange> available = new ArrayList<>();
        if (normal != null) {
            available.add(new TimeRange(normal.getStartTime(), normal.getEndTime()));
        }
        overrides.stream().filter(o -> "EXTRA".equals(o.getType()))
                .map(o -> new TimeRange(o.getStartTime(), o.getEndTime())).forEach(available::add);
        List<TimeRange> blocked = overrides.stream().filter(o -> "BLOCKED".equals(o.getType()))
                .map(o -> new TimeRange(o.getStartTime(), o.getEndTime())).toList();

        ZoneId zone = normal == null ? ZoneOffset.UTC : ZoneId.of(normal.getTimezone());
        List<SlotResponse> result = new ArrayList<>();
        for (TimeRange range : available) {
            for (LocalTime start = range.start(); !start.plusMinutes(SLOT_MINUTES).isAfter(range.end());
                 start = start.plusMinutes(SLOT_MINUTES)) {
                LocalTime end = start.plusMinutes(SLOT_MINUTES);
                LocalTime slotStart = start;
                boolean isBlocked = blocked.stream().anyMatch(b -> overlaps(slotStart, end, b.start(), b.end()));
                OffsetDateTime startAt = LocalDateTime.of(date, start).atZone(zone).toOffsetDateTime();
                OffsetDateTime endAt = LocalDateTime.of(date, end).atZone(zone).toOffsetDateTime();
                result.add(new SlotResponse(startAt, endAt, isBlocked ? "BLOCKED" : "AVAILABLE"));
            }
        }
        result.sort(Comparator.comparing(SlotResponse::startAt));
        return result;
    }

    private void requireDoctor(UUID id) {
        if (doctorRepository.findById(id).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found");
        }
    }

    private void validateRange(LocalTime start, LocalTime end) {
        if (start == null || end == null || !start.isBefore(end)) {
            throw badRequest("Start time must be before end time.");
        }
        if (start.getSecond() != 0 || end.getSecond() != 0
                || start.getMinute() % SLOT_MINUTES != 0 || end.getMinute() % SLOT_MINUTES != 0) {
            throw badRequest("Times must be aligned to 30-minute boundaries.");
        }
    }

    private void validateTimezone(String timezone) {
        try { ZoneId.of(timezone); }
        catch (Exception e) { throw badRequest("Invalid timezone."); }
    }

    private boolean overlaps(LocalTime aStart, LocalTime aEnd, LocalTime bStart, LocalTime bEnd) {
        return aStart.isBefore(bEnd) && bStart.isBefore(aEnd);
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }

    private AvailabilityResponse toAvailability(DoctorAvailabilityRecord r) {
        return new AvailabilityResponse(r.getId(), r.getDoctorId(), DayOfWeek.valueOf(r.getDayOfWeek()),
                r.getStartTime(), r.getEndTime(), r.getTimezone());
    }

    private AvailabilityOverrideResponse toOverride(DoctorAvailabilityOverrideRecord r) {
        return new AvailabilityOverrideResponse(r.getId(), r.getDoctorId(), r.getOverrideDate(),
                r.getStartTime(), r.getEndTime(), r.getType(), r.getReason());
    }

    private record TimeRange(LocalTime start, LocalTime end) {}
}
