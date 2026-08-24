package com.carely.users.service;

import com.carely.jooq.generated.tables.records.UsersRecord;
import com.carely.users.dto.CreateUserRequest;
import com.carely.users.dto.CreateUserResponse;
import com.carely.users.dto.PatientProfileResponse;
import com.carely.users.dto.UpdatePatientProfileRequest;
import com.carely.users.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public CreateUserResponse createUser(CreateUserRequest request) {
        String email = normalizeEmail(request.email());

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Email is already registered");
        }

        String passwordHash =
                passwordEncoder.encode(request.password());

        UsersRecord savedUser = userRepository.insertUser(
                UUID.randomUUID(),
                email,
                passwordHash,
                request.firstName().trim(),
                request.lastName().trim(),
                normalizePhoneNumber(request.phoneNumber())
        );

        return new CreateUserResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getPhoneNumber(),
                savedUser.getRole(),
                savedUser.getStatus()
        );
    }

    @Transactional
    public UsersRecord createDoctorUser(String email,
                                         String rawPassword,
                                         String firstName,
                                         String lastName,
                                         String phoneNumber) {
        String normalizedEmail = normalizeEmail(email);
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }

        return userRepository.insertDoctorUser(
                UUID.randomUUID(),
                normalizedEmail,
                passwordEncoder.encode(rawPassword),
                firstName.trim(),
                lastName.trim(),
                normalizePhoneNumber(phoneNumber)
        );
    }

    @Transactional(readOnly = true)
    public PatientProfileResponse getPatientProfile(String email) {
        UsersRecord user = requirePatient(email);
        return profileResponse(user);
    }

    @Transactional
    public PatientProfileResponse updatePatientProfile(String email, UpdatePatientProfileRequest request) {
        UsersRecord user = requirePatient(email);
        userRepository.upsertPatientProfile(user.getId(), request.dateOfBirth(), request.gender().trim(),
                request.address().trim(), request.emergencyContactName().trim(),
                request.emergencyContactPhone().trim(), request.allergies() == null ? null : request.allergies().trim());
        return profileResponse(user);
    }

    @Transactional(readOnly = true)
    public PatientProfileResponse getPatientProfile(UUID patientId) {
        UsersRecord user = userRepository.findById(patientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));
        return profileResponse(user);
    }

    private PatientProfileResponse profileResponse(UsersRecord user) {
        Optional<UserRepository.PatientProfileRow> profile = userRepository.findPatientProfile(user.getId());
        return profile.map(row -> new PatientProfileResponse(user.getId(), user.getFirstName(), user.getLastName(),
                        user.getEmail(), user.getPhoneNumber(), row.dateOfBirth(), row.gender(), row.address(),
                        row.emergencyContactName(), row.emergencyContactPhone(), row.allergies(), true, row.updatedAt()))
                .orElseGet(() -> new PatientProfileResponse(user.getId(), user.getFirstName(), user.getLastName(),
                        user.getEmail(), user.getPhoneNumber(), null, null, null, null, null, null, false, null));
    }

    private UsersRecord requirePatient(String email) {
        UsersRecord user = userRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));
        if (!"PATIENT".equals(user.getRole())) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only patients can access patient profiles");
        return user;
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return null;
        }

        return phoneNumber.trim();
    }
}
