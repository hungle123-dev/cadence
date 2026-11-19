# Bước 6: Walking skeleton

> **Trạng thái:** Đang làm · Thuộc [tài liệu thiết kế](../README.md) · Trước: [Bước 5](05-database/README.md)

**Cách làm đã thống nhất:** bạn tự viết toàn bộ code và hạ tầng; Claude hướng dẫn, giải thích và review. Mỗi nhiệm vụ bên dưới có mục tiêu, việc phải làm, cách tự kiểm tra và tài liệu liên quan. Làm xong một nhiệm vụ thì nhờ Claude review ("review S3"), sửa xong mới sang nhiệm vụ sau.

## 6.1 Walking skeleton là gì, và khi nào xong

Walking skeleton là **lát mỏng nhất đi xuyên qua mọi tầng và mọi khâu**, chưa có tính năng thật. Mục đích: mọi thứ "nối dây" được kiểm chứng sớm (build, test, kiến trúc, database, frontend, container, CI, deploy), để từ lát tính năng đầu tiên trở đi chỉ còn lo nghiệp vụ.

**Luồng duy nhất của skeleton:** trang chủ Next.js (render phía server) hiển thị danh sách category, lấy từ `GET /api/catalog/categories`, đọc từ bảng `catalog.categories` do Flyway tạo, chạy trên staging có HTTPS, được deploy tự động khi merge vào `main`.

Luồng này chạm đủ: Next.js → Caddy → controller → use case → port → adapter JPA → PostgreSQL → ngược lại; Flyway; test ở cả ba tầng; Docker; CI; CD.

**Xong khi tất cả đều đúng:**
- [ ] Mở `https://<tên miền staging>` thấy danh sách category lấy từ database.
- [ ] `GET /api/actuator/health` trả `UP` (có kiểm tra DB).
- [ ] Bộ test ArchUnit chạy trong CI và **đã từng bắt được một vi phạm cố ý** (bạn tự tạo rồi xoá).
- [ ] Có test domain (không Spring), test use case (port giả), test tích hợp (Testcontainers, Postgres thật).
- [ ] Mở pull request → CI chạy, đỏ thì không merge được.
- [ ] Merge vào `main` → image lên GHCR → VPS tự cập nhật → smoke test xanh, không cần SSH tay.
- [ ] Log của API là JSON, có `correlationId`.

## 6.2 Phiên bản và công cụ

| Thứ | Chọn | Ghi chú |
|---|---|---|
| Java | **25 (LTS)** | |
| Spring Boot | **4.1** (Spring Framework 7) | Nâng lên 4.2 khi ra (dự kiến 19/11/2026) là nâng bản nhỏ. Bản 3.x đã hết hỗ trợ miễn phí từ 30/06/2026 |
| Build | **Maven** (dùng Maven Wrapper `./mvnw`) | Wrapper để máy nào, CI nào cũng dùng đúng một phiên bản Maven |
| Frontend | **Next.js 16** + TypeScript, App Router | |
| Node.js | Bản LTS hiện hành | Kiểm tra trên nodejs.org khi cài |
| Database | **PostgreSQL 18** | |
| Lưu ảnh | SeaweedFS | Chưa cần trong skeleton; thêm ở lát catalog |
| Proxy | Caddy 2 | |
| CI/CD | GitHub Actions + GHCR | |

**Cảnh báo khi đọc hướng dẫn trên mạng:** phần lớn bài viết và câu trả lời vẫn viết cho Spring Boot 3. Boot 4 có thay đổi (chia nhỏ starter, Jackson 3, Hibernate 7, một số cấu hình đổi tên). Gặp lỗi lạ thì **đọc tài liệu chính thức và release notes của Boot 4** trước khi chép code mẫu.

## 6.3 Cấu trúc repo

Một repository (monorepo) cho cả dự án:

```
cadence/
├─ backend/                     Maven, một ứng dụng Spring Boot
│  └─ src/main/java/com/cadence/
│     ├─ CadenceApplication.java
│     ├─ common/                shared kernel (template mục 3)
│     ├─ catalog/  cart/  inventory/  order/  payment/
│     ├─ fulfillment/  identity/  notification/
│     │    └─ mỗi module: domain/ application/ infrastructure/ (template mục 3)
│  └─ src/main/resources/db/migration/<module>/
│  └─ src/test/java/com/cadence/   (ArchitectureTest + test theo module)
├─ frontend/                    Next.js
├─ infra/
│  ├─ compose.yaml              chạy local
│  ├─ compose.staging.yaml      chạy trên VPS
│  ├─ caddy/Caddyfile
│  └─ db/init/                  tạo user cadence_app, cadence_migrator (ADR-0014)
├─ .github/workflows/
├─ docs/                        (đã có)
└─ prototype/                   (đã có)
```

