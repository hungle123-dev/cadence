# ADR-0014: Một DB user cho ứng dụng; ranh giới schema được ép bằng test

- **Trạng thái:** Đã chấp nhận (2026-11-17)
- **Ngày:** 2026-11-17
- **Thay thế:** phần "user DB của mỗi module chỉ có quyền trên schema của mình" trong [ADR-0001](0001-modular-monolith.md). Phần còn lại của ADR-0001 giữ nguyên.
- **Liên quan:** ADR-0001, ADR-0002, ADR-0011; bước 5 mục 5.2, 5.6

## Bối cảnh
ADR-0001 muốn mỗi module dùng một DB user riêng để đọc chéo bảng bị chặn ngay ở database. ADR-0002 yêu cầu đặt hàng chạy **một transaction** đi qua `inventory`, `ordering`, `payment`.

Một transaction chỉ chạy trên một kết nối, và một kết nối chỉ đăng nhập bằng một user. Ba module ba user thì không thể chung một transaction. Hai ADR đã chốt mâu thuẫn nhau; phát hiện khi thiết kế database.

## Các lựa chọn
| Lựa chọn | Ưu | Nhược |
|---|---|---|
| A. Giữ nhiều user, bỏ ADR-0002 (chuyển sang saga) | Ranh giới DB cứng | Mất tính đơn giản và đúng tuyệt đối của đặt hàng; phức tạp hơn nhiều |
| B. Một kết nối, đổi vai trò trong transaction (`SET LOCAL ROLE inventory_role` trước khi gọi module) | Vẫn có quyền DB theo module | Phải chèn lệnh đổi vai trò vào mọi lời gọi giữa module; dễ quên, khó debug, ít người dùng |
| C. **Một DB user cho ứng dụng; ép ranh giới schema bằng test** | Giữ được ADR-0002; đơn giản | Ranh giới không được DB chặn, chỉ được test chặn |

## Quyết định
Chọn **C**.
- Ứng dụng dùng một user (`cadence_app`), có quyền trên mọi schema nghiệp vụ, `messaging`, và chỉ quyền đọc trên `reference`. User này **không** có quyền tạo/xoá bảng; migration chạy bằng user riêng (`cadence_migrator`).
- Ranh giới được ép bằng test chạy trong CI:
  - **ArchUnit:** entity JPA của module X chỉ khai báo `@Table(schema = "x")`; repository và adapter của X chỉ dùng entity của X.
  - **Test quét câu SQL native** (`@Query(nativeQuery = true)`, file `.sql`): chỉ được nhắc tới schema của chính module, `messaging`, `reference`.
  - **Test migration:** file trong `db/migration/x` chỉ tạo/sửa đối tượng trong schema `x`.
- Không có khoá ngoại giữa các schema (5.1), nên dù có ai lách test thì cũng không tạo được ràng buộc chéo âm thầm.

## Hệ quả
- Ranh giới phụ thuộc vào bộ test kiến trúc; bộ test này phải viết ở walking skeleton, trước code nghiệp vụ (như ADR-0001 đã yêu cầu).
- Khi tách một module ra service riêng, module đó có database riêng và user riêng như thường; quyết định này chỉ áp dụng cho giai đoạn monolith.
- Bài học: viết thiết kế database làm lộ ra mâu thuẫn mà hai ADR đọc riêng thì không thấy. Đây đúng là lý do ADR được phép bị thay thế bằng ADR mới.
