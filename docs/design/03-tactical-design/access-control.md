# 3.10 Phân quyền (access control)

> **Trạng thái:** Đã chốt (2026-11-17; bổ sung sau khi bước 3 đã chốt) · Thuộc [Bước 3: Thiết kế chiến thuật](README.md) · Quyết định kiến trúc: [ADR-0015](../04-architecture/adr/0015-iam-in-house-spring-security.md)

Chi tiết hoá CUS-05 ("phân quyền staff/admin theo hành động") và các luật "chỉ xem được dữ liệu của mình" cho khách.

## 3.10.1 Hai loại kiểm tra

| Loại | Câu hỏi | Ví dụ | Kiểm ở đâu |
|---|---|---|---|
| **Theo quyền** (permission) | Người này có được làm hành động X không? | Staff xác nhận đơn COD | Use case của module, trước khi chạm domain |
| **Theo quyền sở hữu** (ownership) | Dữ liệu này có phải của người này không? | Khách xem đơn của chính mình | Use case/query: điều kiện nằm **trong câu truy vấn** (`WHERE account_id = :me`), không lọc sau khi đã đọc |

Loại thứ hai hay bị quên. Quên thì chỉ cần đổi id trên URL là xem được đơn người khác. Ở trang khách, đây là lỗi bảo mật phổ biến nhất.

## 3.10.2 Danh sách quyền

Tên quyền dạng `module.hành_động`. **Mỗi module tự định nghĩa quyền của mình**; `identity` chỉ giữ bảng ánh xạ vai trò → quyền. Code kiểm **quyền**, không bao giờ kiểm **vai trò** (`if role == ADMIN` bị cấm, có test ArchUnit). Thêm vai trò mới chỉ là sửa bảng ánh xạ.

| Quyền | Cho phép | Use case |
|---|---|---|
| `order.view` | Xem danh sách, chi tiết, timeline mọi đơn | ORD-05, ORD-06 |
| `order.confirm` | Xác nhận đơn COD | ORD-02 |
| `order.cancel` | Huỷ đơn bất kỳ (kèm lý do) | ORD-04 |
| `fulfillment.manage` | Tạo kiện, cập nhật trạng thái kiện, nhập mã vận đơn | FUL-01, FUL-02 |
| `inventory.view` | Xem tồn và lịch sử biến động | INV-05 |
| `inventory.receive` | Nhập kho | INV-01 |
| `inventory.adjust` | Điều chỉnh tồn (bắt buộc lý do) | INV-01 |
| `payment.refund.view` | Xem hàng đợi hoàn tiền | PAY-07, PAY-09 |
| `payment.refund.record` | Ghi nhận hoàn tiền thủ công | PAY-09 |
| `payment.refund.gateway` | Hoàn tiền qua API VNPay | PAY-08 |
| `catalog.edit` | Tạo/sửa sản phẩm, colorway, variant, ảnh, category; publish/archive/hẹn giờ | CAT-05, CAT-06, CAT-08 |
| `catalog.price` | Đặt giá niêm yết và giá sale | CAT-07 |
| `identity.staff.manage` | Tạo tài khoản staff, đổi vai trò, khoá/mở khoá | CUS-05 |

## 3.10.3 Ma trận vai trò × quyền

| Quyền | STAFF | ADMIN |
|---|:---:|:---:|
| `order.view`, `order.confirm`, `order.cancel` | ✅ | ✅ |
| `fulfillment.manage` | ✅ | ✅ |
| `inventory.view`, `inventory.receive`, `inventory.adjust` | ✅ | ✅ |
| `payment.refund.view`, `payment.refund.record`, `payment.refund.gateway` | ✅ | ✅ |
| `catalog.edit` | — | ✅ |
| `catalog.price` | — | ✅ |
| `identity.staff.manage` | — | ✅ |

CUSTOMER không có quyền nào trong bảng này; mọi thứ khách làm được đi qua luật sở hữu (3.10.4).

