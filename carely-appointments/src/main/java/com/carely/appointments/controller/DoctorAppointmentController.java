package com.carely.appointments.controller;

import com.carely.appointments.dto.AppointmentResponse;
import com.carely.appointments.dto.UpdateAppointmentStatusRequest;
import com.carely.appointments.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/doctor/appointments")
public class DoctorAppointmentController {
    private final AppointmentService appointmentService;
    public DoctorAppointmentController(AppointmentService appointmentService) { this.appointmentService = appointmentService; }

    @PatchMapping("/{id}/status")
    public AppointmentResponse status(Principal principal, @PathVariable UUID id,
                                      @Valid @RequestBody UpdateAppointmentStatusRequest request) {
        return appointmentService.updateDoctorStatus(id, principal.getName(), request.status());
    }
}
