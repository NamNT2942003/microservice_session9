package com.example.warehouse.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api/v1/warehouse")
public class WarehouseController {

    private final AtomicBoolean isFailed = new AtomicBoolean(false);
    private final AtomicInteger delaySeconds = new AtomicInteger(0);

    @GetMapping("/check/{medicineName}")
    public ResponseEntity<Map<String, Object>> checkStock(@PathVariable String medicineName) {
        if (isFailed.get()) {
            throw new RuntimeException("Warehouse Service Error: Kho tổng đang gặp sự cố kết nối!");
        }

        if (delaySeconds.get() > 0) {
            try {
                Thread.sleep(delaySeconds.get() * 1000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("medicineName", medicineName);
        result.put("inCentralWarehouse", true);
        result.put("stockQuantity", 500);
        result.put("status", "AVAILABLE_IN_CENTRAL_WAREHOUSE");
        result.put("warehouseLocation", "Kho Tong Trung Tam Ha Noi");
        return ResponseEntity.ok(result);
    }

    @PostMapping("/simulate/status")
    public ResponseEntity<Map<String, Object>> setSimulateStatus(
            @RequestParam(defaultValue = "false") boolean failure,
            @RequestParam(defaultValue = "0") int delay) {
        isFailed.set(failure);
        delaySeconds.set(delay);

        Map<String, Object> status = new HashMap<>();
        status.put("simulatedFailure", isFailed.get());
        status.put("simulatedDelaySeconds", delaySeconds.get());
        return ResponseEntity.ok(status);
    }
}
