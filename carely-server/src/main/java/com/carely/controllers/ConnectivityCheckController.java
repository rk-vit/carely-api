package com.carely.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConnectivityCheckController {

    @GetMapping("/fe-connection-check")
    public String conCheck(){
        return "Connection Pakka da bhai";
    }
}
