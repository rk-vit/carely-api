package com.carely.users.repository;

import com.carely.jooq.generated.tables.records.UsersRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import org.jooq.Field;
import org.jooq.Table;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.table;

import static com.carely.jooq.generated.tables.Users.USERS;

@Repository
public class UserRepository {

    private static final Table<?> PATIENT_PROFILES = table(name("patient_profiles"));
    private static final Field<UUID> PROFILE_USER_ID = field(name("patient_profiles", "user_id"), UUID.class);
    private static final Field<LocalDate> PROFILE_DOB = field(name("patient_profiles", "date_of_birth"), LocalDate.class);
    private static final Field<String> PROFILE_GENDER = field(name("patient_profiles", "gender"), String.class);
    private static final Field<String> PROFILE_ADDRESS = field(name("patient_profiles", "address"), String.class);
    private static final Field<String> PROFILE_EMERGENCY_NAME = field(name("patient_profiles", "emergency_contact_name"), String.class);
    private static final Field<String> PROFILE_EMERGENCY_PHONE = field(name("patient_profiles", "emergency_contact_phone"), String.class);
    private static final Field<String> PROFILE_ALLERGIES = field(name("patient_profiles", "allergies"), String.class);
    private static final Field<OffsetDateTime> PROFILE_UPDATED_AT = field(name("patient_profiles", "updated_at"), OffsetDateTime.class);

    private final DSLContext dsl;

    public UserRepository(DSLContext dsl){
        this.dsl = dsl;
    }

    public boolean existsByEmail(String email){
        return dsl.fetchExists(
                dsl.selectOne()
                        .from(USERS)
                        .where(USERS.EMAIL.eq(email))
        );
    }

    public Optional<UsersRecord> findByEmail(String email){
        return dsl.selectFrom(USERS).where(USERS.EMAIL.eq(email)).fetchOptional();
    }

    public Optional<UsersRecord> findById(UUID id) {
        return dsl.selectFrom(USERS).where(USERS.ID.eq(id)).fetchOptional();
    }

    public Optional<PatientProfileRow> findPatientProfile(UUID patientId) {
        return dsl.select(PROFILE_DOB, PROFILE_GENDER, PROFILE_ADDRESS, PROFILE_EMERGENCY_NAME,
                        PROFILE_EMERGENCY_PHONE, PROFILE_ALLERGIES, PROFILE_UPDATED_AT)
                .from(PATIENT_PROFILES)
                .where(PROFILE_USER_ID.eq(patientId))
                .fetchOptional()
                .map(row -> new PatientProfileRow(row.value1(), row.value2(), row.value3(), row.value4(),
                        row.value5(), row.value6(), row.value7()));
    }

    public void upsertPatientProfile(UUID patientId, LocalDate dateOfBirth, String gender, String address,
                                     String emergencyName, String emergencyPhone, String allergies) {
        int updated = dsl.update(PATIENT_PROFILES)
                .set(PROFILE_DOB, dateOfBirth)
                .set(PROFILE_GENDER, gender)
                .set(PROFILE_ADDRESS, address)
                .set(PROFILE_EMERGENCY_NAME, emergencyName)
                .set(PROFILE_EMERGENCY_PHONE, emergencyPhone)
                .set(PROFILE_ALLERGIES, allergies)
                .set(PROFILE_UPDATED_AT, OffsetDateTime.now())
                .where(PROFILE_USER_ID.eq(patientId))
                .execute();
        if (updated == 0) {
            dsl.insertInto(PATIENT_PROFILES)
                    .set(PROFILE_USER_ID, patientId)
                    .set(PROFILE_DOB, dateOfBirth)
                    .set(PROFILE_GENDER, gender)
                    .set(PROFILE_ADDRESS, address)
                    .set(PROFILE_EMERGENCY_NAME, emergencyName)
                    .set(PROFILE_EMERGENCY_PHONE, emergencyPhone)
                    .set(PROFILE_ALLERGIES, allergies)
                    .execute();
        }
    }

    public record PatientProfileRow(LocalDate dateOfBirth, String gender, String address,
                                    String emergencyContactName, String emergencyContactPhone,
                                    String allergies, OffsetDateTime updatedAt) {}

    public UsersRecord insertUser( UUID id,
                                    String email,
                                    String passwordHash,
                                    String firstName,
                                    String lastName,
                                    String phoneNumber){

        return dsl.insertInto(USERS)
                .set(USERS.ID , id)
                .set(USERS.EMAIL,email)
                .set(USERS.PASSWORD_HASH,passwordHash)
                .set(USERS.FIRST_NAME,firstName)
                .set(USERS.LAST_NAME,lastName)
                .set(USERS.PHONE_NUMBER,phoneNumber)
                .set(USERS.ROLE,"PATIENT")
                .set(USERS.STATUS,"PENDING_VERIFICATION")
                .returning()
                .fetchOne();
    }

    public UsersRecord insertDoctorUser(UUID id,
                                         String email,
                                         String passwordHash,
                                         String firstName,
                                         String lastName,
                                         String phoneNumber) {
        return dsl.insertInto(USERS)
                .set(USERS.ID, id)
                .set(USERS.EMAIL, email)
                .set(USERS.PASSWORD_HASH, passwordHash)
                .set(USERS.FIRST_NAME, firstName)
                .set(USERS.LAST_NAME, lastName)
                .set(USERS.PHONE_NUMBER, phoneNumber)
                .set(USERS.ROLE, "DOCTOR")
                .set(USERS.STATUS, "ACTIVE")
                .set(USERS.EMAIL_VERIFIED, true)
                .returning()
                .fetchOne();
    }


}
