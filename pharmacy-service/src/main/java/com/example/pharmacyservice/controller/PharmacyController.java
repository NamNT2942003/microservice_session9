package com.example.pharmacyservice.controller;

import com.example.pharmacyservice.model.Medicine;
import com.example.pharmacyservice.repository.MedicineRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pharmacy")
public class PharmacyController {

    @Value("${app.branch-name:Chưa cấu hình}")
    private String branchName;

    @Value("${app.hotline:Chưa cấu hình}")
    private String hotline;

    @Value("${spring.datasource.url:Chưa cấu hình}")
    private String datasourceUrl;

    private final MedicineRepository medicineRepository;
    private final com.example.pharmacyservice.service.WarehouseClientService warehouseClientService;

    public PharmacyController(MedicineRepository medicineRepository, 
                              com.example.pharmacyservice.service.WarehouseClientService warehouseClientService) {
        this.medicineRepository = medicineRepository;
        this.warehouseClientService = warehouseClientService;
    }

    @GetMapping("/warehouse/check/{medicineName}")
    public ResponseEntity<Map<String, Object>> checkCentralWarehouseStock(
            @org.springframework.web.bind.annotation.PathVariable String medicineName) {
        return ResponseEntity.ok(warehouseClientService.checkStockInWarehouse(medicineName));
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getBranchInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("status", "SUCCESS");
        info.put("service", "pharmacy-service");
        info.put("branchName", branchName);
        info.put("hotline", hotline);
        info.put("datasourceUrl", datasourceUrl);
        info.put("totalMedicinesInStock", medicineRepository.count());
        return ResponseEntity.ok(info);
    }

    @GetMapping("/medicines")
    public ResponseEntity<List<Medicine>> getAllMedicines() {
        return ResponseEntity.ok(medicineRepository.findAll());
    }

    @PostMapping("/medicines")
    public ResponseEntity<Medicine> addMedicine(@RequestBody Medicine medicine) {
        return ResponseEntity.ok(medicineRepository.save(medicine));
    }
}
