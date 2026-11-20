// Gọi backend từ phía server của Next.js (Server Component), không bao giờ từ trình duyệt.

import { connection } from "next/server";

export type Category = {
  id: string;
  parentId: string | null;
  slug: string;
  name: string;
  depth: number;
  position: number;
};

export async function getCategories(): Promise<Category[]> {
  // Chỉ chạy khi có request thật; lúc build Next dừng ở đây (API_BASE_URL chỉ có lúc chạy).
  await connection();

  const baseUrl = process.env.API_BASE_URL;
  if (!baseUrl) {
    throw new Error("API_BASE_URL chưa được đặt (xem frontend/.env.example)");
  }

  // Cache Components: fetch không được cache, trừ khi hàm được đánh dấu "use cache".
  const res = await fetch(`${baseUrl}/catalog/categories`);
  if (!res.ok) {
    throw new Error(`API trả về lỗi ${res.status}`);
  }
  return res.json();
}
