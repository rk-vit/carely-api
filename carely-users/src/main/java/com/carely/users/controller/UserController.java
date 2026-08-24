package com.carely.users.controller;

import com.carely.users.dto.CreateUserRequest;
import com.carely.users.dto.CreateUserResponse;
import com.carely.users.dto.PatientProfileResponse;
import com.carely.users.dto.UpdatePatientProfileRequest;
import com.carely.users.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateUserResponse createUser(@Valid @RequestBody CreateUserRequest req){
        return userService.createUser(req);
    }

    @GetMapping("/patient/profile")
    public PatientProfileResponse patientProfile(java.security.Principal principal) {
        return userService.getPatientProfile(principal.getName());
    }

    @PatchMapping("/patient/profile")
    public PatientProfileResponse updatePatientProfile(java.security.Principal principal,
                                                       @Valid @RequestBody UpdatePatientProfileRequest request) {
        return userService.updatePatientProfile(principal.getName(), request);
    }

}