Package Java của module order vẫn là `order` (không phải từ khoá Java); chỉ schema DB là `ordering` (5.2).

## 6.4 Nhiệm vụ

Làm theo thứ tự. Cột "Trạng thái" cập nhật khi review xong.

| # | Nhiệm vụ | Xong khi | Liên quan | Trạng thái |
|---|---|---|---|---|
| S1 | Khởi tạo Git + repo GitHub | Repo có `.gitignore`, README ngắn, `docs/` và `prototype/` đã commit | — | ✅ |
| S2 | Tạo project backend | `./mvnw verify` chạy được; có đủ 8 package module + `common`, mỗi module có `domain/application/infrastructure` | ADR-0001, template mục 3 | ✅ |
| S3 | Viết bộ test kiến trúc **trước** code nghiệp vụ | Test R1–R10 + luật module + luật JPA + luật schema chạy xanh; tạo một vi phạm cố ý thì đỏ | ADR-0001, 0011, 0014, 0015 | ✅ |
| S4 | PostgreSQL local + Flyway | Migration tạo schema `catalog`, bảng `categories`; seed vài dòng chỉ ở profile local; ứng dụng kết nối bằng `cadence_app` | 5.1, 5.5, 5.10, ADR-0014 | ✅ |
| S5 | Lát mỏng qua hexagon: liệt kê category | `GET /api/catalog/categories` trả JSON; có test domain, test use case, test tích hợp Testcontainers | Template mục 6, ADR-0011 | ✅ |
| S6 | Nền xuyên suốt | Lỗi trả Problem Details; log JSON có `correlationId`; health liveness/readiness | ADR-0006, ADR-0010 | ⚪ |
| S7 | Frontend tối thiểu | Trang `/` render phía server, gọi API, hiện danh sách category | ADR-0006 | ⚪ |
| S8 | **D1** Dockerfile cho backend và frontend | Multi-stage, chạy bằng user không phải root, image backend dưới ~300 MB | ADR-0008 | ⚪ |
| S9 | **D2** Docker Compose local | Một lệnh `docker compose up` chạy Postgres + API + Web + Caddy; mở `http://localhost` thấy trang | ADR-0008 | ⚪ |
| S10 | **D3** CI | Pull request chạy build, test (cả Testcontainers), lint/build frontend; bật branch protection cho `main` | ADR-0009 | ⚪ |
| S11 | **D4** Dựng VPS an toàn | Đăng nhập bằng SSH key, tắt mật khẩu, firewall chỉ mở 22/80/443, user `deploy` không phải root, cài Docker | ADR-0008 | ⚪ |
| S12 | **D5** Tên miền + HTTPS | `https://<staging>` có chứng chỉ hợp lệ qua Caddy | ADR-0008 | ⚪ |
| S13 | **D6** CD | Merge vào `main` → image lên GHCR → VPS kéo về và khởi động → smoke test; rollback bằng tag cũ | ADR-0009 | ⚪ |

### Gợi ý cho từng nhiệm vụ

**S1.** Tạo repo GitHub (public, để CI miễn phí và để nhà tuyển dụng xem). `.gitignore` phải loại `target/`, `node_modules/`, `.next/`, `.env*`, file IDE. Không bao giờ commit bí mật (ADR-0008).

**S2.** Tạo bằng Spring Initializr: Maven, Java 25, Boot 4.1, group `com.cadence`. Chỉ thêm dependency cần cho skeleton: Web, Data JPA, PostgreSQL driver, Flyway, Actuator, Validation, Testcontainers. **Chưa thêm** Security, Spring Session, AWS SDK: thêm khi lát dùng tới. Tạo package rỗng theo cấu trúc template; có thể đặt `package-info.java` mô tả vai trò mỗi module.

**S3.** Đây là nhiệm vụ quan trọng nhất của skeleton. Bộ luật cần có:
- R1–R10 của template (template mục 5.1, có mẫu ArchUnit ở mục 5.2).
- Module chỉ gọi module khác qua `application` của bên kia (R5); `common` không phụ thuộc module nghiệp vụ (R8).
- `jakarta.persistence` chỉ xuất hiện trong `infrastructure/persistence` (ADR-0011).
- Entity JPA của module X chỉ khai báo schema `x` (ADR-0014).
- Không có code kiểm vai trò kiểu `role == ADMIN` (ADR-0015); luật này thêm khi có Security.

