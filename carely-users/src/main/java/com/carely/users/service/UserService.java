package com.carely.users.service;

import com.carely.jooq.generated.tables.records.UsersRecord;
import com.carely.users.dto.CreateUserRequest;
import com.carely.users.dto.CreateUserResponse;
import com.carely.users.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

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
