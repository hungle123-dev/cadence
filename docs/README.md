# Tài liệu dự án Cadence

Website bán hàng trực tiếp (D2C) giày và quần áo thể thao, thị trường Việt Nam. Backend Java theo DDD + Hexagonal + Clean Architecture ([template](../../LearningSE/DDD-HEXAGONAL-CLEAN-TEMPLATE.md)), frontend Next.js.

## Tiến độ

| Bước | Tài liệu | Trạng thái | Cập nhật |
|---|---|---|---|
| 1 | [Phạm vi](design/01-scope.md) | ✅ Đã chốt | 2026-11-16 |
| 2 | [Thiết kế chiến lược](design/02-strategic-design.md): Event Storming, context map, BC canvas | ✅ Đã chốt | 2026-11-17 |
| 3 | [Thiết kế chiến thuật](design/03-tactical-design/README.md): aggregate, bất biến, state machine, use case | ✅ Đã chốt | 2026-11-17 |
| 4 | [Kiến trúc và ADR](design/04-architecture/README.md): sơ đồ C4, 17 ADR, lộ trình DevOps | ✅ Đã chốt | 2026-11-17 |
| 5 | [Thiết kế database](design/05-database/README.md): schema theo module, bảng + cột + lý do, index, ràng buộc | ✅ Đã chốt | 2026-11-17 |
| 6 | [Walking skeleton](design/06-walking-skeleton.md): 13 nhiệm vụ S1–S13, bạn tự làm, Claude review | 🔵 Đang làm | 2026-11-17 |

