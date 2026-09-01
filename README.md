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
2. **Kiểm tra REST API**:
   - `GET http://localhost:8081/api/pharmacy/info` : Trả về thông tin chi nhánh và trạng thái kết nối.
   - `GET http://localhost:8081/api/pharmacy/medicines` : Trả về danh sách thuốc trong kho.
