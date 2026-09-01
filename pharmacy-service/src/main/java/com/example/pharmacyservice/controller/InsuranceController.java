package com.example.pharmacyservice.controller;

import com.example.pharmacyservice.dto.InsuranceRequest;
import com.example.pharmacyservice.dto.InsuranceResponse;
import com.example.pharmacyservice.service.InsuranceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/insurance")
public class InsuranceController {

    private final InsuranceService insuranceService;

    public InsuranceController(InsuranceService insuranceService) {
        this.insuranceService = insuranceService;
    }

    @PostMapping("/verify")
    public CompletableFuture<ResponseEntity<InsuranceResponse>> verifyInsurance(@RequestBody InsuranceRequest request) {
        return insuranceService.verifyInsurance(request).thenApply(ResponseEntity::ok);
    }
}
