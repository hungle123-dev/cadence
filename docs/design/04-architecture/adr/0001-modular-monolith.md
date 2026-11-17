# ADR-0001: Modular monolith, một module mỗi bounded context

- **Trạng thái:** Đã chấp nhận (2026-11-17); phần "mỗi module một DB user" bị thay thế bởi [ADR-0014](0014-single-db-user-schema-boundary-by-tests.md)
- **Ngày:** 2026-11-17
- **Liên quan:** G4, N7, bước 2 (8 BC), template mục 3 và 5

## Bối cảnh
Bước 2 chia hệ thống thành 8 bounded context. Một người làm, ~180 giờ, deploy trên một máy nhỏ. Mục tiêu G4 yêu cầu ranh giới module **được ép bằng test**, không chỉ là quy ước.

## Các lựa chọn
| Lựa chọn | Ưu | Nhược |
|---|---|---|
| A. Microservices (mỗi BC một service) | Ranh giới cứng, scale riêng | Mạng, transaction phân tán, 8 pipeline, tốn RAM; quá sức một người |
| B. Monolith không chia module | Nhanh lúc đầu | Ranh giới mờ dần, không chứng minh được G4 |
| C. **Modular monolith**: một ứng dụng, mỗi BC một module, ép luật bằng ArchUnit | Ranh giới rõ, một lần deploy, transaction cục bộ khi cần (ADR-0002) | Phải kỷ luật; ranh giới chỉ mạnh bằng bộ test kiến trúc |
| D. Như C nhưng dùng Spring Modulith | Có sẵn kiểm tra module | Thêm framework phải học; template của bạn đã có luật ArchUnit riêng |

## Quyết định
Chọn **C**. Một ứng dụng Spring Boot; mỗi BC một package gốc (`order`, `inventory`, `payment`, `catalog`, `cart`, `fulfillment`, `identity`, `notification`) cộng `common` làm shared kernel, cấu trúc bên trong theo template (`domain`, `application`, `infrastructure`).

- Luật R1–R10 của template viết thành test ArchUnit, chạy trong CI. Thêm luật: module A chỉ gọi module B qua **port của A** + ACL adapter, không import `domain` của B.
- Một database PostgreSQL, **mỗi module một schema** (đoạn dưới về DB user riêng đã được thay bằng ADR-0014) (`order`, `inventory`…). User DB của mỗi module chỉ có quyền trên schema của mình, nên đọc chéo bảng bị chặn cả ở tầng DB.

## Hệ quả
- Tách một module thành service riêng sau này chỉ cần thay ACL adapter (gọi HTTP) và outbox (gửi qua broker).
- Ngoại lệ có chủ đích: ADR-0002 cho phép một transaction đi qua 3 module khi đặt hàng.
- Phải viết bộ test ArchUnit ngay ở walking skeleton, trước khi có code nghiệp vụ.
