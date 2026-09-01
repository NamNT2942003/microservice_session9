package com.example.pharmacyservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceResponse {
    private String invoiceNumber;
    private String status;
    private String message;
    private Double totalAmount;
    private Integer attemptsCount;
    private LocalDateTime issuedAt;
}
