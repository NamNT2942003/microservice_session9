package com.example.pharmacyservice.controller;

import com.example.pharmacyservice.dto.BillItemRequest;
import com.example.pharmacyservice.dto.BillRequest;
import com.example.pharmacyservice.dto.BillResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/bill")
@RefreshScope
public class BillController {

    @Value("${pharmacy.vat-rate:0.1}")
    private Double vatRate;

    @Value("${app.branch-name:Nha Thuoc So 1}")
    private String branchName;

    @GetMapping("/vat-rate")
    public ResponseEntity<Map<String, Object>> getCurrentVatRate() {
        Map<String, Object> response = new HashMap<>();
        response.put("branchName", branchName);
        response.put("vatRate", vatRate);
        response.put("vatPercentage", String.format("%.1f%%", vatRate * 100));
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<BillResponse> createBill(@RequestBody BillRequest request) {
        double subTotal = 0.0;

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (BillItemRequest item : request.getItems()) {
                double price = item.getPrice() != null ? item.getPrice() : 0.0;
                int quantity = item.getQuantity() != null ? item.getQuantity() : 1;
                subTotal += (price * quantity);
            }
        } else if (request.getSubTotal() != null) {
            subTotal = request.getSubTotal();
        }

        // Công thức tính: tổng tiền thuốc + % thuế VAT của tổng tiền thuốc
        double vatAmount = subTotal * vatRate;
        double totalAmount = subTotal + vatAmount;

        BillResponse response = BillResponse.builder()
                .branchName(branchName)
                .items(request.getItems())
                .subTotal(subTotal)
                .vatRate(vatRate)
                .vatRateFormatted(String.format("%.1f%%", vatRate * 100))
                .vatAmount(vatAmount)
                .totalAmount(totalAmount)
                .createdAt(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }
}