Ghi chú:
- Prototype cho staff sửa nội dung sản phẩm (trừ giá). GĐ1 theo scope: chỉ admin có `catalog.edit`. Muốn giống prototype thì chỉ cần thêm `catalog.edit` cho STAFF, không sửa code.
- Hoàn tiền qua API (`payment.refund.gateway`) chuyển tiền thật khi chạy thật. Theo scope, staff được làm (PAY-08); ở sandbox không có tiền thật. Nếu chạy thật, nên chuyển quyền này cho ADMIN hoặc yêu cầu người thứ hai duyệt (để GĐ2).

## 3.10.4 Luật sở hữu cho khách

| Hành động | Guest | Customer | Cách kiểm |
|---|---|---|---|
| Xem sản phẩm | ✅ | ✅ | Công khai |
| Giỏ hàng | Giỏ có mã trong cookie của mình | Giỏ gắn `account_id` của mình | ADR-0004; không có API nào nhận `cartId` từ client |
| Đặt hàng | ✅ | ✅ | Rate limit (N5) |
| Xem đơn | Sau khi tra cứu đúng mã đơn + email/SĐT | Đơn có `account_id` = mình | Điều kiện nằm trong câu truy vấn |
| Huỷ đơn | Sau khi tra cứu (xem dưới) | Đơn của mình | Thêm điều kiện O6 (chưa xử lý) |
| Sổ địa chỉ | — | Địa chỉ của mình | `WHERE account_id = :me` |

**Quyền tạm cho guest:** tra cứu đúng (mã đơn + email/SĐT) thì server cấp một cookie ký, HttpOnly, chỉ áp dụng cho **đúng đơn đó**, hạn 30 phút. Guest xem và huỷ đơn bằng cookie này, không phải nhập lại thông tin cho mỗi thao tác. Mã đơn một mình không bao giờ đủ để xem đơn.

**Không tìm thấy và không có quyền trả cùng một kết quả** (404) với dữ liệu thuộc sở hữu người khác. Trả 403 là để lộ "đơn này có tồn tại".

## 3.10.5 Luật cho tài khoản và phiên

| # | Luật | Vì sao |
|---|---|---|
| AC1 | Mặc định là **từ chối**: endpoint nào không khai báo quyền hoặc luật sở hữu thì không ai gọi được (có test quét mọi endpoint) | Quên khai báo không biến thành lỗ hổng |
| AC2 | Đăng nhập thành công thì **tạo phiên mới** (chống session fixation) | ADR-0006 |
| AC3 | Đổi mật khẩu, khoá tài khoản, đổi vai trò → **huỷ mọi phiên** của tài khoản đó | Thu hồi quyền ngay (lý do chọn session thay vì JWT) |
| AC4 | Phiên khách: trượt 30 ngày. Phiên staff/admin: tối đa 8 giờ, không có "ghi nhớ đăng nhập" | Tài khoản có quyền cao thì phiên ngắn |
| AC5 | Đăng nhập sai: thông báo chung ("email hoặc mật khẩu không đúng"); rate limit theo IP và theo email, chậm dần, **không khoá vĩnh viễn** | Không lộ email nào đã đăng ký; khoá cứng thì kẻ xấu khoá được tài khoản người khác |
| AC6 | Mật khẩu: tối thiểu 8 ký tự, chặn mật khẩu quá phổ biến, không bắt buộc ký tự đặc biệt; hash bằng argon2id | Theo khuyến nghị hiện đại (NIST): dài và không phổ biến quan trọng hơn quy tắc phức tạp |
| AC7 | Không ai tự khoá hoặc tự hạ quyền chính mình; luôn còn ít nhất một ADMIN đang hoạt động | Tránh mất quyền quản trị |
| AC8 | Mọi sự kiện quan trọng về tài khoản (đăng nhập thành công/thất bại, đổi mật khẩu, đổi vai trò, khoá) ghi vào `identity.auth_events` | Truy vết; thao tác nghiệp vụ đã có `actor` ở `order_activities`, `stock_movements` |
| AC9 | Staff và admin không tự đăng ký; chỉ admin tạo (`identity.staff.manage`) | CUS-05 |
| AC10 | MFA (TOTP) cho staff/admin: **GĐ2** | Ngoài ngân sách 180 giờ; ghi rõ là rủi ro đã biết |
