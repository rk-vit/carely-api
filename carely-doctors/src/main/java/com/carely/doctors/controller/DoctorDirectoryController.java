package com.carely.doctors.controller;

import com.carely.doctors.dto.DoctorDirectoryResponse;
import com.carely.doctors.service.DoctorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/doctors")
public class DoctorDirectoryController {
    private final DoctorService doctorService;

    public DoctorDirectoryController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @GetMapping
    public List<DoctorDirectoryResponse> list() {
        return doctorService.listDoctors();
    }
}
