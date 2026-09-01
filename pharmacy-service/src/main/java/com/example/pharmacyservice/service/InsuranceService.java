package com.example.pharmacyservice.service;

import com.example.pharmacyservice.dto.InsuranceRequest;
import com.example.pharmacyservice.dto.InsuranceResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Service
public class InsuranceService {

    private static final Logger log = LoggerFactory.getLogger(InsuranceService.class);

    @CircuitBreaker(name = "insuranceCB", fallbackMethod = "insuranceFallback")
    @TimeLimiter(name = "insuranceTL", fallbackMethod = "insuranceFallback")
    @Retry(name = "insuranceRetry", fallbackMethod = "insuranceFallback")
    public CompletableFuture<InsuranceResponse> verifyInsurance(InsuranceRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            log.info(">>>> [XAC THUC THE BHYT] Dang goi cong BHYT cho the: {}, Benh nhan: {}", 
                    request.getInsuranceCardNumber(), request.getPatientName());

            if (request.isSimulateError()) {
                throw new RuntimeException("Cổng Bảo Hiểm Y Tế báo lỗi máy chủ (500 Error)");
            }

            if (request.getSimulateDelaySeconds() > 0) {
                try {
                    log.info(">>>> [CONG BHYT PHAN HOI CHAM] Dang gia lap xu ly cham {} giay...", request.getSimulateDelaySeconds());
                    Thread.sleep(request.getSimulateDelaySeconds() * 1000L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Yeu cau bi ngat do timeout");
                }
            }

            double originalPrice = request.getOriginalPrice() != null ? request.getOriginalPrice() : 0.0;
            double discountRate = 0.8; // BHYT chi trả 80%
            double discountAmount = originalPrice * discountRate;
            double finalPrice = originalPrice - discountAmount;

            return InsuranceResponse.builder()
                    .insuranceCardNumber(request.getInsuranceCardNumber())
                    .patientName(request.getPatientName())
                    .medicineName(request.getMedicineName())
                    .originalPrice(originalPrice)
                    .discountRate(discountRate)
                    .discountAmount(discountAmount)
                    .finalPriceToPay(finalPrice)
                    .status("INSURANCE_VERIFIED_SUCCESS")
                    .note("BHYT chi trả 80%. Bệnh nhân cùng chi trả 20%.")
                    .verifiedAt(LocalDateTime.now())
                    .build();
        });
    }

    public CompletableFuture<InsuranceResponse> insuranceFallback(InsuranceRequest request, Throwable throwable) {
        log.warn(">>>> [INSURANCE FALLBACK] Khong the xac thuc the BHYT: {}. Ly do: {}", 
                request.getInsuranceCardNumber(), throwable.getMessage());

        double originalPrice = request.getOriginalPrice() != null ? request.getOriginalPrice() : 0.0;

        InsuranceResponse fallback = InsuranceResponse.builder()
                .insuranceCardNumber(request.getInsuranceCardNumber())
                .patientName(request.getPatientName())
                .medicineName(request.getMedicineName())
                .originalPrice(originalPrice)
                .discountRate(0.0)
                .discountAmount(0.0)
                .finalPriceToPay(originalPrice)
                .status("FALLBACK_ORIGINAL_PRICE")
                .note("Xác thực bảo hiểm sau (Không thể kết nối cổng BHYT hoặc quá thời gian chờ: " + throwable.getClass().getSimpleName() + ")")
                .verifiedAt(LocalDateTime.now())
                .build();

        return CompletableFuture.completedFuture(fallback);
    }
}