Trạng thái: ⚪ Chưa làm → 🔵 Đang viết → 🟡 Đang review → ✅ Đã chốt. Tài liệu đã chốt vẫn sửa được, nhưng mỗi lần sửa ghi vào [Nhật ký thay đổi](#nhật-ký-thay-đổi).

**Việc tiếp theo:** S10 (CI bằng GitHub Actions) trong [bước 6](design/06-walking-skeleton.md) mục 6.4. Làm xong nhờ Claude review.

## Cấu trúc thư mục

```
docs/
├─ README.md                      ← trang này: mục lục, tiến độ, nhật ký thay đổi
├─ ecommerce-domain.md            ← nghiên cứu domain (kiến thức chung, không phải thiết kế dự án)
└─ design/
   ├─ 01-scope.md
   ├─ 02-strategic-design.md
   ├─ 03-tactical-design/
   │   ├─ README.md               ← nguyên tắc chung, điều chỉnh, trạng thái điểm nóng
   │   ├─ order.md · inventory.md · payment.md   ← 3 BC core
   │   ├─ events.md               ← hợp đồng sự kiện giữa các BC
   │   └─ supporting-contexts.md  ← 5 BC còn lại (mức CRUD)
   ├─ 04-architecture/
   │   ├─ README.md               ← sơ đồ C4, danh sách ADR
   │   └─ adr/                    ← mỗi quyết định một file
   ├─ 05-database/
   │   ├─ README.md               ← quy ước, schema, bảng dùng chung, migration
   │   ├─ ordering.md · inventory.md · payment.md   ← 3 module core
   │   └─ supporting.md           ← catalog, cart, fulfillment, identity, notification
   └─ 06-walking-skeleton.md
```

## Quy ước

- **Số mục giữ nguyên khi tách file** (1.5, 2.6, 3.2.2…), nên các tham chiếu kiểu "mục 3.3" vẫn đúng. Bảng dưới cho biết số mục nằm ở file nào.
- **Mã dùng xuyên suốt** (từ tài liệu tới code và test):

| Mã | Ý nghĩa | Định nghĩa ở |
|---|---|---|
| `CAT-`, `INV-`, `CART-`, `CHK-`, `PAY-`, `ORD-`, `FUL-`, `CUS-`, `NOT-` | Use case | [01-scope.md](design/01-scope.md) mục 1.5 |
| `G1`–`G7` | Mục tiêu dự án | 01-scope.md mục 1.3 |
| `N1`–`N10` | Yêu cầu phi chức năng | 01-scope.md mục 1.6 |
| `Q1`–`Q4` | Quyết định phạm vi | 01-scope.md mục 1.10 |
| `P1`–`P13` (bước 2) | Policy "khi … thì …" | [02-strategic-design.md](design/02-strategic-design.md) mục 2.2 |
| `H1`–`H8` | Điểm nóng cần quyết | 02-strategic-design.md mục 2.7; trạng thái ở [03 README](design/03-tactical-design/README.md) mục 3.9 |
| `O1`–`O8`, `I1`–`I8`, `P1`–`P9` (bước 3) | Bất biến của order, inventory, payment | [order.md](design/03-tactical-design/order.md), [inventory.md](design/03-tactical-design/inventory.md), [payment.md](design/03-tactical-design/payment.md) |
| `A1`–`A3` | Bổ sung context map | 03 README mục 3.7 |
| `ADR-NNNN` | Quyết định kiến trúc | [04-architecture/adr/](design/04-architecture/adr/) |
| `D1`–`D10` | Việc trong lộ trình DevOps | [04-architecture/README.md](design/04-architecture/README.md) mục 4.6 |
| `module.hành_động` (ví dụ `order.confirm`) | Quyền | [access-control.md](design/03-tactical-design/access-control.md) mục 3.10.2 |
| `AC1`–`AC10` | Luật tài khoản và phiên | access-control.md mục 3.10.5 |

Lưu ý: `P1…` ở bước 2 là policy, `P1…` ở bước 3 là bất biến payment. Khi tham chiếu chéo, ghi rõ "policy P8" hoặc "bất biến P2".

| Số mục | File |
|---|---|
| 1.1 – 1.11 | design/01-scope.md |
| 2.1 – 2.7 | design/02-strategic-design.md |
| 3.1, 3.7, 3.9 | design/03-tactical-design/README.md |
| 3.2 | design/03-tactical-design/order.md |
| 3.3 | design/03-tactical-design/inventory.md |
| 3.4 | design/03-tactical-design/payment.md |
| 3.5 (use case chi tiết) | nằm trong file của BC tương ứng |
| 3.6 | design/03-tactical-design/events.md |
| 3.8 | design/03-tactical-design/supporting-contexts.md |
| 3.10 | design/03-tactical-design/access-control.md |
| 4.1 – 4.6 | design/04-architecture/README.md |
| 5.1 – 5.6 | design/05-database/README.md |
| 5.7 | design/05-database/ordering.md |
| 5.8 | design/05-database/inventory.md |
| 5.9 | design/05-database/payment.md |
| 5.10 | design/05-database/supporting.md |

## Nhật ký thay đổi

| Ngày | Tài liệu | Thay đổi |
|---|---|---|
| 2026-11-16 | 01-scope | Viết và chốt phạm vi giai đoạn 1 (45 use case, ~180 giờ) |
| 2026-11-16 | 01-scope | Q1: thương hiệu Cadence; Q2: ảnh; thêm mục 1.11 (prototype là tầm nhìn) |
| 2026-11-17 | 01-scope | Q2: về sau tự tạo và tự host ảnh |
| 2026-11-17 | 02-strategic-design | Viết và chốt: 8 bounded context, context map, canvas |
| 2026-11-17 | 03-tactical-design | Viết bản nháp; bổ sung context map A1–A3 |
| 2026-11-17 | (toàn bộ) | Tách `project-design.md` thành các file theo bước, thêm trang này |
| 2026-11-17 | 03-tactical-design | Chốt bước 3 (gồm bổ sung context map A1–A3). H1, H4, H7 chuyển sang ADR ở bước 4 |
| 2026-11-17 | 04-architecture | Viết bản nháp: sơ đồ C4 mức 1–2, luồng đặt hàng VNPay, môi trường, ADR-0001…0010, lộ trình DevOps D1–D10 |
| 2026-11-17 | 01-scope | Q3 trỏ tới ADR-0008 (đề xuất 1 VPS); ghi nhận bạn tự làm CI/CD/DevOps |
| 2026-11-17 | 04-architecture | Đổi MinIO → SeaweedFS (MinIO cộng đồng đã archive 02/2026). Thêm ADR-0011 (Spring Data JPA), ADR-0012 (VNPay sandbox, PayOS để GĐ2), ADR-0013 (không Redis/broker ở GĐ1) |
| 2026-11-17 | 01-scope, 04-architecture | Giữ ngân sách 180 giờ; DevOps D1–D6 nằm trong walking skeleton, D7–D8 trong các lát; thiếu giờ thì bỏ Loki trước |
| 2026-11-17 | 03-tactical-design, 01-scope | Bổ sung sau khi đối chiếu với tài liệu ôn System Design: bất biến P9 (gửi hạn sang VNPay + ân hạn 3 phút trước khi cho hết hạn); sinh lại mã đơn khi trùng (tối đa 3 lần); thêm "đối soát toàn bộ với cổng thanh toán" vào ngoài phạm vi (GĐ2–3) |
| 2026-11-17 | 04-architecture | Chốt bước 4: 13 ADR chuyển sang "Đã chấp nhận"; Q3 chốt theo ADR-0008 |
| 2026-11-17 | 05-database | Viết bản nháp: 10 schema, bảng + cột + lý do, index, ràng buộc, migration và seed |
| 2026-11-17 | 04-architecture | Thêm ADR-0014 (một DB user, ranh giới schema ép bằng test), thay phần "mỗi module một DB user" của ADR-0001 vì mâu thuẫn với ADR-0002 |
| 2026-11-17 | 03-tactical-design | Thêm sự kiện `CustomerEmailVerified` (identity → order) cho CUS-03, phát hiện khi thiết kế bảng identity |
| 2026-11-17 | 03-tactical-design | Thêm 3.10 [access-control.md](design/03-tactical-design/access-control.md): 13 quyền, ma trận vai trò × quyền, luật sở hữu cho khách, luật phiên AC1–AC10 (đang review) |
| 2026-11-17 | 04-architecture | Thêm ADR-0015: IAM tự làm bằng Spring Security, điều kiện chuyển sang Keycloak (đang đề xuất) |
| 2026-11-17 | 05-database | Thêm bảng `identity.auth_events` (AC8) |
| 2026-11-17 | 05-database, 04-architecture, 03-tactical-design | Chốt bước 5; ADR-0014, ADR-0015 chuyển sang "Đã chấp nhận"; chốt ma trận quyền 3.10 |
| 2026-11-17 | 06-walking-skeleton | Viết kế hoạch bước 6: định nghĩa xong, phiên bản, cấu trúc repo, 13 nhiệm vụ S1–S13 (bạn tự làm, Claude hướng dẫn và review), thứ tự các lát dọc |
| 2026-11-17 | 04-architecture, 05-database | Cập nhật phiên bản: Java 25, Spring Boot 4.1 (bản 3.x hết hỗ trợ miễn phí 30/06/2026), Maven, PostgreSQL 18 |
| 2026-11-19 | 04-architecture | Thêm ADR-0016: frontend dùng Next.js 16 + Tailwind, Cache Components bật, React Compiler chưa bật (quyết ở S7) |
| 2026-11-20 | 06-walking-skeleton | S1–S8 xong. S8: chấp nhận image backend ~415 MB (vượt mục tiêu ~300 MB vì base JRE + Ubuntu), frontend ~225 MB |
| 2026-11-20 | 04-architecture, 06-walking-skeleton, 01-scope | S9 xong. Thêm ADR-0017: reverse proxy là Nginx thay Caddy (bạn chọn để học); ADR-0008 giữ phần còn lại. Cấu hình DB chuyển sang `application.yaml` dùng chung để chạy được không cần profile `local` |
