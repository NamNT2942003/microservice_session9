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
public class BillRequest {
    private List<BillItemRequest> items;
    private Double subTotal; // Tùy chọn nếu muốn truyền trực tiếp tổng tiền trước thuế
}