Viết xong, **cố ý vi phạm** một luật (ví dụ cho domain import một class của Spring), thấy test đỏ, rồi xoá. Đó là bằng chứng bộ test hoạt động.

**S4.** Script trong `infra/db/init/` tạo `cadence_migrator` (Flyway dùng) và `cadence_app` (ứng dụng dùng, không có quyền tạo/xoá bảng). Migration theo quy ước 5.5 (`db/migration/catalog/V2026…__catalog_create_categories.sql`). Seed category chỉ chạy ở profile `local`.

**S5.** Làm đúng luồng template mục 6.2 (use case đọc):
`CategoryController` (infrastructure/restapi) → `ListCategoriesUseCase` (application/port/driving) → `ListCategoriesService` → `CategoryReadPort` (application/port/driven) → adapter JPA (infrastructure/persistence). Port **không trả entity JPA** (R9). Ba loại test: domain (nếu `Category` có luật, ví dụ tối đa 3 cấp), use case với port giả, tích hợp với Testcontainers.

**S6.** Một bộ xử lý lỗi chung trả Problem Details (RFC 9457) có mã lỗi ổn định. Filter đọc header `X-Correlation-Id` hoặc tạo mới, đưa vào MDC để mọi dòng log có nó, và trả lại trong response. Log JSON bằng cấu hình logging có sẵn của Spring Boot (structured logging). Health: bật liveness và readiness.

**S7.** `create-next-app` với TypeScript, App Router. Trang `/` là Server Component gọi API bằng biến môi trường (`API_BASE_URL`), không gọi từ trình duyệt. Chưa cần giao diện đẹp; giao diện thật làm ở các lát, dựa vào prototype.

**S8–S13.** Theo lộ trình DevOps ở [bước 4 mục 4.6](04-architecture/README.md). Gợi ý chung:
- Image backend: stage build dùng JDK, stage chạy dùng JRE; có thể thử layered jar để build lại nhanh.
- Compose local: Postgres có volume để dữ liệu không mất; API chờ Postgres **healthy** rồi mới chạy (healthcheck + `depends_on: condition`).
- CI: runner `ubuntu-latest` có sẵn Docker nên Testcontainers chạy được; cache thư mục Maven và npm.
- VPS: làm lần lượt, mỗi bước kiểm tra vẫn SSH vào được **trước khi** đóng cửa sổ cũ (tránh tự khoá mình ngoài).
- CD: tag image theo commit SHA, không chỉ `latest`, để rollback được.

## 6.5 Làm việc với Claude

- **Hỏi khái niệm** bất cứ lúc nào ("layered jar là gì?", "MDC là gì?").
- **Review:** báo "review S…", Claude đọc code và chạy test, đối chiếu với thiết kế (bất biến, ADR, template) và chỉ ra chỗ cần sửa, kèm lý do. Claude không viết lại code thay bạn, trừ khi bạn yêu cầu một đoạn ví dụ.
- **Kẹt quá lâu** (ví dụ quá 1 giờ cho một lỗi): gửi lỗi và những gì đã thử, Claude gợi ý hướng, không đưa đáp án ngay trừ khi bạn muốn.
- Sau mỗi nhiệm vụ: cập nhật cột "Trạng thái" ở bảng 6.4 và nhật ký trong [docs/README.md](../README.md).

## 6.6 Sau skeleton: các lát dọc

| Lát | Nội dung | Vì sao thứ tự này |
|---|---|---|
| 1 | Catalog đọc (PLP, PDP, SEO) → giỏ → đặt hàng COD → giữ hàng; test đồng thời N1 | Chạm phần khó nhất (giữ hàng, ADR-0002/0003) sớm nhất |
| 2 | VNPay: URL, IPN, hết hạn, đối soát, tiền đến muộn; outbox; email | G3; cần staging có HTTPS (đã có từ skeleton) |
| 3 | Staff: xác nhận, huỷ, kiện hàng, trừ kho, hoàn về; timeline | Hoàn tất vòng đời đơn |
| 4 | Admin catalog, tài khoản, phân quyền, tìm kiếm | Phần CRUD, làm sau cùng; CAT-04 là mục cắt đầu tiên khi trễ |

Kế hoạch chi tiết cho từng lát viết khi tới lát đó.
