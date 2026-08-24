package com.carely.appointments.service;

import com.carely.appointments.dto.AppointmentResponse;
import com.carely.appointments.dto.CreateAppointmentRequest;
import com.carely.appointments.dto.RescheduleAppointmentRequest;
import com.carely.appointments.dto.ConsultationRequest;
import com.carely.appointments.dto.ConsultationResponse;
import com.carely.appointments.repository.AppointmentRepository;
import com.carely.doctors.service.AvailabilityService;
import com.carely.doctors.service.DoctorService;
import com.carely.jooq.generated.tables.records.AppointmentsRecord;
import com.carely.jooq.generated.tables.records.UsersRecord;
import com.carely.users.repository.UserRepository;
import com.carely.users.dto.PatientProfileResponse;
import com.carely.users.service.UserService;
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
    private final UserService userService;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              UserRepository userRepository,
                              AvailabilityService availabilityService,
                              DoctorService doctorService,
                              UserService userService) {
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.availabilityService = availabilityService;
        this.doctorService = doctorService;
        this.userService = userService;
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

    @Transactional
    public AppointmentResponse cancel(UUID appointmentId, String patientEmail) {
        requireOwnedAppointment(appointmentId, patientEmail);
        AppointmentsRecord cancelled = appointmentRepository.cancel(appointmentId);
        if (cancelled == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only held or booked appointments can be cancelled");
        }
        return toResponse(cancelled);
    }

    @Transactional
    public AppointmentResponse reschedule(UUID appointmentId, String patientEmail, RescheduleAppointmentRequest request) {
        AppointmentsRecord current = requireOwnedAppointment(appointmentId, patientEmail);
        validateRequestedSlot(new CreateAppointmentRequest(current.getDoctorId(), request.startAt(), request.endAt(), current.getSymptoms()));
        boolean available = availabilityService.slots(current.getDoctorId(), request.startAt().toLocalDate()).stream()
                .anyMatch(slot -> slot.startAt().toInstant().equals(request.startAt().toInstant())
                        && slot.endAt().toInstant().equals(request.endAt().toInstant())
                        && "AVAILABLE".equals(slot.status()));
        if (!available) throw new ResponseStatusException(HttpStatus.CONFLICT, "Selected slot is unavailable");
        try {
            AppointmentsRecord moved = appointmentRepository.reschedule(appointmentId, request.startAt(), request.endAt());
            if (moved == null) throw new ResponseStatusException(HttpStatus.CONFLICT, "Only booked appointments can be rescheduled");
            return toResponse(moved);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Selected slot was just booked by another patient");
        }
    }

    @Transactional
    public AppointmentResponse updateDoctorStatus(UUID appointmentId, String doctorEmail, String status) {
        UUID doctorId = doctorService.getDoctorByEmail(doctorEmail).id();
        AppointmentsRecord appointment = requireAppointment(appointmentId);
        if (!doctorId.equals(appointment.getDoctorId())) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Appointment does not belong to you");
        return updateStatus(appointmentId, status);
    }

    @Transactional
    public AppointmentResponse updateAdminStatus(UUID appointmentId, String status) {
        requireAppointment(appointmentId);
        return updateStatus(appointmentId, status);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> allAppointments() {
        return appointmentRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PatientProfileResponse patientProfileForDoctor(UUID appointmentId, String doctorEmail) {
        UUID doctorId = doctorService.getDoctorByEmail(doctorEmail).id();
        AppointmentsRecord appointment = requireAppointment(appointmentId);
        if (!doctorId.equals(appointment.getDoctorId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Appointment does not belong to you");
        }
        return userService.getPatientProfile(appointment.getPatientId());
    }

    @Transactional
    public ConsultationResponse submitConsultation(UUID appointmentId, String doctorEmail, ConsultationRequest request) {
        UUID doctorId = doctorService.getDoctorByEmail(doctorEmail).id();
        AppointmentsRecord appointment = requireAppointment(appointmentId);
        if (!doctorId.equals(appointment.getDoctorId())) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Appointment does not belong to you");
        if (!List.of("BOOKED", "COMPLETED").contains(appointment.getStatus())) throw new ResponseStatusException(HttpStatus.CONFLICT, "This appointment cannot receive consultation notes");
        appointmentRepository.upsertConsultation(appointmentId, request.clinicalNotes().trim(), clean(request.diagnosis()),
                clean(request.prescription()), clean(request.summary()), request.followUpDate());
        if ("BOOKED".equals(appointment.getStatus())) appointmentRepository.updateStatus(appointmentId, "COMPLETED");
        return consultationResponse(appointmentId);
    }

    @Transactional(readOnly = true)
    public ConsultationResponse getConsultationForPatient(UUID appointmentId, String patientEmail) {
        AppointmentsRecord appointment = requireAppointment(appointmentId);
        if (!appointment.getPatientId().equals(requirePatient(patientEmail).getId())) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Appointment does not belong to you");
        return consultationResponse(appointmentId);
    }

    private ConsultationResponse consultationResponse(UUID appointmentId) {
        return appointmentRepository.findConsultation(appointmentId)
                .map(row -> new ConsultationResponse(appointmentId, row.clinicalNotes(), row.diagnosis(), row.prescription(), row.summary(), row.followUpDate(), row.updatedAt()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Consultation has not been submitted"));
    }

    private String clean(String value) { return value == null || value.isBlank() ? null : value.trim(); }

    private AppointmentResponse updateStatus(UUID appointmentId, String status) {
        if (!List.of("CANCELLED", "COMPLETED", "NO_SHOW").contains(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported appointment status");
        }
        AppointmentsRecord updated = appointmentRepository.updateStatus(appointmentId, status);
        return toResponse(updated);
    }

    private AppointmentsRecord requireAppointment(UUID appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));
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
