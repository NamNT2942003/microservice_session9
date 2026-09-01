package com.example.pharmacyservice.controller;

import com.example.pharmacyservice.dto.InvoiceRequest;
import com.example.pharmacyservice.dto.InvoiceResponse;
import com.example.pharmacyservice.service.ElectronicInvoiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/invoice")
public class InvoiceController {

    private final ElectronicInvoiceService invoiceService;

    public InvoiceController(ElectronicInvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @PostMapping("/issue")
    public ResponseEntity<InvoiceResponse> issueInvoice(@RequestBody InvoiceRequest request) {
        return ResponseEntity.ok(invoiceService.issueInvoice(request));
    }
}
