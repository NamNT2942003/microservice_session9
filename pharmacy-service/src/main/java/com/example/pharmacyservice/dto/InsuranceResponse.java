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
public class InsuranceResponse {
    private String insuranceCardNumber;
    private String patientName;
    private String medicineName;
    private Double originalPrice;
    private Double discountRate;
    private Double discountAmount;
    private Double finalPriceToPay;
    private String status;
    private String note;
    private LocalDateTime verifiedAt;
}
