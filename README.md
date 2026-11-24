# Cadence

Website bán giày và đồ thể thao: storefront cho khách và trang quản trị cho nhân viên. Dự án cá nhân, tập trung làm đúng những luồng khó của thương mại điện tử: giữ hàng khi nhiều người cùng mua, thanh toán VNPay với IPN, vòng đời đơn hàng ba trục.

> Dự án đang ở giai đoạn dựng nền móng: khung backend, frontend, hạ tầng và CI/CD.

## Công nghệ

| Phần | Công nghệ |
|---|---|
| Backend | Java 25, Spring Boot 4.2, modular monolith theo DDD + Hexagonal + Clean Architecture |
| Frontend | Next.js 16 (App Router, Cache Components), TypeScript |
| Dữ liệu | PostgreSQL 17, Flyway |
| Hạ tầng | Docker Compose, Nginx, GitHub Actions |
| Test | JUnit 5, Testcontainers, ArchUnit |

## Chạy ở máy local

Cần: Docker, JDK 25 trở lên, Node.js theo phiên bản trong [`.nvmrc`](.nvmrc) (hiện là 22). Dùng nvm hoặc fnm thì gõ `nvm use` / `fnm use` ở thư mục gốc; trên Windows, `nvm-windows` không đọc file này nên gõ thẳng phiên bản (`nvm use 22`).

```
cp .env.example .env
docker compose up --build
```

Mở `http://localhost:8080`. Email do hệ thống gửi xem ở `http://localhost:8025`. Nếu máy đã có chương trình khác chiếm cổng (ví dụ PostgreSQL cài sẵn ở 5432), đổi `HTTP_PORT`, `DB_PORT`… trong `.env`; chạy backend trực tiếp thì đặt thêm `SERVER_PORT`.

Chạy từng phần khi đang phát triển:

```
docker compose up db mail                        # chỉ DB và hộp thư
cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
cd frontend && npm install && npm run dev
```

Swagger UI (profile `dev`): `http://localhost:8080/api/swagger-ui.html` khi chạy backend trực tiếp.

## Test

```
cd backend && ./mvnw verify          # unit, tích hợp với PostgreSQL thật, luật kiến trúc
cd frontend && npm run lint && npm run typecheck && npm run build
```

## Tài liệu

| Tài liệu | Nội dung |
|---|---|
| [`docs/01-srs.md`](docs/01-srs.md) | Đặc tả yêu cầu |
| [`docs/02-architecture.md`](docs/02-architecture.md) | Kiến trúc, bảng áp dụng kiến thức |
| [`docs/adr/`](docs/adr/README.md) | Các quyết định khó đảo ngược |
| [`docs/03-database.md`](docs/03-database.md) | Thiết kế cơ sở dữ liệu |
| [`docs/04-test-plan.md`](docs/04-test-plan.md) | Kế hoạch kiểm thử |
| [`deploy/README.md`](deploy/README.md) | Triển khai lên VPS |
