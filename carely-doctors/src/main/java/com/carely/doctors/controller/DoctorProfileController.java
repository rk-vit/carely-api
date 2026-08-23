package com.carely.doctors.controller;

import com.carely.doctors.dto.DoctorResponse;
import com.carely.doctors.dto.UpdateDoctorRequest;
import com.carely.doctors.service.DoctorService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/doctor/profile")
public class DoctorProfileController {
    private final DoctorService doctorService;

    public DoctorProfileController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @GetMapping
    public DoctorResponse get(java.security.Principal principal) {
        return doctorService.getDoctorByEmail(principal.getName());
    }

    @PatchMapping
    public DoctorResponse update(java.security.Principal principal,
                                 @Valid @RequestBody UpdateDoctorRequest request) {
        DoctorResponse current = doctorService.getDoctorByEmail(principal.getName());
        return doctorService.updateDoctor(current.id(), request);
    }
}
