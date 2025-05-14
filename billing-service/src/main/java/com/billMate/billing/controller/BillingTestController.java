package com.billMate.billing.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/billing")
public class BillingTestController {

    @GetMapping("/test")
    public String testEndpoint(){
        return "Acceso autorizado a billing-service";
    }
}
