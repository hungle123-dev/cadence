# ADR-0016: Frontend: Next.js 16 + Tailwind, Cache Components bật, React Compiler chưa bật

- **Trạng thái:** Đã chấp nhận (2026-11-19)
- **Ngày:** 2026-11-19
- **Liên quan:** G7 (SEO), N3 (hiệu năng), ADR-0006 (cùng tên miền, gọi API phía server), ADR-0013 (chưa cần thì chưa thêm), S7

## Bối cảnh
S7 dựng frontend tối thiểu bằng `create-next-app` (Next.js 16.4). Cần chốt ba lựa chọn: cách làm giao diện, mô hình cache, và React Compiler. Prototype HTML chỉ để hình dung sản phẩm; CSS của nó không được dùng lại.

## Các lựa chọn và quyết định

| Câu hỏi | Lựa chọn | Chọn | Vì sao |
|---|---|---|---|
| Cách làm giao diện | Tailwind CSS / CSS Modules / CSS-in-JS | **Tailwind CSS v4** | Token thiết kế (màu, font, khoảng cách từ `DESIGN.md` của prototype) khai một lần trong `@theme` của `globals.css`; giao diện đồng nhất; dùng được shadcn/ui; phổ biến với Next.js |
| Mô hình cache | Cách cũ (tuỳ chọn trên `fetch`) / **Cache Components** | **Cache Components** (mặc định của create-next-app 16.4) | Mặc định mọi dữ liệu lấy mới mỗi request; chỗ nào cache thì đánh dấu `"use cache"` + `cacheLife` + `cacheTag`. Hợp với trang bán hàng: danh sách và chi tiết sản phẩm cache được, giỏ hàng và tài khoản luôn động |
| React Compiler | Bật / chưa bật | **Chưa bật** | Skeleton và các trang đầu chủ yếu là Server Component, compiler gần như không có việc; build chậm hơn. Bật khi có trang tương tác nhiều phía trình duyệt (giỏ hàng, checkout, bộ lọc) và đo thấy lợi |

## Luật đi kèm
- **Dữ liệu chưa cache phải nằm trong `<Suspense>`.** Khung trang dựng sẵn lúc build, phần dữ liệu đổ vào lúc có request (Partial Prerendering). Nhờ vậy **build không cần backend chạy**, nên CI (S10) build được độc lập.
- **Gọi backend chỉ từ phía server của Next.js**, qua biến `API_BASE_URL` (không có tiền tố `NEXT_PUBLIC_`), nên địa chỉ backend không lộ ra trình duyệt và không vướng CORS.
- **Không dùng tính năng `experimental`** trong `next.config.ts` nếu không có lý do ghi rõ. `experimental.agentFeedback` do create-next-app thêm đã được gỡ.
- `partialPrefetching` (mặc định của bản mới, chỉ hoạt động cùng Cache Components) được giữ.
- **Tổ chức thư mục:** trang khách nằm trong route group `src/app/(storefront)/` (không xuất hiện trong URL), trang quản trị nằm trong `src/app/admin/` (đường dẫn `/admin`, ADR-0006: chung một app). Khi cần deploy admin riêng, chuyển `admin/` thành app thứ hai trong pnpm workspace.
- **Công cụ:** pnpm 10.33 (`packageManager` trong `package.json`, giống dự án YBA), Node 24 ghi trong `.nvmrc` (máy dev tối thiểu 22), cài thư viện luôn ghi phiên bản chính xác (`.npmrc`: `save-exact=true`). Chưa dùng workspace vì chỉ có một app JavaScript.
- **Font theo `DESIGN.md`:** Be Vietnam Pro (chữ thường) và Barlow Condensed (tiêu đề), nạp bằng `next/font` với bộ chữ `vietnamese`. Không dùng font mặc định của mẫu (Geist chỉ có bộ chữ Latin, thiếu dấu tiếng Việt).

## Hệ quả
- Ở lát catalog, phải quyết cụ thể: trang nào dùng `"use cache"`, giữ bao lâu (`cacheLife`), làm mới bằng `revalidateTag` khi admin sửa sản phẩm. Quyết kèm số đo k6 (N3).
- Phần lớn hướng dẫn trên mạng viết cho mô hình cache cũ; đọc tài liệu đi kèm trong `node_modules/next/dist/docs/` trước khi làm theo.
- Bật React Compiler sau này chỉ cần một dòng `reactCompiler: true` và một package.
