package com.carely.appointments.repository;

import com.carely.jooq.generated.tables.records.AppointmentsRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jooq.Field;
import org.jooq.Table;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.table;

import static com.carely.jooq.generated.tables.Appointments.APPOINTMENTS;

@Repository
public class AppointmentRepository {
    private static final Table<?> VISIT_CONSULTATIONS = table(name("visit_consultations"));
    private static final Field<UUID> CONSULTATION_APPOINTMENT_ID = field(name("visit_consultations", "appointment_id"), UUID.class);
    private static final Field<String> CONSULTATION_NOTES = field(name("visit_consultations", "clinical_notes"), String.class);
    private static final Field<String> CONSULTATION_DIAGNOSIS = field(name("visit_consultations", "diagnosis"), String.class);
    private static final Field<String> CONSULTATION_PRESCRIPTION = field(name("visit_consultations", "prescription"), String.class);
    private static final Field<String> CONSULTATION_SUMMARY = field(name("visit_consultations", "summary"), String.class);
    private static final Field<LocalDate> CONSULTATION_FOLLOW_UP = field(name("visit_consultations", "follow_up_date"), LocalDate.class);
    private static final Field<OffsetDateTime> CONSULTATION_UPDATED_AT = field(name("visit_consultations", "updated_at"), OffsetDateTime.class);
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

    public List<AppointmentsRecord> findAll() {
        return dsl.selectFrom(APPOINTMENTS)
                .orderBy(APPOINTMENTS.START_AT.asc())
                .fetch();
    }

    public AppointmentsRecord cancel(UUID id) {
        return dsl.update(APPOINTMENTS)
                .set(APPOINTMENTS.STATUS, "CANCELLED")
                .set(APPOINTMENTS.HOLD_EXPIRES_AT, (OffsetDateTime) null)
                .set(APPOINTMENTS.UPDATED_AT, OffsetDateTime.now())
                .where(APPOINTMENTS.ID.eq(id))
                .and(APPOINTMENTS.STATUS.in("HELD", "BOOKED"))
                .returning()
                .fetchOne();
    }

    public AppointmentsRecord reschedule(UUID id, OffsetDateTime startAt, OffsetDateTime endAt) {
        return dsl.update(APPOINTMENTS)
                .set(APPOINTMENTS.START_AT, startAt)
                .set(APPOINTMENTS.END_AT, endAt)
                .set(APPOINTMENTS.UPDATED_AT, OffsetDateTime.now())
                .where(APPOINTMENTS.ID.eq(id))
                .and(APPOINTMENTS.STATUS.eq("BOOKED"))
                .returning()
                .fetchOne();
    }

    public AppointmentsRecord updateStatus(UUID id, String status) {
        return dsl.update(APPOINTMENTS)
                .set(APPOINTMENTS.STATUS, status)
                .set(APPOINTMENTS.HOLD_EXPIRES_AT, (OffsetDateTime) null)
                .set(APPOINTMENTS.UPDATED_AT, OffsetDateTime.now())
                .where(APPOINTMENTS.ID.eq(id))
                .returning()
                .fetchOne();
    }

    public Optional<ConsultationRow> findConsultation(UUID appointmentId) {
        return dsl.select(CONSULTATION_NOTES, CONSULTATION_DIAGNOSIS, CONSULTATION_PRESCRIPTION,
                        CONSULTATION_SUMMARY, CONSULTATION_FOLLOW_UP, CONSULTATION_UPDATED_AT)
                .from(VISIT_CONSULTATIONS).where(CONSULTATION_APPOINTMENT_ID.eq(appointmentId))
                .fetchOptional().map(row -> new ConsultationRow(row.value1(), row.value2(), row.value3(),
                        row.value4(), row.value5(), row.value6()));
    }

    public void upsertConsultation(UUID appointmentId, String notes, String diagnosis, String prescription,
                                   String summary, LocalDate followUpDate) {
        int updated = dsl.update(VISIT_CONSULTATIONS)
                .set(CONSULTATION_NOTES, notes).set(CONSULTATION_DIAGNOSIS, diagnosis)
                .set(CONSULTATION_PRESCRIPTION, prescription).set(CONSULTATION_SUMMARY, summary)
                .set(CONSULTATION_FOLLOW_UP, followUpDate).set(CONSULTATION_UPDATED_AT, OffsetDateTime.now())
                .where(CONSULTATION_APPOINTMENT_ID.eq(appointmentId)).execute();
        if (updated == 0) {
            dsl.insertInto(VISIT_CONSULTATIONS).set(CONSULTATION_APPOINTMENT_ID, appointmentId)
                    .set(CONSULTATION_NOTES, notes).set(CONSULTATION_DIAGNOSIS, diagnosis)
                    .set(CONSULTATION_PRESCRIPTION, prescription).set(CONSULTATION_SUMMARY, summary)
                    .set(CONSULTATION_FOLLOW_UP, followUpDate).execute();
        }
    }

    public record ConsultationRow(String clinicalNotes, String diagnosis, String prescription,
                                  String summary, LocalDate followUpDate, OffsetDateTime updatedAt) {}

    private void deleteExpiredHolds() {
        dsl.deleteFrom(APPOINTMENTS)
                .where(APPOINTMENTS.STATUS.eq("HELD"))
                .and(APPOINTMENTS.HOLD_EXPIRES_AT.le(OffsetDateTime.now()))
                .execute();
    }
}
