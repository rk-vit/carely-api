package com.carely.appointments.repository;

import com.carely.jooq.generated.tables.records.AppointmentsRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.carely.jooq.generated.tables.Appointments.APPOINTMENTS;

@Repository
public class AppointmentRepository {
    private final DSLContext dsl;

    public AppointmentRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public AppointmentsRecord insertHold(UUID id,
                                         UUID doctorId,
                                         UUID patientId,
                                         OffsetDateTime startAt,
                                         OffsetDateTime endAt,
                                         String symptoms,
                                         OffsetDateTime holdExpiresAt) {
        deleteExpiredHolds();

        return dsl.insertInto(APPOINTMENTS)
                .set(APPOINTMENTS.ID, id)
                .set(APPOINTMENTS.DOCTOR_ID, doctorId)
                .set(APPOINTMENTS.PATIENT_ID, patientId)
                .set(APPOINTMENTS.START_AT, startAt)
                .set(APPOINTMENTS.END_AT, endAt)
                .set(APPOINTMENTS.SYMPTOMS, symptoms)
                .set(APPOINTMENTS.STATUS, "HELD")
                .set(APPOINTMENTS.HOLD_EXPIRES_AT, holdExpiresAt)
                .returning()
                .fetchOne();
    }

    public Optional<AppointmentsRecord> findById(UUID id) {
        return dsl.selectFrom(APPOINTMENTS)
                .where(APPOINTMENTS.ID.eq(id))
                .fetchOptional();
    }

    public AppointmentsRecord confirmHold(UUID id) {
        return dsl.update(APPOINTMENTS)
                .set(APPOINTMENTS.STATUS, "BOOKED")
                .set(APPOINTMENTS.HOLD_EXPIRES_AT, (OffsetDateTime) null)
                .set(APPOINTMENTS.UPDATED_AT, OffsetDateTime.now())
                .where(APPOINTMENTS.ID.eq(id))
                .and(APPOINTMENTS.STATUS.eq("HELD"))
                .and(APPOINTMENTS.HOLD_EXPIRES_AT.gt(OffsetDateTime.now()))
                .returning()
                .fetchOne();
    }

    public List<AppointmentsRecord> findByPatientId(UUID patientId) {
        return dsl.selectFrom(APPOINTMENTS)
                .where(APPOINTMENTS.PATIENT_ID.eq(patientId))
                .orderBy(APPOINTMENTS.START_AT.desc())
                .fetch();
    }

    public List<AppointmentsRecord> findByDoctorId(UUID doctorId) {
        return dsl.selectFrom(APPOINTMENTS)
                .where(APPOINTMENTS.DOCTOR_ID.eq(doctorId))
                .orderBy(APPOINTMENTS.START_AT.asc())
                .fetch();
    }

    private void deleteExpiredHolds() {
        dsl.deleteFrom(APPOINTMENTS)
                .where(APPOINTMENTS.STATUS.eq("HELD"))
                .and(APPOINTMENTS.HOLD_EXPIRES_AT.le(OffsetDateTime.now()))
                .execute();
    }
}
