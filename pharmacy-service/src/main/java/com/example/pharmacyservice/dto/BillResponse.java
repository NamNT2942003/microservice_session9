package com.example.pharmacyservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillResponse {
    private String branchName;
    private List<BillItemRequest> items;
    private Double subTotal;
    private Double vatRate;
    private String vatRateFormatted;
    private Double vatAmount;
    private Double totalAmount;
    private LocalDateTime createdAt;
}
