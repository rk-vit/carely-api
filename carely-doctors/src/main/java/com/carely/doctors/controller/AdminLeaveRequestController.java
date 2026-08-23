package com.carely.doctors.controller;

import com.carely.doctors.dto.LeaveRequestResponse;
import com.carely.doctors.dto.ReviewLeaveRequest;
import com.carely.doctors.service.LeaveRequestService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/leave-requests")
public class AdminLeaveRequestController {
    private final LeaveRequestService leaveService;

    public AdminLeaveRequestController(LeaveRequestService leaveService) {
        this.leaveService = leaveService;
    }

    @GetMapping
    public List<LeaveRequestResponse> list(@RequestParam(required = false) String status) {
        return leaveService.listForAdmin(status);
    }

    @PatchMapping("/{id}/approve")
    public LeaveRequestResponse approve(Principal principal, @PathVariable UUID id,
                                        @Valid @RequestBody(required = false) ReviewLeaveRequest request) {
        return leaveService.review(id, principal.getName(), "APPROVED", request);
    }

    @PatchMapping("/{id}/reject")
    public LeaveRequestResponse reject(Principal principal, @PathVariable UUID id,
                                       @Valid @RequestBody(required = false) ReviewLeaveRequest request) {
        return leaveService.review(id, principal.getName(), "REJECTED", request);
    }
}
