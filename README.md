# Cadence

Website bán giày và quần áo thể thao trực tiếp từ thương hiệu (D2C) cho thị trường Việt Nam: VND, COD, VNPay, địa chỉ theo địa giới hành chính 2025.

Cadence là thương hiệu giả định. Đây là dự án portfolio làm một mình, nhưng được xây như hệ thống chạy thật: đúng domain, dữ liệu nhất quán, có test, có CI/CD, có deploy.

## Trạng thái

Thiết kế xong (bước 1–5). Đang làm **walking skeleton** (bước 6).

## Dự án muốn chứng minh

- **Không bán quá số hàng** khi nhiều người tranh mua cùng lúc: giữ hàng nguyên tử, có test đồng thời.
- **Thanh toán an toàn** với VNPay: IPN có xác thực chữ ký, idempotency, hết hạn, đối soát, xử lý tiền đến muộn.
- **Kiến trúc có kỷ luật:** modular monolith theo DDD + Hexagonal + Clean Architecture, ranh giới module được ép bằng test ArchUnit.
- **Vận hành được:** CI/CD, log có cấu trúc, metrics, deploy trên VPS.

## Công nghệ

| Phần | Công nghệ |
|---|---|
| Backend | Java 25, Spring Boot 4.1, Spring Data JPA, Flyway, Maven |
| Frontend | Next.js 16, TypeScript |
| Dữ liệu | PostgreSQL 18, SeaweedFS (ảnh, API S3) |
| Thanh toán | VNPay sandbox |
| Hạ tầng | Docker Compose, Nginx, GitHub Actions, GHCR, 1 VPS |
| Quan sát | Prometheus, Grafana, Loki |

## Tài liệu

| Tài liệu | Nội dung |
|---|---|
| [prototype/](prototype/) | Prototype giao diện bằng HTML (trang bán hàng và trang quản trị) |

## Chạy

**Prototype** (HTML tĩnh):

```bash
cd prototype && python -m http.server 5500
```

Mở `http://localhost:5500`.

**Ứng dụng:** đang làm, hướng dẫn sẽ có ở nhiệm vụ S9 (Docker Compose).
