package com.carely.doctors.service;

import com.carely.doctors.dto.CreateDoctorRequest;
import com.carely.doctors.dto.DoctorResponse;
import com.carely.doctors.dto.UpdateDoctorRequest;
import com.carely.doctors.repository.DoctorRepository;
import com.carely.jooq.generated.tables.records.DoctorsRecord;
import com.carely.jooq.generated.tables.records.UsersRecord;
import com.carely.users.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class DoctorService {
    private final DoctorRepository doctorRepository;
    private final UserService userService;

    public DoctorService(DoctorRepository doctorRepository, UserService userService) {
        this.doctorRepository = doctorRepository;
        this.userService = userService;
    }

    @Transactional
    public DoctorResponse createDoctor(CreateDoctorRequest request) {
        validateHours(request.workingStartTime(), request.workingEndTime());
        UsersRecord user = userService.createDoctorUser(
                request.email(), request.temporaryPassword(), request.firstName(),
                request.lastName(), request.phoneNumber());

        DoctorsRecord doctor = doctorRepository.insertDoctor(
                UUID.randomUUID(), user.getId(), request.specialization().trim(),
                request.medicalLicenseNumber().trim(), defaultInt(request.yearsOfExperience(), 0),
                defaultMoney(request.consultationFee()), request.biography(),
                request.workingStartTime(), request.workingEndTime(),
                defaultInt(request.slotDurationMinutes(), 30));

        return getDoctor(doctor.getId());
    }

    @Transactional
    public DoctorResponse updateDoctor(UUID id, UpdateDoctorRequest request) {
        DoctorResponse current = getDoctor(id);
        String specialization = request.specialization() == null ? current.specialization() : request.specialization().trim();
        String license = request.medicalLicenseNumber() == null ? current.medicalLicenseNumber() : request.medicalLicenseNumber().trim();
        Integer experience = request.yearsOfExperience() == null ? current.yearsOfExperience() : request.yearsOfExperience();
        BigDecimal fee = request.consultationFee() == null ? current.consultationFee() : request.consultationFee();
        String biography = request.biography() == null ? current.biography() : request.biography();
        var start = request.workingStartTime() == null ? current.workingStartTime() : request.workingStartTime();
        var end = request.workingEndTime() == null ? current.workingEndTime() : request.workingEndTime();
        Integer slot = request.slotDurationMinutes() == null ? current.slotDurationMinutes() : request.slotDurationMinutes();
        Boolean active = request.active() == null ? current.active() : request.active();
        validateHours(start, end);
        doctorRepository.updateDoctor(id, specialization, license, experience, fee, biography, start, end, slot, active);
        return getDoctor(id);
    }

    @Transactional(readOnly = true)
    public DoctorResponse getDoctor(UUID id) {
        return doctorRepository.findResponseById(id).map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found"));
    }

    @Transactional(readOnly = true)
    public DoctorResponse getDoctorByEmail(String email) {
        return doctorRepository.findResponseByEmail(email).map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor profile not found"));
    }

    private DoctorResponse toResponse(DoctorRepository.DoctorResponseRow row) {
        return new DoctorResponse(row.id(), row.userId(), row.email(), row.firstName(), row.lastName(),
                row.phoneNumber(), row.specialization(), row.medicalLicenseNumber(), row.yearsOfExperience(),
                row.consultationFee(), row.biography(), row.workingStartTime(), row.workingEndTime(),
                row.slotDurationMinutes(), row.active());
    }

    private void validateHours(java.time.LocalTime start, java.time.LocalTime end) {
        if (start != null && end != null && !start.isBefore(end)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Working start time must be before end time");
        }
    }

    private int defaultInt(Integer value, int fallback) { return value == null ? fallback : value; }
    private BigDecimal defaultMoney(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }
}
