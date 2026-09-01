package com.example.pharmacyservice.service;

import com.example.pharmacyservice.repository.MedicineRepository;
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
    private final MedicineRepository medicineRepository;

    @Value("${warehouse.service.url:http://localhost:8082}")
    private String warehouseServiceUrl;

    public WarehouseClientService(RestTemplate restTemplate, MedicineRepository medicineRepository) {
        this.restTemplate = restTemplate;
        this.medicineRepository = medicineRepository;
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
        log.warn(">>>> [CIRCUIT BREAKER FALLBACK] Mất kết nối kho tổng khi kiểm tra thuốc: {}. Lý do: {}", 
                medicineName, throwable.getMessage());

        // Lấy dữ liệu tồn kho cục bộ tại quầy thuốc
        long localStock = medicineRepository.findAll().stream()
                .filter(m -> m.getName() != null && m.getName().toLowerCase().contains(medicineName.toLowerCase()))
                .mapToInt(m -> m.getQuantity() != null ? m.getQuantity() : 0)
                .sum();

        Map<String, Object> fallbackResponse = new HashMap<>();
        fallbackResponse.put("medicineName", medicineName);
        fallbackResponse.put("inCentralWarehouse", false);
        fallbackResponse.put("status", "FALLBACK_LOCAL_MODE");
        fallbackResponse.put("message", "Không thể kết nối kho tổng. Hệ thống sẽ sử dụng dữ liệu tồn kho cục bộ để tiếp tục giao dịch");
        fallbackResponse.put("localStockQuantity", localStock);
        fallbackResponse.put("note", "Cho phép bán lẻ dựa trên số lượng tồn kho tại chỗ.");
        fallbackResponse.put("errorType", throwable.getClass().getSimpleName());
        return fallbackResponse;
    }
}
