// Gọi backend từ phía server của Next.js (Server Component), không bao giờ từ trình duyệt.

export type Category = {
  id: string;
  parentId: string | null;
  slug: string;
  name: string;
  depth: number;
  position: number;
};

export async function getCategories(): Promise<Category[]> {
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
