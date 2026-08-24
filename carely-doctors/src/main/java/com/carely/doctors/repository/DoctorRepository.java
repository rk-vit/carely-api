package com.carely.doctors.repository;

import com.carely.jooq.generated.tables.records.DoctorsRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

import static com.carely.jooq.generated.tables.Doctors.DOCTORS;
import static com.carely.jooq.generated.tables.Users.USERS;

@Repository
public class DoctorRepository {
    private final DSLContext dsl;

    public DoctorRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public DoctorsRecord insertDoctor(UUID id, UUID userId, String specialization,
                                      String license, Integer experience, BigDecimal fee,
                                      String biography, LocalTime start, LocalTime end,
                                      Integer slotDuration) {
        return dsl.insertInto(DOCTORS)
                .set(DOCTORS.ID, id)
                .set(DOCTORS.USER_ID, userId)
                .set(DOCTORS.SPECIALIZATION, specialization)
                .set(DOCTORS.MEDICAL_LICENSE_NUMBER, license)
                .set(DOCTORS.YEARS_OF_EXPERIENCE, experience)
                .set(DOCTORS.CONSULTATION_FEE, fee)
                .set(DOCTORS.BIOGRAPHY, biography)
                .set(DOCTORS.WORKING_START_TIME, start)
                .set(DOCTORS.WORKING_END_TIME, end)
                .set(DOCTORS.SLOT_DURATION_MINUTES, slotDuration)
                .returning()
                .fetchOne();
    }

    public Optional<DoctorsRecord> findById(UUID id) {
        return dsl.selectFrom(DOCTORS).where(DOCTORS.ID.eq(id)).fetchOptional();
    }

    public DoctorsRecord updateDoctor(UUID id, String specialization, String license,
                                      Integer experience, BigDecimal fee, String biography,
                                      LocalTime start, LocalTime end, Integer slotDuration,
                                      Boolean active) {
        return dsl.update(DOCTORS)
                .set(DOCTORS.SPECIALIZATION, specialization)
                .set(DOCTORS.MEDICAL_LICENSE_NUMBER, license)
                .set(DOCTORS.YEARS_OF_EXPERIENCE, experience)
                .set(DOCTORS.CONSULTATION_FEE, fee)
                .set(DOCTORS.BIOGRAPHY, biography)
                .set(DOCTORS.WORKING_START_TIME, start)
                .set(DOCTORS.WORKING_END_TIME, end)
                .set(DOCTORS.SLOT_DURATION_MINUTES, slotDuration)
                .set(DOCTORS.ACTIVE, active)
                .set(DOCTORS.UPDATED_AT, java.time.OffsetDateTime.now())
                .where(DOCTORS.ID.eq(id))
                .returning()
                .fetchOne();
    }

    public Optional<DoctorResponseRow> findResponseById(UUID id) {
        return dsl.select(
                        DOCTORS.ID, DOCTORS.USER_ID, USERS.EMAIL, USERS.FIRST_NAME,
                        USERS.LAST_NAME, USERS.PHONE_NUMBER, DOCTORS.SPECIALIZATION,
                        DOCTORS.MEDICAL_LICENSE_NUMBER, DOCTORS.YEARS_OF_EXPERIENCE,
                        DOCTORS.CONSULTATION_FEE, DOCTORS.BIOGRAPHY,
                        DOCTORS.WORKING_START_TIME, DOCTORS.WORKING_END_TIME,
                        DOCTORS.SLOT_DURATION_MINUTES, DOCTORS.ACTIVE
                )
                .from(DOCTORS)
                .join(USERS).on(USERS.ID.eq(DOCTORS.USER_ID))
                .where(DOCTORS.ID.eq(id))
                .fetchOptional()
                .map(row -> new DoctorResponseRow(row.value1(), row.value2(), row.value3(), row.value4(),
                        row.value5(), row.value6(), row.value7(), row.value8(), row.value9(), row.value10(),
                        row.value11(), row.value12(), row.value13(), row.value14(), row.value15()));
    }

    public Optional<DoctorResponseRow> findResponseByEmail(String email) {
        return dsl.select(
                        DOCTORS.ID, DOCTORS.USER_ID, USERS.EMAIL, USERS.FIRST_NAME,
                        USERS.LAST_NAME, USERS.PHONE_NUMBER, DOCTORS.SPECIALIZATION,
                        DOCTORS.MEDICAL_LICENSE_NUMBER, DOCTORS.YEARS_OF_EXPERIENCE,
                        DOCTORS.CONSULTATION_FEE, DOCTORS.BIOGRAPHY,
                        DOCTORS.WORKING_START_TIME, DOCTORS.WORKING_END_TIME,
                        DOCTORS.SLOT_DURATION_MINUTES, DOCTORS.ACTIVE
                )
                .from(DOCTORS)
                .join(USERS).on(USERS.ID.eq(DOCTORS.USER_ID))
                .where(USERS.EMAIL.eq(email))
                .fetchOptional()
                .map(row -> new DoctorResponseRow(row.value1(), row.value2(), row.value3(), row.value4(),
                        row.value5(), row.value6(), row.value7(), row.value8(), row.value9(), row.value10(),
                        row.value11(), row.value12(), row.value13(), row.value14(), row.value15()));
    }

    public List<DoctorResponseRow> findAllResponses() {
        return dsl.select(
                        DOCTORS.ID, DOCTORS.USER_ID, USERS.EMAIL, USERS.FIRST_NAME,
                        USERS.LAST_NAME, USERS.PHONE_NUMBER, DOCTORS.SPECIALIZATION,
                        DOCTORS.MEDICAL_LICENSE_NUMBER, DOCTORS.YEARS_OF_EXPERIENCE,
                        DOCTORS.CONSULTATION_FEE, DOCTORS.BIOGRAPHY,
                        DOCTORS.WORKING_START_TIME, DOCTORS.WORKING_END_TIME,
                        DOCTORS.SLOT_DURATION_MINUTES, DOCTORS.ACTIVE
                )
                .from(DOCTORS)
                .join(USERS).on(USERS.ID.eq(DOCTORS.USER_ID))
                .orderBy(USERS.LAST_NAME.asc(), USERS.FIRST_NAME.asc())
                .fetch()
                .map(row -> new DoctorResponseRow(row.value1(), row.value2(), row.value3(), row.value4(),
                        row.value5(), row.value6(), row.value7(), row.value8(), row.value9(), row.value10(),
                        row.value11(), row.value12(), row.value13(), row.value14(), row.value15()));
    }

    public record DoctorResponseRow(UUID id, UUID userId, String email, String firstName,
                                    String lastName, String phoneNumber, String specialization,
                                    String medicalLicenseNumber, Integer yearsOfExperience,
                                    BigDecimal consultationFee, String biography,
                                    LocalTime workingStartTime, LocalTime workingEndTime,
                                    Integer slotDurationMinutes, Boolean active) {}
}
