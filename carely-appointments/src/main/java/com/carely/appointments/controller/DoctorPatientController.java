package com.carely.appointments.controller;

import com.carely.appointments.service.AppointmentService;
import com.carely.appointments.dto.ConsultationRequest;
import com.carely.appointments.dto.ConsultationResponse;
import jakarta.validation.Valid;
import com.carely.users.dto.PatientProfileResponse;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/doctor/appointments")
public class DoctorPatientController {
    private final AppointmentService appointmentService;
    public DoctorPatientController(AppointmentService appointmentService) { this.appointmentService = appointmentService; }

    @GetMapping("/{id}/patient-profile")
    public PatientProfileResponse patientProfile(Principal principal, @PathVariable UUID id) {
        return appointmentService.patientProfileForDoctor(id, principal.getName());
    }

    @PatchMapping("/{id}/consultation")
    public ConsultationResponse consultation(Principal principal, @PathVariable UUID id,
                                             @Valid @RequestBody ConsultationRequest request) {
        return appointmentService.submitConsultation(id, principal.getName(), request);
    }
}
