import { Suspense } from "react";

import { getCategories } from "@/lib/api";

// Cache Components: khung trang (tiêu đề, dòng "Đang tải…") được dựng sẵn lúc build.
// Danh sách category lấy từ API lúc có request, rồi đổ vào chỗ của dòng "Đang tải…".
export default function HomePage() {
  return (
    <main className="p-8">
      <h1 className="text-2xl font-bold">Cadence</h1>
      <Suspense fallback={<p>Đang tải danh mục…</p>}>
        <CategoryList />
      </Suspense>
    </main>
  );
}

async function CategoryList() {
  const categories = await getCategories();
  const roots = categories.filter((c) => c.depth === 1);

  return (
    <ul>
      {roots.map((root) => (
        <li key={root.id}>
          {root.name}
          <ul>
            {categories
              .filter((c) => c.parentId === root.id)
              .map((child) => (
                <li key={child.id}>{child.name}</li>
              ))}
          </ul>
        </li>
      ))}
    </ul>
  );
}
