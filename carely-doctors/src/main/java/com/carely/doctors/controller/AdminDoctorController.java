package com.carely.doctors.controller;

import com.carely.doctors.dto.CreateDoctorRequest;
import com.carely.doctors.dto.DoctorResponse;
import com.carely.doctors.dto.UpdateDoctorRequest;
import com.carely.doctors.service.DoctorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/doctors")
public class AdminDoctorController {
    private final DoctorService doctorService;

    public AdminDoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DoctorResponse create(@Valid @RequestBody CreateDoctorRequest request) {
        return doctorService.createDoctor(request);
    }


    @GetMapping("/{id}")
    public DoctorResponse get(@PathVariable UUID id) {
        return doctorService.getDoctor(id);
    }

    @PatchMapping("/{id}")
    public DoctorResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateDoctorRequest request) {
        return doctorService.updateDoctor(id, request);
    }
}
