# ADR-0015: IAM tự làm bằng Spring Security ở GĐ1, sẵn đường chuyển sang Keycloak

- **Trạng thái:** Đã chấp nhận (2026-11-17)
- **Ngày:** 2026-11-17
- **Liên quan:** CUS-01…CUS-05, N5, ADR-0006 (session cookie), ADR-0013, ADR-0014; [access-control.md](../../03-tactical-design/access-control.md)

## Bối cảnh
Hệ thống cần xác thực (đăng nhập), phân quyền (staff/admin theo hành động, khách chỉ thấy dữ liệu của mình) và quản lý tài khoản. Bước 2 xếp `identity` là **generic subdomain**: làm mỏng, dựa vào thư viện. GĐ1 chỉ có một ứng dụng, 3 vai trò cố định, đăng nhập bằng email + mật khẩu; đăng nhập Google, OTP SĐT là "chưa định" (mục 1.7).

## Các lựa chọn
| Lựa chọn | Ưu | Nhược |
|---|---|---|
| A. **Tự làm bằng Spring Security** (session trong PostgreSQL) | Ít thành phần; khớp ADR-0006; không thêm RAM; đủ cho nhu cầu GĐ1 | Tự lo các chi tiết bảo mật (AC1–AC9); MFA, đăng nhập mạng xã hội phải tự thêm |
| B. Keycloak tự host (OIDC) | IAM chuẩn doanh nghiệp: SSO, MFA, đăng nhập Google, quản lý vai trò có giao diện | Thêm một ứng dụng Java (~0,5–1 GB RAM) + database; luồng OIDC với Next.js render phía server phức tạp; tốn giờ |
| C. Dịch vụ ngoài (Auth0, Clerk, Firebase Auth, Cognito) | Nhanh, có sẵn MFA và mạng xã hội | Phụ thuộc bên ngoài, giới hạn gói free; dữ liệu người dùng nằm ở nơi khác; ít học được |

## Quyết định
Chọn **A** cho GĐ1, thiết kế để đổi sang B mà không chạm các module khác:

- **Port `CurrentUser`** (trong `common`): `userId`, loại người dùng (`GUEST`, `CUSTOMER`, `STAFF`, `ADMIN`), tập quyền. Module nghiệp vụ chỉ biết port này, không import Spring Security hay bảng `accounts` (test ArchUnit).
- **Kiểm quyền, không kiểm vai trò.** Mỗi module định nghĩa quyền của mình (`order.confirm`…); `identity` ánh xạ vai trò → quyền ([access-control.md](../../03-tactical-design/access-control.md) mục 3.10.3).
- **Kiểm ở use case** (tầng application), không chỉ ở controller hay giao diện. Luật sở hữu nằm trong câu truy vấn.
- Mặc định từ chối (AC1); phiên mới khi đăng nhập, huỷ mọi phiên khi đổi mật khẩu/khoá/đổi vai trò (AC2, AC3); argon2id (AC6); `identity.auth_events` để truy vết (AC8).

**Điều kiện chuyển sang Keycloak** (ghi sẵn để không phải tranh luận lại): cần đăng nhập Google/Facebook, cần MFA cho admin mà không muốn tự viết, có ứng dụng thứ hai cần đăng nhập chung (SSO), hoặc tách module thành service cần token chuẩn. Khi đó chỉ thay adapter của `identity` (đăng nhập qua OIDC, đọc quyền từ token) và giữ nguyên `CurrentUser`.

## Hệ quả
- Không thêm container; hợp ngân sách 180 giờ và VPS 4 GB.
- Phải tự kiểm các điểm bảo mật thường bị bỏ sót (AC1–AC9); mỗi điểm cần một test.
- Chưa có MFA cho admin ở GĐ1: rủi ro đã biết, ghi trong README dự án (AC10).
- Keycloak là ứng viên tốt cho lộ trình học ở GĐ2, cùng lúc với đăng nhập Google.
