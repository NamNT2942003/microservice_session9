package com.example.pharmacyservice.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class WarehouseClientService {

    private static final Logger log = LoggerFactory.getLogger(WarehouseClientService.class);

    private final RestTemplate restTemplate;

    @Value("${warehouse.service.url:http://localhost:8082}")
    private String warehouseServiceUrl;

    public WarehouseClientService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @CircuitBreaker(name = "warehouseCB", fallbackMethod = "checkWarehouseFallback")
    public Map<String, Object> checkStockInWarehouse(String medicineName) {
        String url = warehouseServiceUrl + "/api/v1/warehouse/check/" + medicineName;
        log.info(">>>> [CALLING WAREHOUSE-SERVICE] URL: {}", url);
        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        return response;
    }

    public Map<String, Object> checkWarehouseFallback(String medicineName, Throwable throwable) {
        log.warn(">>>> [CIRCUIT BREAKER TRIGGERED / FALLBACK] Failed to call warehouse for medicine: {}. Reason: {}", 
                medicineName, throwable.getMessage());

        Map<String, Object> fallbackResponse = new HashMap<>();
        fallbackResponse.put("medicineName", medicineName);
        fallbackResponse.put("inCentralWarehouse", false);
        fallbackResponse.put("status", "FALLBACK_OFFLINE_MODE");
        fallbackResponse.put("message", "Kho tong dang gap su co hoac mach dang OPEN. Vui long kiem tra hang ton cuc bo tai cua hang.");
        fallbackResponse.put("errorDetail", throwable.getClass().getSimpleName() + ": " + throwable.getMessage());
        return fallbackResponse;
    }
}
