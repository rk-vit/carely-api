package com.carely.appointments.service;

import com.carely.appointments.dto.AppointmentResponse;
import com.carely.appointments.dto.CreateAppointmentRequest;
import com.carely.appointments.repository.AppointmentRepository;
import com.carely.doctors.service.AvailabilityService;
import com.carely.doctors.service.DoctorService;
import com.carely.jooq.generated.tables.records.AppointmentsRecord;
import com.carely.jooq.generated.tables.records.UsersRecord;
import com.carely.users.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AppointmentService {
    private static final int SLOT_DURATION_MINUTES = 30;
    private static final int HOLD_DURATION_MINUTES = 5;

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final AvailabilityService availabilityService;
    private final DoctorService doctorService;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              UserRepository userRepository,
                              AvailabilityService availabilityService,
                              DoctorService doctorService) {
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.availabilityService = availabilityService;
        this.doctorService = doctorService;
    }

    @Transactional
    public AppointmentResponse hold(String patientEmail, CreateAppointmentRequest request) {
        UsersRecord patient = requirePatient(patientEmail);
        validateRequestedSlot(request);

        boolean available = availabilityService
                .slots(request.doctorId(), request.startAt().toLocalDate())
                .stream()
                .anyMatch(slot -> slot.startAt().toInstant().equals(request.startAt().toInstant())
                        && slot.endAt().toInstant().equals(request.endAt().toInstant())
                        && "AVAILABLE".equals(slot.status()));

        if (!available) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Selected slot is unavailable");
        }

        OffsetDateTime holdExpiresAt = OffsetDateTime.now().plusMinutes(HOLD_DURATION_MINUTES);

        try {
            AppointmentsRecord appointment = appointmentRepository.insertHold(
                    UUID.randomUUID(),
                    request.doctorId(),
                    patient.getId(),
                    request.startAt(),
                    request.endAt(),
                    request.symptoms().trim(),
                    holdExpiresAt
            );
            return toResponse(appointment);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Selected slot was just booked by another patient"
            );
        }
    }

    @Transactional
    public AppointmentResponse confirm(UUID appointmentId, String patientEmail) {
        requireOwnedAppointment(appointmentId, patientEmail);

        AppointmentsRecord booked = appointmentRepository.confirmHold(appointmentId);
        if (booked == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Appointment hold expired");
        }

        return toResponse(booked);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> mine(String patientEmail) {
        UUID patientId = requirePatient(patientEmail).getId();
        return appointmentRepository.findByPatientId(patientId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> doctorAppointments(String doctorEmail) {
        UUID doctorId = doctorService.getDoctorByEmail(doctorEmail).id();
        return appointmentRepository.findByDoctorId(doctorId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private UsersRecord requirePatient(String email) {
        UsersRecord user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));

        if (!"PATIENT".equals(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only patients can book appointments");
        }
        return user;
    }

    private AppointmentsRecord requireOwnedAppointment(UUID appointmentId, String patientEmail) {
        AppointmentsRecord appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));
        UUID patientId = requirePatient(patientEmail).getId();

        if (!patientId.equals(appointment.getPatientId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Appointment does not belong to you");
        }
        return appointment;
    }

    private void validateRequestedSlot(CreateAppointmentRequest request) {
        if (!request.startAt().isBefore(request.endAt())
                || !request.startAt().isAfter(OffsetDateTime.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Appointment must be a future 30-minute slot"
            );
        }

        long duration = Duration.between(request.startAt(), request.endAt()).toMinutes();
        if (duration != SLOT_DURATION_MINUTES || request.startAt().getMinute() % SLOT_DURATION_MINUTES != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Appointment must be a 30-minute slot");
        }
    }

    private AppointmentResponse toResponse(AppointmentsRecord appointment) {
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getDoctorId(),
                appointment.getPatientId(),
                appointment.getStartAt(),
                appointment.getEndAt(),
                appointment.getStatus(),
                appointment.getSymptoms(),
                appointment.getHoldExpiresAt(),
                userRepository.findById(appointment.getPatientId())
                        .map(user -> (user.getFirstName() + " " + user.getLastName()).trim())
                        .orElse("Patient"),
                userRepository.findById(appointment.getPatientId())
                        .map(UsersRecord::getEmail)
                        .orElse(null)
        );
    }
}
