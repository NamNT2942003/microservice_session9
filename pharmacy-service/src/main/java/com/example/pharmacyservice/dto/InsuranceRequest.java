package com.example.pharmacyservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsuranceRequest {
    private String insuranceCardNumber;
    private String patientName;
    private String medicineName;
    private Double originalPrice;
    private int simulateDelaySeconds; // Giả lập phản hồi chậm từ cổng bảo hiểm
    private boolean simulateError;        // Giả lập cổng bảo hiểm bị sập
}
