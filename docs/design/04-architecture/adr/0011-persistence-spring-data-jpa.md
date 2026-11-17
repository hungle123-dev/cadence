# ADR-0011: Lưu trữ bằng Spring Data JPA, tách khỏi domain model

- **Trạng thái:** Đã chấp nhận (2026-11-17)
- **Ngày:** 2026-11-17
- **Liên quan:** ADR-0001, ADR-0003, ADR-0005, ADR-0009; template mục 4.1 và 5 (domain không phụ thuộc framework)

## Bối cảnh
Cần chọn cách code Java nói chuyện với PostgreSQL. Template yêu cầu tầng `domain` không phụ thuộc framework. Một vài chỗ cần câu SQL đặc biệt (giữ hàng có điều kiện, lấy outbox bằng `SKIP LOCKED`, truy vấn lọc PLP).

## Các lựa chọn
| Lựa chọn | Ưu | Nhược |
|---|---|---|
| A. JDBC / SQL viết tay (JdbcClient, jOOQ) | Kiểm soát hoàn toàn câu SQL | Nhiều code ánh xạ; ít phổ biến trong tuyển dụng ở VN |
| B. JPA, **gắn `@Entity` thẳng lên class domain** | Ít code nhất | Domain dính JPA: cần constructor rỗng, lazy proxy, quan hệ hai chiều; vi phạm template |
| C. **JPA, entity JPA riêng ở tầng infrastructure**, ánh xạ sang domain | Domain sạch, vẫn dùng được sức mạnh của JPA; phổ biến | Phải viết mapper (domain ↔ entity) |

## Quyết định
Chọn **C**.
- Mỗi module có package `infrastructure/persistence` chứa entity JPA, repository Spring Data và **adapter** hiện thực port repository của domain. Domain không import `jakarta.persistence`. Test ArchUnit kiểm tra điều này.
- **Một aggregate = một repository**, lưu và nạp cả aggregate (root + entity con) cùng lúc. Không có repository cho entity con.
- Tham chiếu sang aggregate khác **bằng id**, không dùng `@ManyToOne` tới entity của module khác.
- `@Version` trên entity root hiện thực optimistic lock (bước 3, nguyên tắc 5).
- Schema do **Flyway** quản lý; Hibernate để `ddl-auto=validate` (chỉ kiểm tra, không tự sửa bảng).
- Tắt `open-in-view`; dùng fetch join hoặc entity graph khi cần, tránh N+1.
- Câu lệnh đặc biệt dùng `@Modifying @Query` (native) trong adapter: giữ hàng có điều kiện (ADR-0003), lấy outbox `FOR UPDATE SKIP LOCKED` (ADR-0005).
- Phía đọc phức tạp (lọc PLP có facet, danh sách đơn cho staff) được phép dùng projection hoặc SQL native trả thẳng DTO, không đi qua aggregate.

## Hệ quả
- Thêm code mapper; có thể dùng MapStruct để giảm việc viết tay.
- Phải hiểu vòng đời entity của Hibernate (managed, detached, dirty checking) để tránh lỗi ngầm. Test tích hợp với Testcontainers bắt các lỗi này.
- "SQL thuần" chỉ còn ở vài chỗ cần nguyên tử hoặc hiệu năng, và đều nằm trong adapter.
