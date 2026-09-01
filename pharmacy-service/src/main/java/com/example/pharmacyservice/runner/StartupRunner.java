package com.example.pharmacyservice.runner;

import com.example.pharmacyservice.model.Medicine;
import com.example.pharmacyservice.repository.MedicineRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class StartupRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupRunner.class);

    @Value("${app.branch-name:Chưa cấu hình}")
    private String branchName;

    @Value("${app.hotline:Chưa cấu hình}")
    private String hotline;

    @Value("${spring.datasource.url:Chưa cấu hình}")
    private String datasourceUrl;

    private final MedicineRepository medicineRepository;

    public StartupRunner(MedicineRepository medicineRepository) {
        this.medicineRepository = medicineRepository;
    }

    @Override
    public void run(String... args) {
        // Khởi tạo một số dữ liệu mẫu vào Database H2
        medicineRepository.saveAll(Arrays.asList(
                Medicine.builder().name("Paracetamol 500mg").price(25000.0).quantity(100).build(),
                Medicine.builder().name("Panadol Extra").price(45000.0).quantity(50).build(),
                Medicine.builder().name("Vitamin C 1000mg").price(60000.0).quantity(80).build(),
                Medicine.builder().name("Berberin").price(15000.0).quantity(200).build()
        ));

        // In ra thông tin chi nhánh lấy từ Config Server / Git
        log.info("=================================================================");
        log.info(">>>> [PHARMACY SERVICE INITIALIZED SUCCESSFULLY]");
        log.info(">>>> [BRANCH NAME]   : {}", branchName);
        log.info(">>>> [HOTLINE]       : {}", hotline);
        log.info(">>>> [DATASOURCE URL]: {}", datasourceUrl);
        log.info(">>>> [TOTAL MEDICINES LOADED]: {}", medicineRepository.count());
        log.info("=================================================================");
    }
}
