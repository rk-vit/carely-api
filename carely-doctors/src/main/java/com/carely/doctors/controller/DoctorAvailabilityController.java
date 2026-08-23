package com.carely.doctors.controller;

import com.carely.doctors.dto.AvailabilityOverrideRequest;
import com.carely.doctors.dto.AvailabilityOverrideResponse;
import com.carely.doctors.dto.AvailabilityRequest;
import com.carely.doctors.dto.AvailabilityResponse;
import com.carely.doctors.dto.SlotResponse;
import com.carely.doctors.service.AvailabilityService;
import com.carely.doctors.service.DoctorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
public class DoctorAvailabilityController {
    private final AvailabilityService service;
    private final DoctorService doctorService;

    public DoctorAvailabilityController(AvailabilityService service, DoctorService doctorService) {
        this.service = service;
        this.doctorService = doctorService;
    }

    @GetMapping("/doctor/availability")
    public List<AvailabilityResponse> list(Principal principal) {
        return service.listAvailability(doctorId(principal));
    }

    @PutMapping("/doctor/availability/{day}")
    public AvailabilityResponse save(Principal principal, @PathVariable DayOfWeek day,
                                     @Valid @RequestBody AvailabilityRequest request) {
        return service.saveAvailability(doctorId(principal), day, request);
    }

    @DeleteMapping("/doctor/availability/{day}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Principal principal, @PathVariable DayOfWeek day) {
        service.deleteAvailability(doctorId(principal), day);
    }

    @PostMapping("/doctor/availability-overrides")
    @ResponseStatus(HttpStatus.CREATED)
    public AvailabilityOverrideResponse addOverride(Principal principal,
                                                     @Valid @RequestBody AvailabilityOverrideRequest request) {
        return service.addOverride(doctorId(principal), request);
    }

    @GetMapping("/doctor/availability-overrides")
    public List<AvailabilityOverrideResponse> listOverrides(Principal principal,
                                                              @RequestParam(required = false) LocalDate date) {
        return service.listOverrides(doctorId(principal), date);
    }

    @DeleteMapping("/doctor/availability-overrides/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOverride(Principal principal, @PathVariable UUID id) {
        service.deleteOverride(doctorId(principal), id);
    }

    @GetMapping("/doctors/{doctorId}/slots")
    public List<SlotResponse> slots(@PathVariable UUID doctorId, @RequestParam LocalDate date) {
        return service.slots(doctorId, date);
    }

    private UUID doctorId(Principal principal) {
        return doctorService.getDoctorByEmail(principal.getName()).id();
    }
}
