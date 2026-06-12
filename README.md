# WEB-CỨU-HỘ — Hệ thống tiếp nhận & điều phối cứu hộ khẩn cấp

Nền tảng quản lý và điều phối các cuộc gọi cấp cứu, hỗ trợ người dân trong tình huống khẩn cấp tại Việt Nam. Tích hợp bản đồ tương tác, trò chuyện AI, xác thực OAuth2.

## Công nghệ

- **Frontend:** Next.js 15, React, Tailwind CSS, Leaflet
- **Backend:** Java 25, Spring Boot 3, JWT, OAuth2
- **Database:** MySQL 8+
- **AI:** OpenRouter (free model), tool calling truy vấn database

## Yêu cầu hệ thống

- **Java 25** (kiểm tra trong `pom.xml` nếu khác)
- **MySQL 8+** đang chạy local port 3306
- **Node.js 20+**
- **Maven** (hoặc dùng `mvnw`)

## Hướng dẫn chạy dự án

### 1. Clone

```bash
git clone https://github.com/JackMarines/WEB-CUU-HO.git
cd WEB-CUU-HO
```

### 2. Cấu hình backend

Tạo file `emergency-response-backend/src/main/resources/application.properties` với nội dung tối thiểu sau:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/emergency_response?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=NHAP_MAT_KHAU_MYSQL_CUA_BAN
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.jpa.open-in-view=false
spring.sql.init.mode=always
spring.jpa.defer-datasource-initialization=true
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
app.jwt.secret=day-la-chuoi-bi-mat-it-nhat-32-ky-tu
app.jwt.expiration=86400000
app.upload.dir=uploads
app.oauth.redirect-uri=http://localhost:3000/auth/callback
```

> **Lưu ý:** File này đã được thêm vào `.gitignore` — sẽ không bao giờ bị đẩy lên GitHub. Password và secret hãy tự thay bằng giá trị của bạn.

### 3. Chạy backend

```bash
cd emergency-response-backend
mvn compile
mvn spring-boot:run
```

Backend chạy tại **http://localhost:8080**. Tự động tạo database, bảng, và dữ liệu mẫu.

### 4. Chạy frontend (terminal riêng)

```bash
cd emergency-response-frontend
npm install
npm run dev
```

Frontend chạy tại **http://localhost:3000**.

### Tài khoản mặc định

| Email | Password | Vai trò |
|---|---|---|
| `admin@emergency.vn` | `admin123` | admin |
| `superadmin@emergency.vn` | `admin123` | superadmin |

> Có thể đăng nhập bằng email/password hoặc OAuth2 (Google/GitHub) — OAuth2 yêu cầu email phải có trong hệ thống từ trước.

## Tính năng chính

- **Bản đồ tương tác:** Hiển thị cuộc gọi khẩn cấp, trung tâm cứu hộ theo thời gian thực
- **AI Chatbot:** Trả lời tự động dựa trên dữ liệu database (công cụ, trung tâm, loại thảm họa)
- **Xác thực:** JWT + OAuth2 (Google, GitHub) + phân quyền admin/superadmin
- **Quản trị:** Dashboard, CRUD người dùng/cuộc gọi/trung tâm/loại thảm họa
- **Responsive:** Tối ưu mobile và desktop
