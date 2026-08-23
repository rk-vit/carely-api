package com.carely.doctors.service;

import com.carely.doctors.dto.CreateLeaveRequest;
import com.carely.doctors.dto.LeaveRequestResponse;
import com.carely.doctors.dto.ReviewLeaveRequest;
import com.carely.doctors.repository.LeaveRequestRepository;
import com.carely.users.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class LeaveRequestService {
    private final LeaveRequestRepository leaveRepository;
    private final DoctorService doctorService;
    private final UserRepository userRepository;

    public LeaveRequestService(LeaveRequestRepository leaveRepository, DoctorService doctorService,
                               UserRepository userRepository) {
        this.leaveRepository = leaveRepository;
        this.doctorService = doctorService;
        this.userRepository = userRepository;
    }

    @Transactional
    public LeaveRequestResponse create(String email, CreateLeaveRequest request) {
        if (request.endDate().isBefore(request.startDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date must be on or after start date");
        }
        UUID doctorId = doctorService.getDoctorByEmail(email).id();
        if (leaveRepository.existsOverlapping(doctorId, request.startDate(), request.endDate(), null)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This leave period overlaps an existing request");
        }
        var saved = leaveRepository.insert(UUID.randomUUID(), doctorId, request.startDate(), request.endDate(), request.reason().trim());
        return get(saved.getId());
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestResponse> listForDoctor(String email) {
        UUID doctorId = doctorService.getDoctorByEmail(email).id();
        return leaveRepository.findForDoctor(doctorId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestResponse> listForAdmin(String status) {
        if (status != null && !status.matches("PENDING|APPROVED|REJECTED")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid leave request status");
        }
        return leaveRepository.findAll(status).stream().map(this::toResponse).toList();
    }

    @Transactional
    public LeaveRequestResponse review(UUID id, String adminEmail, String decision, ReviewLeaveRequest request) {
        var current = leaveRepository.find(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Leave request not found"));
        if (!"PENDING".equals(current.status())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only pending leave requests can be reviewed");
        }
        if ("APPROVED".equals(decision) && leaveRepository.existsOverlapping(
                current.doctorId(), current.startDate(), current.endDate(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This approval overlaps another leave request");
        }
        UUID reviewerId = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Admin account not found"))
                .getId();
        leaveRepository.review(id, decision, reviewerId, request == null ? null : request.reviewerNote());
        return get(id);
    }

    @Transactional(readOnly = true)
    public LeaveRequestResponse get(UUID id) {
        return leaveRepository.find(id).map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Leave request not found"));
    }

    private LeaveRequestResponse toResponse(LeaveRequestRepository.LeaveRow row) {
        String name = (row.firstName() == null ? "" : row.firstName()) +
                (row.lastName() == null ? "" : " " + row.lastName());
        return new LeaveRequestResponse(row.id(), row.doctorId(), name.trim(), row.email(), row.startDate(),
                row.endDate(), row.reason(), row.status(), row.reviewedBy(), row.reviewedAt(),
                row.reviewerNote(), row.createdAt());
    }
}
