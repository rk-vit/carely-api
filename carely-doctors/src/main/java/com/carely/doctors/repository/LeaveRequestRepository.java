package com.carely.doctors.repository;

import com.carely.jooq.generated.tables.records.DoctorLeaveRequestsRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.carely.jooq.generated.tables.DoctorLeaveRequests.DOCTOR_LEAVE_REQUESTS;
import static com.carely.jooq.generated.tables.Doctors.DOCTORS;
import static com.carely.jooq.generated.tables.Users.USERS;

@Repository
public class LeaveRequestRepository {
    private final DSLContext dsl;

    public LeaveRequestRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public DoctorLeaveRequestsRecord insert(UUID id, UUID doctorId, LocalDate start,
                                            LocalDate end, String reason) {
        return dsl.insertInto(DOCTOR_LEAVE_REQUESTS)
                .set(DOCTOR_LEAVE_REQUESTS.ID, id)
                .set(DOCTOR_LEAVE_REQUESTS.DOCTOR_ID, doctorId)
                .set(DOCTOR_LEAVE_REQUESTS.START_DATE, start)
                .set(DOCTOR_LEAVE_REQUESTS.END_DATE, end)
                .set(DOCTOR_LEAVE_REQUESTS.REASON, reason)
                .set(DOCTOR_LEAVE_REQUESTS.STATUS, "PENDING")
                .returning()
                .fetchOne();
    }

    public boolean existsOverlapping(UUID doctorId, LocalDate start, LocalDate end, UUID excludingId) {
        var condition = DOCTOR_LEAVE_REQUESTS.DOCTOR_ID.eq(doctorId)
                .and(DOCTOR_LEAVE_REQUESTS.STATUS.in("PENDING", "APPROVED"))
                .and(DOCTOR_LEAVE_REQUESTS.START_DATE.le(end))
                .and(DOCTOR_LEAVE_REQUESTS.END_DATE.ge(start));
        if (excludingId != null) condition = condition.and(DOCTOR_LEAVE_REQUESTS.ID.ne(excludingId));
        return dsl.fetchExists(dsl.selectOne().from(DOCTOR_LEAVE_REQUESTS).where(condition));
    }

    public Optional<LeaveRow> find(UUID id) {
        return baseQuery().where(DOCTOR_LEAVE_REQUESTS.ID.eq(id)).fetchOptional()
                .map(this::toRow);
    }

    public List<LeaveRow> findForDoctor(UUID doctorId) {
        return baseQuery().where(DOCTOR_LEAVE_REQUESTS.DOCTOR_ID.eq(doctorId))
                .orderBy(DOCTOR_LEAVE_REQUESTS.CREATED_AT.desc()).fetch().map(this::toRow);
    }

    public List<LeaveRow> findAll(String status) {
        var query = baseQuery();
        if (status != null) {
            return query.where(DOCTOR_LEAVE_REQUESTS.STATUS.eq(status))
                    .orderBy(DOCTOR_LEAVE_REQUESTS.CREATED_AT.desc()).fetch().map(this::toRow);
        }
        return query.orderBy(DOCTOR_LEAVE_REQUESTS.CREATED_AT.desc()).fetch().map(this::toRow);
    }

    public DoctorLeaveRequestsRecord review(UUID id, String status, UUID reviewerId, String note) {
        return dsl.update(DOCTOR_LEAVE_REQUESTS)
                .set(DOCTOR_LEAVE_REQUESTS.STATUS, status)
                .set(DOCTOR_LEAVE_REQUESTS.REVIEWED_BY, reviewerId)
                .set(DOCTOR_LEAVE_REQUESTS.REVIEWED_AT, OffsetDateTime.now())
                .set(DOCTOR_LEAVE_REQUESTS.REVIEWER_NOTE, note)
                .set(DOCTOR_LEAVE_REQUESTS.UPDATED_AT, OffsetDateTime.now())
                .where(DOCTOR_LEAVE_REQUESTS.ID.eq(id))
                .returning().fetchOne();
    }

    private org.jooq.SelectJoinStep<org.jooq.Record13<UUID, UUID, String, String, String, LocalDate,
            LocalDate, String, String, UUID, OffsetDateTime, String, OffsetDateTime>> baseQuery() {
        return dsl.select(
                        DOCTOR_LEAVE_REQUESTS.ID, DOCTOR_LEAVE_REQUESTS.DOCTOR_ID,
                        USERS.FIRST_NAME, USERS.LAST_NAME, USERS.EMAIL,
                        DOCTOR_LEAVE_REQUESTS.START_DATE, DOCTOR_LEAVE_REQUESTS.END_DATE,
                        DOCTOR_LEAVE_REQUESTS.REASON, DOCTOR_LEAVE_REQUESTS.STATUS,
                        DOCTOR_LEAVE_REQUESTS.REVIEWED_BY, DOCTOR_LEAVE_REQUESTS.REVIEWED_AT,
                        DOCTOR_LEAVE_REQUESTS.REVIEWER_NOTE, DOCTOR_LEAVE_REQUESTS.CREATED_AT)
                .from(DOCTOR_LEAVE_REQUESTS)
                .join(DOCTORS).on(DOCTORS.ID.eq(DOCTOR_LEAVE_REQUESTS.DOCTOR_ID))
                .join(USERS).on(USERS.ID.eq(DOCTORS.USER_ID));
    }

    private LeaveRow toRow(org.jooq.Record13<UUID, UUID, String, String, String, LocalDate,
            LocalDate, String, String, UUID, OffsetDateTime, String, OffsetDateTime> row) {
        return new LeaveRow(row.value1(), row.value2(), row.value3(), row.value4(), row.value5(),
                row.value6(), row.value7(), row.value8(), row.value9(), row.value10(), row.value11(),
                row.value12(), row.value13());
    }

    public record LeaveRow(UUID id, UUID doctorId, String firstName, String lastName, String email,
                           LocalDate startDate, LocalDate endDate, String reason, String status,
                           UUID reviewedBy, OffsetDateTime reviewedAt, String reviewerNote,
                           OffsetDateTime createdAt) {}
}
