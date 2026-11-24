# ADR-0001: Modular monolith theo DDD + Hexagonal, không dùng microservices

- **Trạng thái:** Chấp nhận
- **Ngày:** 2026-11-24
- **Liên quan:** SRS C-07, G2, G3, G4; kiến trúc mục 2, 7, 12.4

## Bối cảnh

Hệ thống có 6 vùng nghiệp vụ: danh mục, tồn kho, đơn hàng (gồm giỏ, tính tiền, voucher), thanh toán, tài khoản, thông báo (cách chia ở mục 7.2–7.3 của tài liệu kiến trúc). Một người phát triển, ngân sách hạ tầng gần bằng 0, lưu lượng nhỏ. Yêu cầu khó nhất là **tính nhất quán**: giữ hàng, ghi lượt dùng voucher, tạo đơn và ghi email vào outbox phải cùng thành công hoặc cùng thất bại.

Quyết định này khó đảo ngược vì nó định hình toàn bộ cấu trúc code, cách triển khai và cách các phần nói chuyện với nhau.

## Các phương án

| Phương án | Ưu | Nhược |
|---|---|---|
| **Microservices** (mỗi vùng một service, một DB) | Triển khai và mở rộng độc lập | Đặt hàng trải qua nhiều service → cần SAGA và bù trừ, message broker, nhiều container. Không có nhu cầu mở rộng độc lập. Một người không vận hành nổi |
| **Monolith phân tầng truyền thống** (controller / service / repository chung cho cả app) | Nhanh lúc đầu | Luật nghiệp vụ rải trong service; các vùng dính chùm; khó test nghiệp vụ mà không dựng Spring |
| **Modular monolith, mỗi module theo DDD + Hexagonal + Clean** | Một transaction cho luồng đặt hàng; một artifact; ranh giới module rõ; nghiệp vụ test được bằng Java thuần | Không mở rộng từng phần riêng được; nhiều file hơn cho mỗi use case; cần kỷ luật giữ ranh giới |

## Quyết định

Chọn **modular monolith**. Mỗi module là một bounded context, chia ba tầng `domain / application / infrastructure` theo DDD + Hexagonal + Clean Architecture. Module giao tiếp với nhau qua use case công khai (port + adapter) hoặc domain event. Ranh giới được kiểm bằng ArchUnit trong CI.

Để không nặng nề, áp dụng hai mức như template: module có nghiệp vụ dùng đủ ba tầng; phần chỉ là CRUD (sổ địa chỉ trong `account`) và module không có luật nghiệp vụ (`notification`) được làm mỏng. Không tách module chỉ vì "nghe giống bounded context": module nào không có luật, dữ liệu và lý do thay đổi riêng thì gộp vào module gần nhất.

## Hệ quả

- **Tốt:** luồng đặt hàng dùng một transaction ACID; không cần SAGA hay 2PC.
- **Tốt:** luật nghiệp vụ nằm trong domain thuần Java, unit test chạy trong vài mili giây.
- **Tốt:** nếu sau này cần tách (ví dụ `payment`), đường cắt đã có: port, adapter, schema riêng.
- **Xấu:** cả hệ thống chung một tiến trình; một module lỗi bộ nhớ ảnh hưởng toàn bộ.
- **Xấu:** nhiều lớp (command, view, port, adapter, mapper) cho mỗi use case. Bù lại bằng việc làm mỏng module CRUD.

## Khi nào xem xét lại

Khi một phần cần mở rộng hoặc triển khai với nhịp độ khác hẳn phần còn lại, hoặc khi có nhiều nhóm cùng phát triển.
