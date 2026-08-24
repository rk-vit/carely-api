package com.carely.appointments.controller;

import com.carely.appointments.dto.AppointmentResponse;
import com.carely.appointments.dto.CreateAppointmentRequest;
import com.carely.appointments.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {
    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping("/holds")
    @ResponseStatus(HttpStatus.CREATED)
    public AppointmentResponse hold(Principal principal,
                                    @Valid @RequestBody CreateAppointmentRequest request) {
        return appointmentService.hold(principal.getName(), request);
    }

    @PostMapping("/{id}/confirm")
    public AppointmentResponse confirm(Principal principal, @PathVariable UUID id) {
        return appointmentService.confirm(id, principal.getName());
    }

    @GetMapping("/mine")
    public List<AppointmentResponse> mine(Principal principal) {
        return appointmentService.mine(principal.getName());
    }

    @GetMapping("/doctor")
    public List<AppointmentResponse> doctorAppointments(Principal principal) {
        return appointmentService.doctorAppointments(principal.getName());
    }
}
