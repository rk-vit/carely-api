package com.carely.doctors.repository;

import com.carely.jooq.generated.tables.records.DoctorAvailabilityOverrideRecord;
import com.carely.jooq.generated.tables.records.DoctorAvailabilityRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.carely.jooq.generated.tables.DoctorAvailability.DOCTOR_AVAILABILITY;
import static com.carely.jooq.generated.tables.DoctorAvailabilityOverride.DOCTOR_AVAILABILITY_OVERRIDE;

@Repository
public class AvailabilityRepository {
    private final DSLContext dsl;

    public AvailabilityRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public Optional<DoctorAvailabilityRecord> findAvailability(UUID doctorId, DayOfWeek day) {
        return dsl.selectFrom(DOCTOR_AVAILABILITY)
                .where(DOCTOR_AVAILABILITY.DOCTOR_ID.eq(doctorId))
                .and(DOCTOR_AVAILABILITY.DAY_OF_WEEK.eq(day.name()))
                .fetchOptional();
    }

    public List<DoctorAvailabilityRecord> findAllAvailability(UUID doctorId) {
        return dsl.selectFrom(DOCTOR_AVAILABILITY)
                .where(DOCTOR_AVAILABILITY.DOCTOR_ID.eq(doctorId))
                .orderBy(DOCTOR_AVAILABILITY.DAY_OF_WEEK)
                .fetch();
    }

    public DoctorAvailabilityRecord upsertAvailability(UUID doctorId, DayOfWeek day,
                                                        java.time.LocalTime start,
                                                        java.time.LocalTime end,
                                                        String timezone) {
        return dsl.insertInto(DOCTOR_AVAILABILITY)
                .set(DOCTOR_AVAILABILITY.ID, UUID.randomUUID())
                .set(DOCTOR_AVAILABILITY.DOCTOR_ID, doctorId)
                .set(DOCTOR_AVAILABILITY.DAY_OF_WEEK, day.name())
                .set(DOCTOR_AVAILABILITY.START_TIME, start)
                .set(DOCTOR_AVAILABILITY.END_TIME, end)
                .set(DOCTOR_AVAILABILITY.TIMEZONE, timezone)
                .onConflict(DOCTOR_AVAILABILITY.DOCTOR_ID, DOCTOR_AVAILABILITY.DAY_OF_WEEK)
                .doUpdate()
                .set(DOCTOR_AVAILABILITY.START_TIME, start)
                .set(DOCTOR_AVAILABILITY.END_TIME, end)
                .set(DOCTOR_AVAILABILITY.TIMEZONE, timezone)
                .set(DOCTOR_AVAILABILITY.UPDATED_AT, java.time.OffsetDateTime.now())
                .returning()
                .fetchOne();
    }

    public int deleteAvailability(UUID doctorId, DayOfWeek day) {
        return dsl.deleteFrom(DOCTOR_AVAILABILITY)
                .where(DOCTOR_AVAILABILITY.DOCTOR_ID.eq(doctorId))
                .and(DOCTOR_AVAILABILITY.DAY_OF_WEEK.eq(day.name()))
                .execute();
    }

    public List<DoctorAvailabilityOverrideRecord> findOverrides(UUID doctorId, LocalDate date) {
        return dsl.selectFrom(DOCTOR_AVAILABILITY_OVERRIDE)
                .where(DOCTOR_AVAILABILITY_OVERRIDE.DOCTOR_ID.eq(doctorId))
                .and(DOCTOR_AVAILABILITY_OVERRIDE.OVERRIDE_DATE.eq(date))
                .orderBy(DOCTOR_AVAILABILITY_OVERRIDE.START_TIME)
                .fetch();
    }

    public List<DoctorAvailabilityOverrideRecord> findAllOverrides(UUID doctorId) {
        return dsl.selectFrom(DOCTOR_AVAILABILITY_OVERRIDE)
                .where(DOCTOR_AVAILABILITY_OVERRIDE.DOCTOR_ID.eq(doctorId))
                .fetch();
    }

    public DoctorAvailabilityOverrideRecord insertOverride(UUID doctorId, LocalDate date,
                                                             java.time.LocalTime start,
                                                             java.time.LocalTime end,
                                                             String type, String reason) {
        return dsl.insertInto(DOCTOR_AVAILABILITY_OVERRIDE)
                .set(DOCTOR_AVAILABILITY_OVERRIDE.ID, UUID.randomUUID())
                .set(DOCTOR_AVAILABILITY_OVERRIDE.DOCTOR_ID, doctorId)
                .set(DOCTOR_AVAILABILITY_OVERRIDE.OVERRIDE_DATE, date)
                .set(DOCTOR_AVAILABILITY_OVERRIDE.START_TIME, start)
                .set(DOCTOR_AVAILABILITY_OVERRIDE.END_TIME, end)
                .set(DOCTOR_AVAILABILITY_OVERRIDE.TYPE, type)
                .set(DOCTOR_AVAILABILITY_OVERRIDE.REASON, reason)
                .returning()
                .fetchOne();
    }

    public int deleteOverride(UUID doctorId, UUID overrideId) {
        return dsl.deleteFrom(DOCTOR_AVAILABILITY_OVERRIDE)
                .where(DOCTOR_AVAILABILITY_OVERRIDE.ID.eq(overrideId))
                .and(DOCTOR_AVAILABILITY_OVERRIDE.DOCTOR_ID.eq(doctorId))
                .execute();
    }
}
