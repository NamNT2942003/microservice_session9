package com.example.pharmacyservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceRequest {
    private String invoiceCode;
    private String customerName;
    private String customerTaxCode;
    private List<BillItemRequest> items;
    private Double totalAmount;
    private boolean simulateNetworkError; // Cho phép giả lập lỗi mạng để test retry
}
