package com.carely.users.repository;

import com.carely.jooq.generated.tables.records.UsersRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

import static com.carely.jooq.generated.tables.Users.USERS;

@Repository
public class UserRepository {

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
