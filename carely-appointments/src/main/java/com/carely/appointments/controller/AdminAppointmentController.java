package com.carely.appointments.controller;

import com.carely.appointments.dto.AppointmentResponse;
import com.carely.appointments.dto.UpdateAppointmentStatusRequest;
import com.carely.appointments.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/appointments")
public class AdminAppointmentController {
    private final AppointmentService appointmentService;
    public AdminAppointmentController(AppointmentService appointmentService) { this.appointmentService = appointmentService; }

    @GetMapping
    public List<AppointmentResponse> all() { return appointmentService.allAppointments(); }

    @PatchMapping("/{id}/status")
    public AppointmentResponse status(@PathVariable UUID id, @Valid @RequestBody UpdateAppointmentStatusRequest request) {
        return appointmentService.updateAdminStatus(id, request.status());
    }
}
