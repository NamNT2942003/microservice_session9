# Quản trị cấu hình tập trung cho Chuỗi hiệu thuốc (Spring Cloud Config Server & Git)

Repository: [NamNT2942003/microservice_session9](https://github.com/NamNT2942003/microservice_session9)

---

## 1. Mục tiêu
- **Kiến thức**: Tách cấu hình Database và các tham số vận hành hiệu thuốc ra khỏi mã nguồn ứng dụng.
- **Kỹ năng**: Thiết lập Config Server để quản lý cấu hình cho nhiều chi nhánh hiệu thuốc từ một kho Git duy nhất.

---

## 2. Cấu trúc Project

```
c:\MicroService\Session9\
├── config-repo/
│   ├── pharmacy-service.properties       # Cấu hình nhánh chính (Nha Thuoc So 1)
│   └── pharmacy-service-dev.properties   # Cấu hình nhánh thử nghiệm (Nha Thuoc So 2)
├── config-server/                        # Spring Cloud Config Server (Port 8888)
│   ├── pom.xml
│   └── src/main/java/com/example/configserver/
│       ├── ConfigServerApplication.java
│       └── resources/application.properties
├── pharmacy-service/                     # Pharmacy Client Service (Port 8081)
│   ├── pom.xml
│   └── src/main/java/com/example/pharmacyservice/
│       ├── PharmacyServiceApplication.java
│       ├── controller/PharmacyController.java
│       ├── model/Medicine.java
│       ├── repository/MedicineRepository.java
│       ├── runner/StartupRunner.java
│       └── resources/application.properties
├── pharmacy-service.properties           # File cấu hình root
└── .gitignore
```

---

## 3. Các thông số cấu hình tập trung trên Git

File `pharmacy-service.properties`:
```properties
# Database Configuration (H2 In-Memory)
spring.datasource.url=jdbc:h2:mem:pharmacydb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
spring.h2.console.enabled=true

# Pharmacy Operating Parameters
app.branch-name=Nha Thuoc So 1
app.hotline=1900xxxx
```

---

## 4. Hướng dẫn chạy và kiểm thử

### Bước 1: Khởi động Config Server (Port 8888)
```bash
cd config-server
.\mvnw.cmd spring-boot:run
```
Kiểm tra Config Server đọc cấu hình từ Git:
- Mở trình duyệt hoặc curl: `http://localhost:8888/pharmacy-service/default`

### Bước 2: Khởi động Pharmacy Service (Port 8081)
```bash
cd pharmacy-service
.\mvnw.cmd spring-boot:run
```

### Bước 3: Xác minh kết quả
1. **Kiểm tra Log Console**:
   Ứng dụng `pharmacy-service` sẽ in thông tin chi nhánh lấy từ Config Server:
   ```
   =================================================================
   >>>> [PHARMACY SERVICE INITIALIZED SUCCESSFULLY]
   >>>> [BRANCH NAME]   : Nha Thuoc So 1
   >>>> [HOTLINE]       : 1900xxxx
   >>>> [DATASOURCE URL]: jdbc:h2:mem:pharmacydb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
   >>>> [TOTAL MEDICINES LOADED]: 4
   =================================================================
   ```
2. **Kiểm tra REST API & Quản lý thuốc**:
   - `GET http://localhost:8081/api/pharmacy/info` : Trả về thông tin chi nhánh và trạng thái kết nối.
   - `GET http://localhost:8081/api/pharmacy/medicines` : Trả về danh sách thuốc trong kho.

---

## 5. Cập nhật giá thuốc / Thuế VAT "Nóng" không Restart (@RefreshScope)

1. **Xem tỷ lệ thuế hiện tại**:
   ```bash
   GET http://localhost:8081/api/v1/bill/vat-rate
   ```
2. **Tính tiền hóa đơn**:
   ```bash
   POST http://localhost:8081/api/v1/bill
   Content-Type: application/json

   {
     "items": [
       {
         "medicineName": "Panadol Extra",
         "price": 50000.0,
         "quantity": 2
       }
     ]
   }
   ```
   *(Công thức: Tổng tiền = Tiền thuốc + (Tiền thuốc * % VAT))*

3. **Cập nhật cấu hình trên Git**:
   - Đổi `pharmacy.vat-rate=0.05` (5%) trong `pharmacy-service.properties`, sau đó commit & push lên Git.

4. **Kích hoạt Dynamic Refresh**:
   ```bash
   POST http://localhost:8081/actuator/refresh
   Content-Type: application/json
   Body: {}
   ```
   *Mức thuế mới 5% sẽ được nạp lại vào bộ nhớ ngay lập tức mà không cần khởi động lại ứng dụng.*

---

## 7. Phương án dự phòng Fallback khi mất kết nối kho tổng (Bài tập 4)

- **Cơ chế**: Khi kho tổng bị ngắt mạch hoặc lỗi mạng, hàm Fallback tự động truy vấn tồn kho cục bộ trong DB của hiệu thuốc.
- **Kiểm thử**:
  ```bash
  GET http://localhost:8081/api/pharmacy/warehouse/check/Panadol
  ```
  *Phản hồi: Trả về thông báo "Không thể kết nối kho tổng. Hệ thống sẽ sử dụng dữ liệu tồn kho cục bộ để tiếp tục giao dịch" kèm `localStockQuantity`.*

---

## 8. Giới hạn số lượng đơn thuốc và Tự động thử lại (Rate Limiter & Retry)

### 1. Giới hạn tần suất xuất hóa đơn (Rate Limiter - 5 hóa đơn / 10s):
```bash
POST http://localhost:8081/api/v1/invoice/issue
Content-Type: application/json

{
  "customerName": "Nguyen Van A",
  "totalAmount": 150000.0,
  "simulateNetworkError": false
}
```
*Gửi từ 6 request liên tiếp trở lên trong 10 giây sẽ nhận phản hồi `RATE_LIMITED`.*

### 2. Tự động thử lại khi lỗi mạng (Retry - 3 lần, cách nhau 2s):
```bash
POST http://localhost:8081/api/v1/invoice/issue
Content-Type: application/json

{
  "customerName": "Nguyen Van B",
  "totalAmount": 200000.0,
  "simulateNetworkError": true
}
```
*Hệ thống tự thử lại 3 lần rồi chuyển sang lưu tạm hóa đơn offline `SAVED_LOCALLY_PENDING_SYNC`.*

---

## 9. Hệ thống bán thuốc tự phục hồi toàn diện (Full Resilience - BHYT)

- **TimeLimiter (3s)**: Tự ngắt khi cổng BHYT phản hồi chậm > 3s.
- **Retry (3 lần, 2s)**: Tự động gửi lại nếu gặp sự cố kết nối.
- **Circuit Breaker (Ngưỡng 60%)**: Ngắt mạch nếu tỷ lệ lỗi liên tục vượt quá 60%.
- **Fallback**: Trả về giá gốc chưa chiết khấu + ghi chú "Xác thực bảo hiểm sau".

### Kiểm thử:
```bash
POST http://localhost:8081/api/v1/insurance/verify
Content-Type: application/json

{
  "insuranceCardNumber": "DN4010123456789",
  "patientName": "Nguyen Van A",
  "medicineName": "Panadol Extra",
  "originalPrice": 100000.0,
  "simulateDelaySeconds": 5,
  "simulateError": false
}
```
*Khi `simulateDelaySeconds = 5` (> 3s), hệ thống tự ngắt sau 3s và trả về Fallback.*
