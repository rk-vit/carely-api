package com.carely.doctors.controller;

import com.carely.doctors.dto.CreateLeaveRequest;
import com.carely.doctors.dto.LeaveRequestResponse;
import com.carely.doctors.service.LeaveRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/doctor/leave-requests")
public class DoctorLeaveRequestController {
    private final LeaveRequestService leaveService;

    public DoctorLeaveRequestController(LeaveRequestService leaveService) {
        this.leaveService = leaveService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LeaveRequestResponse create(Principal principal, @Valid @RequestBody CreateLeaveRequest request) {
        return leaveService.create(principal.getName(), request);
    }

    @GetMapping
    public List<LeaveRequestResponse> list(Principal principal) {
        return leaveService.listForDoctor(principal.getName());
    }
}
