# Triển khai lên VPS

Làm một lần khi dựng máy chủ demo (milestone M0). Sau đó mỗi lần merge vào `main`, CI tự build image và triển khai.

## 1. Chuẩn bị

- Một VPS Linux (Ubuntu 24.04 trở lên), tối thiểu 2 GB RAM.
- Một domain trỏ bản ghi `A` về IP của VPS.
- Trên VPS: cài Docker Engine và Docker Compose plugin, cài `certbot`.

## 2. Thư mục trên VPS

```
/opt/cadence/
├── compose.prod.yaml        ← chép từ deploy/compose.prod.yaml
├── .env                     ← bí mật thật, chỉ nằm trên máy chủ
├── nginx/                   ← chép từ deploy/nginx/ (prod.conf.template, snippets/)
└── postgres/init.sh         ← chép từ deploy/postgres/init.sh
```

`.env` trên VPS:

```
DOMAIN=<domain của bạn>
GHCR_OWNER=<tên tài khoản GitHub, chữ thường>
IMAGE_TAG=latest
POSTGRES_PASSWORD=<chuỗi ngẫu nhiên dài>
DB_OWNER_PASSWORD=<chuỗi ngẫu nhiên dài>
DB_APP_PASSWORD=<chuỗi ngẫu nhiên dài>
```

Sinh mật khẩu: `openssl rand -base64 32`.

## 3. Chứng chỉ HTTPS lần đầu

Nginx cần chứng chỉ để khởi động khối `443`, nên lấy chứng chỉ trước bằng chế độ standalone (cổng 80 còn trống):

```
sudo certbot certonly --standalone -d <domain>
sudo mkdir -p /var/www/certbot
```

Gia hạn tự động về sau dùng webroot (Nginx đã phục vụ `/.well-known/acme-challenge/`):

```
sudo certbot renew --webroot -w /var/www/certbot --deploy-hook "docker exec cadence-nginx-1 nginx -s reload"
```

## 4. Chạy

```
cd /opt/cadence
docker compose -f compose.prod.yaml pull
docker compose -f compose.prod.yaml up -d
```

Kiểm tra: `https://<domain>` hiện trang chủ với dòng "Hệ thống sẵn sàng".

## 5. Giới hạn đã biết: mất kết nối vài giây mỗi lần triển khai

`docker compose up -d` dừng container cũ rồi mới chạy container mới. Trong khoảng đó (thường 10–30 giây, chủ yếu là thời gian Spring Boot khởi động và Flyway chạy migration), Nginx trả lỗi 502 cho request tới backend.

**Chấp nhận cho bản demo**, vì:

- Lưu lượng nhỏ, triển khai ít lần mỗi ngày.
- Request đang chạy dở được làm cho xong trước khi tắt (`server.shutdown: graceful`).
- Container mới chỉ nhận traffic khi healthcheck báo khoẻ.

**Khi nào cần làm tốt hơn và làm thế nào:** khi có người dùng thật. Cách làm là chạy hai bản backend song song (blue-green): bật bản mới, chờ healthcheck khoẻ, chuyển `upstream` của Nginx sang bản mới, rồi mới tắt bản cũ. Điều kiện để làm được: migration phải tương thích ngược, tức bản cũ vẫn chạy được trên schema mới (tài liệu database mục 7).

Nếu migration lỗi, backend mới không khởi động được và healthcheck không bao giờ khoẻ. Quay lại bản trước bằng cách đặt `IMAGE_TAG=<mã commit cũ>` trong `.env` (mục 2) rồi chạy lại các lệnh ở mục 4, sau đó sửa migration bằng một file **mới**, không sửa file đã chạy.

## 6. Sao lưu DB hằng ngày

`crontab -e` trên VPS:

```
0 3 * * * docker exec cadence-db-1 pg_dump -U postgres -Fc cadence > /opt/cadence/backup/cadence-$(date +\%F).dump && find /opt/cadence/backup -name '*.dump' -mtime +7 -delete
```

## 7. Cho CI tự triển khai

Trong GitHub repo → Settings:

| Loại | Tên | Giá trị |
|---|---|---|
| Variable | `DEPLOY_ENABLED` | `true` |
| Secret | `VPS_HOST` | IP hoặc domain của VPS |
| Secret | `VPS_USER` | user SSH trên VPS (có quyền chạy docker) |
| Secret | `VPS_SSH_KEY` | private key SSH dành riêng cho CI |

Khi chưa đặt `DEPLOY_ENABLED`, CI vẫn build và đẩy image nhưng bỏ qua bước triển khai.

Image trên GitHub Container Registry mặc định là private: hoặc đặt package thành public, hoặc chạy `docker login ghcr.io` trên VPS bằng một token chỉ có quyền `read:packages`.
