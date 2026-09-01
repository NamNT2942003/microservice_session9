package com.example.pharmacyservice.service;

import com.example.pharmacyservice.dto.InvoiceRequest;
import com.example.pharmacyservice.dto.InvoiceResponse;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ElectronicInvoiceService {

    private static final Logger log = LoggerFactory.getLogger(ElectronicInvoiceService.class);
    private final AtomicInteger retryAttemptCounter = new AtomicInteger(0);

    @RateLimiter(name = "invoiceLimiter", fallbackMethod = "rateLimitFallback")
    @Retry(name = "invoiceRetry", fallbackMethod = "retryFallback")
    public InvoiceResponse issueInvoice(InvoiceRequest request) {
        int attempt = retryAttemptCounter.incrementAndGet();
        log.info(">>>> [XUAT HOA DON DIEN TU] Dang xu ly hoa don cho khach: {}, Lan thu: {}", 
                request.getCustomerName(), attempt);

        if (request.isSimulateNetworkError()) {
            log.warn(">>>> [LOI MANG TAM THOI] Ket noi he thong hoa don tong cuc thue bi gian doan (Lan thu: {})", attempt);
            throw new RuntimeException("Lỗi kết nối cổng xuất hóa đơn điện tử Tổng cục Thuế (Network Timeout)");
        }

        // Reset counter khi thành công
        retryAttemptCounter.set(0);

        return InvoiceResponse.builder()
                .invoiceNumber("HDDT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .status("SUCCESS")
                .message("Xuất hóa đơn điện tử thành công")
                .totalAmount(request.getTotalAmount())
                .attemptsCount(attempt)
                .issuedAt(LocalDateTime.now())
                .build();
    }

    // Fallback khi vượt quá giới hạn Rate Limiter (tối đa 5 hóa đơn / 10s)
    public InvoiceResponse rateLimitFallback(InvoiceRequest request, RequestNotPermitted exception) {
        log.warn(">>>> [RATE LIMITER TRIGGERED] May ban thuoc gui qua nhieu hoa don: {}", exception.getMessage());
        return InvoiceResponse.builder()
                .invoiceNumber("HDDT-REJECTED")
                .status("RATE_LIMITED")
                .message("Bạn đã gửi quá nhiều yêu cầu xuất hóa đơn (Giới hạn tối đa 5 hóa đơn / 10 giây). Vui lòng đợi trong giây lát!")
                .totalAmount(request.getTotalAmount())
                .attemptsCount(0)
                .issuedAt(LocalDateTime.now())
                .build();
    }

    // Fallback khi Retry 3 lần vẫn thất bại do lỗi mạng
    public InvoiceResponse retryFallback(InvoiceRequest request, Exception exception) {
        int totalAttempts = retryAttemptCounter.getAndSet(0);
        log.error(">>>> [RETRY EXHAUSTED FALLBACK] Da thu lai {} lan nhung van that bai: {}", 
                totalAttempts, exception.getMessage());
        return InvoiceResponse.builder()
                .invoiceNumber("HDDT-PENDING-SYNC")
                .status("SAVED_LOCALLY_PENDING_SYNC")
                .message("Cổng hóa đơn điện tử đang bảo trì/lỗi mạng sau 3 lần thử lại. Hóa đơn đã được lưu tạm offline tại quầy và sẽ tự đồng bộ sau.")
                .totalAmount(request.getTotalAmount())
                .attemptsCount(totalAttempts > 0 ? totalAttempts : 3)
                .issuedAt(LocalDateTime.now())
                .build();
    }
}
