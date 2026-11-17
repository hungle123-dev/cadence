/* Dữ liệu giả cho prototype. Cấu trúc bám mô hình domain: Product → Colorway → Variant(size).
   Ảnh: Unsplash (giấy phép Unsplash, miễn phí), đã lọc ảnh lộ logo thương hiệu khác.
   Lưu ý prototype: mỗi sản phẩm chỉ có ảnh của 1 màu; ảnh phụ trong gallery là ảnh bối cảnh. */
window.CAD = window.CAD || {};

CAD.img = function (id, w) {
  return 'https://images.unsplash.com/photo-' + id + '?auto=format&fit=crop&q=70&w=' + (w || 900);
};

/* Nhóm màu dùng cho bộ lọc */
CAD.colorFamilies = {
  black: { name: 'Đen', hex: '#151515' }, white: { name: 'Trắng', hex: '#f4f4f1' },
  grey: { name: 'Xám', hex: '#9a9a96' }, green: { name: 'Xanh rêu', hex: '#5d6343' },
  blue: { name: 'Xanh navy', hex: '#24324f' }, pink: { name: 'Hồng', hex: '#e6b8b0' },
  brown: { name: 'Nâu', hex: '#8a6a4a' }
};

CAD.products = [
  { slug: 'tempo-2', cat: 'giay-chay-bo', name: 'Cadence Tempo 2', type: 'shoes', sport: 'chay-bo', category: 'Giày chạy bộ', gender: 'nam',
    price: 3290000, salePrice: null, flag: 'Mới', rank: 1, rating: 4.7, reviews: 128,
    colors: [{ name: 'Ô liu / Trắng', family: 'green' }, { name: 'Đen / Trắng', family: 'black' }, { name: 'Xám mây', family: 'grey' }],
    img: '1631542156377-473540702434', gallery: ['1590333748338-d629e4564ad9', '1758506971986-b0d0edebd8d5'],
    sizes: { '39': 3, '40': 8, '41': 5, '42': 0, '43': 2, '44': 6 },
    desc: 'Giày chạy hằng ngày cho những buổi chạy dài trên đường nhựa. Đệm CadenceFoam giữ độ êm trong thời tiết nóng ẩm, thân dệt thoáng khí và khô nhanh sau mưa.',
    specs: { 'Trọng lượng': '228 g · size 42', 'Độ chênh gót–mũi': '8 mm', 'Chiều cao đế': '36 / 28 mm', 'Đệm': 'CadenceFoam N2', 'Phù hợp': 'Chạy dài, chạy tempo' },
    fit: 0.1 },
  { slug: 'court-classic', cat: 'giay-lifestyle', name: 'Court Classic Gum', type: 'shoes', sport: 'lifestyle', category: 'Giày lifestyle', gender: 'unisex',
    price: 2190000, salePrice: null, flag: null, rank: 3, rating: 4.8, reviews: 311,
    colors: [{ name: 'Trắng ngà / Gum', family: 'white' }, { name: 'Đen / Gum', family: 'black' }, { name: 'Nâu đất', family: 'brown' }],
    img: '1727061180303-d91cdeca6f9c', gallery: ['1643935272030-0dbc34b8fd29'],
    sizes: { '38': 4, '39': 6, '40': 9, '41': 7, '42': 5, '43': 3 },
    desc: 'Dáng giày tennis cổ điển, da lộn mềm và đế cao su gum. Đi hằng ngày, phối được với mọi thứ.',
    specs: { 'Chất liệu thân': 'Da lộn', 'Đế': 'Cao su gum tự nhiên', 'Lót': 'Đệm EVA tháo rời được' },
    fit: 0 },
  { slug: 'canvas-low', cat: 'giay-lifestyle', name: 'Canvas Low', type: 'shoes', sport: 'lifestyle', category: 'Giày lifestyle', gender: 'nam',
    price: 1290000, salePrice: null, flag: null, rank: 5, rating: 4.6, reviews: 203,
    colors: [{ name: 'Xanh navy', family: 'blue' }, { name: 'Đen', family: 'black' }],
    img: '1496202703211-aa28e9500c30', gallery: [],
    sizes: { '39': 5, '40': 6, '41': 4, '42': 3, '43': 2, '44': 0 },
    desc: 'Giày vải canvas cổ thấp, đế vulcanized bền và nhẹ.',
    specs: { 'Chất liệu thân': 'Canvas 12oz', 'Đế': 'Cao su vulcanized' },
    fit: 0 },
  { slug: 'daily-canvas', cat: 'giay-lifestyle', name: 'Daily Canvas', type: 'shoes', sport: 'lifestyle', category: 'Giày lifestyle', gender: 'nu',
    price: 1190000, salePrice: null, flag: 'Mới', rank: 9, rating: 4.5, reviews: 18,
    colors: [{ name: 'Hồng đất', family: 'pink' }, { name: 'Trắng', family: 'white' }],
    img: '1552912276-56ef47874741', gallery: [],
    sizes: { '35': 2, '36': 5, '37': 6, '38': 3, '39': 1 },
    desc: 'Giày vải nhẹ, dây hai màu, êm cho cả ngày dài.',
    specs: { 'Chất liệu thân': 'Canvas', 'Đế': 'Cao su' },
    fit: 0.2 },
  { slug: 'everyday-trainer', cat: 'giay-tap', name: 'Everyday Trainer', type: 'shoes', sport: 'tap-luyen', category: 'Giày tập luyện', gender: 'nam',
    price: 2390000, salePrice: null, flag: null, rank: 7, rating: 4.2, reviews: 39,
    colors: [{ name: 'Xám', family: 'grey' }],
    img: '1643935272030-0dbc34b8fd29', gallery: [],
    sizes: { '40': 3, '41': 4, '42': 4, '43': 2 },
    desc: 'Đế phẳng, vững cho bài tập tạ và cardio trong phòng gym.',
    specs: { 'Độ chênh gót–mũi': '4 mm', 'Đế': 'Cao su chống trượt' },
    fit: 0 },
  { slug: 'daily-run-tee', cat: 'ao-thun', name: 'Daily Run Tee', type: 'apparel', sport: 'chay-bo', category: 'Áo chạy bộ', gender: 'nam',
    price: 690000, salePrice: null, flag: 'Mới', rank: 2, rating: 4.6, reviews: 92,
    colors: [{ name: 'Trắng', family: 'white' }, { name: 'Đen', family: 'black' }],
    img: '1622445275463-afa2ab738c34', gallery: ['1651761179569-4ba2aa054997', '1620799139652-715e4d5b232d'],
    sizes: { 'S': 4, 'M': 7, 'L': 3, 'XL': 0 },
    desc: 'Áo chạy vải lưới tái chế, khô nhanh, đường may phẳng không cọ xát.',
    specs: { 'Chất liệu': '100% polyester tái chế', 'Trọng lượng vải': '110 gsm', 'Form': 'Regular' },
    fit: 0 },
  { slug: 'club-tee-3', cat: 'ao-thun', name: 'Club Tee, gói 3 áo', type: 'apparel', sport: 'lifestyle', category: 'Áo thun', gender: 'unisex',
    price: 990000, salePrice: 790000, flag: 'Sale', rank: 10, rating: 4.7, reviews: 240,
    colors: [{ name: 'Đen', family: 'black' }],
    img: '1610502778270-c5c6f4c7d575', gallery: [],
    sizes: { 'S': 3, 'M': 5, 'L': 0, 'XL': 2 },
    desc: 'Ba áo thun cotton dày dặn, cổ tròn bo chắc, không bai sau giặt.',
    specs: { 'Chất liệu': '100% cotton', 'Trọng lượng vải': '220 gsm', 'Form': 'Regular' },
    fit: 0.1 },
  { slug: 'relaxed-tee', cat: 'ao-thun', name: 'Relaxed Tee', type: 'apparel', sport: 'lifestyle', category: 'Áo thun', gender: 'nu',
    price: 490000, salePrice: null, flag: 'Thành viên', rank: 12, rating: 4.5, reviews: 21,
    colors: [{ name: 'Trắng', family: 'white' }],
    img: '1622445270936-5dcb604970e7', gallery: ['1737094540182-d602f8a7dd1d'],
    sizes: { 'XS': 3, 'S': 5, 'M': 2, 'L': 0 },
    desc: 'Áo thun trắng cơ bản, form thả nhẹ.',
    specs: { 'Chất liệu': '95% cotton, 5% spandex', 'Form': 'Relaxed' },
    fit: 0.2 },
  { slug: 'studio-tee', cat: 'ao-thun', name: 'Studio Tee', type: 'apparel', sport: 'tap-luyen', category: 'Áo tập', gender: 'nam',
    price: 550000, salePrice: null, flag: null, rank: 13, rating: 4.3, reviews: 12,
    colors: [{ name: 'Trắng', family: 'white' }],
    img: '1622445272461-c6580cab8755', gallery: [],
    sizes: { 'S': 2, 'M': 3, 'L': 3, 'XL': 1 },
    desc: 'Áo tập co giãn bốn chiều, thấm hút mồ hôi.',
    specs: { 'Chất liệu': 'Cotton pha spandex', 'Form': 'Regular' },
    fit: -0.2 },
  { slug: 'club-crew', cat: 'hoodie', name: 'Club Crew Sweat', type: 'apparel', sport: 'lifestyle', category: 'Áo nỉ', gender: 'unisex',
    price: 990000, salePrice: null, flag: null, rank: 14, rating: 4.8, reviews: 67,
    colors: [{ name: 'Trắng', family: 'white' }, { name: 'Xám', family: 'grey' }],
    img: '1620799140408-edc6dcb6d633', gallery: [],
    sizes: { 'S': 1, 'M': 0, 'L': 4, 'XL': 2 },
    desc: 'Áo nỉ cotton chải lông, ấm nhưng không bí.',
    specs: { 'Chất liệu': '80% cotton, 20% polyester', 'Trọng lượng vải': '380 gsm', 'Form': 'Relaxed' },
    fit: 0.3 },
  /* ---- Sản phẩm mở rộng cho prototype (dữ liệu minh hoạ; ảnh Unsplash đã lọc logo hãng khác) ---- */
  {"slug": "pace-glide-w", "cat": "giay-chay-bo", "name": "Cadence Pace Glide nữ", "type": "shoes", "sport": "chay-bo", "category": "Giày chạy bộ", "gender": "nu", "price": 2690000, "salePrice": null, "flag": "Mới", "rank": 19, "rating": 4.3, "reviews": 141, "colors": [{"name": "Trắng / Hồng", "family": "pink"}, {"name": "Trắng", "family": "white"}], "img": "1653179767393-381b3bfae1a9", "gallery": ["1452626038306-9aae5e071dd3", "1553012547-284d8c582b1f"], "sizes": {"35": 0, "36": 2, "37": 8, "38": 8, "39": 1, "40": 1}, "desc": "Phiên bản nữ với form mũi gọn, gót ôm chân, đệm êm cho buổi chạy 5–10 km.", "specs": {"Trọng lượng": "221 g · size 38", "Độ chênh gót–mũi": "8 mm", "Đệm": "CadenceFoam", "Mặt đế": "Cao su bám đường ướt", "Phù hợp": "Chạy hằng ngày"}, "fit": 0.1},
  {"slug": "mudline-trail", "cat": "giay-trail", "name": "Mudline Trail", "type": "shoes", "sport": "trail", "category": "Giày chạy địa hình", "gender": "unisex", "price": 2890000, "salePrice": 2290000, "flag": "Sale", "rank": 35, "rating": 4.5, "reviews": 18, "colors": [{"name": "Xám / Vàng", "family": "grey"}], "img": "1792955104796-31d15ec8c28f", "gallery": ["1530143311094-34d807799e8f"], "sizes": {"37": 2, "38": 1, "39": 4, "40": 8, "41": 0, "42": 3, "43": 5, "44": 2}, "desc": "Gai đế 5 mm thoát bùn nhanh, thân giày chống nước nhẹ cho đường mòn mùa mưa.", "specs": {"Trọng lượng": "305 g · size 42", "Gai đế": "4–5 mm", "Mũi giày": "Gia cố chống va đập", "Phù hợp": "Đường mòn, dốc đất"}, "fit": 0},
  {"slug": "heritage-low", "cat": "giay-lifestyle", "name": "Heritage Low", "type": "shoes", "sport": "lifestyle", "category": "Giày lifestyle", "gender": "nam", "price": 2290000, "salePrice": null, "flag": null, "rank": 34, "rating": 4.2, "reviews": 76, "colors": [{"name": "Xám nhạt / Đỏ đô", "family": "grey"}], "img": "1699092372473-a829f4421580", "gallery": ["1765710307817-db7fd96602d4", "1645557194575-4086233e70ff"], "sizes": {"39": 2, "40": 1, "41": 1, "42": 0, "43": 8, "44": 0}, "desc": "Dáng giày tập thập niên 80, viền gót đỏ đô, đế cao su thơm mùi cũ.", "specs": {"Trọng lượng": "360 g · size 42", "Thân giày": "Da tổng hợp / canvas", "Đế": "Cao su lưu hoá", "Phù hợp": "Đi hằng ngày"}, "fit": -0.3},
  {"slug": "prism-low", "cat": "giay-lifestyle", "name": "Prism Low", "type": "shoes", "sport": "lifestyle", "category": "Giày lifestyle", "gender": "nu", "price": 2190000, "salePrice": 1690000, "flag": "Sale", "rank": 38, "rating": 4.4, "reviews": 6, "colors": [{"name": "Trắng / Nhiều màu", "family": "white"}], "img": "1560769629-975ec94e6a86", "gallery": ["1645557194575-4086233e70ff", "1765710307817-db7fd96602d4"], "sizes": {"35": 8, "36": 12, "37": 3, "38": 5, "39": 0, "40": 5}, "desc": "Chi tiết màu pastel trên nền trắng, đế dày vừa đủ để cao thêm 3 cm.", "specs": {"Trọng lượng": "310 g · size 38", "Thân giày": "Da tổng hợp / canvas", "Đế": "Cao su lưu hoá", "Phù hợp": "Đi hằng ngày"}, "fit": -0.3},
  {"slug": "easy-slip", "cat": "giay-lifestyle", "name": "Easy Slip-on", "type": "shoes", "sport": "lifestyle", "category": "Giày lifestyle", "gender": "unisex", "price": 1590000, "salePrice": null, "flag": null, "rank": 29, "rating": 4.7, "reviews": 24, "colors": [{"name": "Nâu da bò / Cam", "family": "brown"}], "img": "1634581448750-22a591d78099", "gallery": ["1765710307817-db7fd96602d4", "1645557194575-4086233e70ff"], "sizes": {"37": 3, "38": 1, "39": 5, "40": 12, "41": 3, "42": 12, "43": 3, "44": 0}, "desc": "Xỏ vào là đi, đế dày êm, hợp đi phố cả ngày.", "specs": {"Trọng lượng": "330 g · size 42", "Thân giày": "Da tổng hợp / canvas", "Đế": "Cao su lưu hoá", "Phù hợp": "Đi hằng ngày"}, "fit": -0.1},
  {"slug": "essential-tee", "cat": "ao-thun", "name": "Essential Tee", "type": "apparel", "sport": "lifestyle", "category": "Áo thun", "gender": "nam", "price": 450000, "salePrice": null, "flag": "Bán chạy", "rank": 37, "rating": 4.8, "reviews": 24, "colors": [{"name": "Trắng", "family": "white"}, {"name": "Đen", "family": "black"}], "img": "1651761179569-4ba2aa054997", "gallery": ["1765710307817-db7fd96602d4", "1645557194575-4086233e70ff"], "sizes": {"S": 5, "M": 3, "L": 0, "XL": 12}, "desc": "Áo thun cotton 220 gsm, cổ tròn đứng form, giặt nhiều không giãn.", "specs": {"Chất liệu": "Cotton 100%, 220 gsm", "Công nghệ": "Thoát ẩm, khô nhanh", "Dáng": "Vừa người", "Giặt": "Máy, nước lạnh"}, "fit": 0.1},
  {"slug": "drycool-tee", "cat": "ao-thun", "name": "DryCool Tee", "type": "apparel", "sport": "chay-bo", "category": "Áo thun", "gender": "nam", "price": 590000, "salePrice": null, "flag": null, "rank": 41, "rating": 4.4, "reviews": 76, "colors": [{"name": "Xanh xám", "family": "blue"}], "img": "1564584217132-2271feaeb3c5", "gallery": ["1452626038306-9aae5e071dd3", "1553012547-284d8c582b1f"], "sizes": {"S": 2, "M": 1, "L": 5, "XL": 2}, "desc": "Áo chạy vải lưới siêu nhẹ, khô nhanh dưới nắng trưa Sài Gòn.", "specs": {"Chất liệu": "Polyester tái chế 100%", "Công nghệ": "Thoát ẩm, khô nhanh", "Dáng": "Vừa người", "Giặt": "Máy, nước lạnh"}, "fit": -0.3},
  {"slug": "organic-tee", "cat": "ao-thun", "name": "Organic Tee nữ", "type": "apparel", "sport": "lifestyle", "category": "Áo thun", "gender": "nu", "price": 490000, "salePrice": null, "flag": "Mới", "rank": 45, "rating": 4.6, "reviews": 103, "colors": [{"name": "Xanh lá nhạt", "family": "green"}], "img": "1759572095329-1dcf9522762b", "gallery": ["1765710307817-db7fd96602d4", "1645557194575-4086233e70ff"], "sizes": {"XS": 8, "S": 1, "M": 12, "L": 8}, "desc": "Cotton hữu cơ mềm, dáng hơi rộng, màu nhuộm từ thực vật.", "specs": {"Chất liệu": "Cotton hữu cơ 100%", "Công nghệ": "Thoát ẩm, khô nhanh", "Dáng": "Vừa người", "Giặt": "Máy, nước lạnh"}, "fit": 0},
  {"slug": "graphic-tee", "cat": "ao-thun", "name": "Graphic Tee", "type": "apparel", "sport": "lifestyle", "category": "Áo thun", "gender": "unisex", "price": 550000, "salePrice": 390000, "flag": "Sale", "rank": 49, "rating": 4.7, "reviews": 18, "colors": [{"name": "Đen", "family": "black"}], "img": "1618354691373-d851c5c3a990", "gallery": ["1645557194575-4086233e70ff", "1765710307817-db7fd96602d4"], "sizes": {"S": 2, "M": 2, "L": 0, "XL": 0}, "desc": "Áo thun in hình tròn nhỏ ở ngực, form unisex.", "specs": {"Chất liệu": "Cotton 100%, 200 gsm", "Công nghệ": "Thoát ẩm, khô nhanh", "Dáng": "Vừa người", "Giặt": "Máy, nước lạnh"}, "fit": 0.1},
  {"slug": "kids-tee", "cat": "ao-thun", "name": "Kids Tee", "type": "apparel", "sport": "lifestyle", "category": "Áo thun", "gender": "tre-em", "price": 290000, "salePrice": null, "flag": null, "rank": 53, "rating": 4.5, "reviews": 52, "colors": [{"name": "Trắng", "family": "white"}], "img": "1778671394516-8270eac13c42", "gallery": ["1765710307817-db7fd96602d4", "1645557194575-4086233e70ff"], "sizes": {"XS": 2, "S": 0}, "desc": "Áo thun trẻ em vải mềm, đường may phẳng không cấn da.", "specs": {"Chất liệu": "Cotton 100%", "Công nghệ": "Thoát ẩm, khô nhanh", "Dáng": "Vừa người", "Giặt": "Máy, nước lạnh"}, "fit": 0},
  {"slug": "run-tee-white", "cat": "ao-thun", "name": "Run Tee", "type": "apparel", "sport": "chay-bo", "category": "Áo thun", "gender": "nam", "price": 520000, "salePrice": null, "flag": null, "rank": 36, "rating": 4.7, "reviews": 103, "colors": [{"name": "Trắng", "family": "white"}], "img": "1521572163474-6864f9cf17ab", "gallery": ["1452626038306-9aae5e071dd3", "1553012547-284d8c582b1f"], "sizes": {"S": 8, "M": 8, "L": 2, "XL": 1}, "desc": "Áo chạy cơ bản, vải thoát ẩm, đường may phẳng chống cọ xát.", "specs": {"Chất liệu": "Polyester 88%, spandex 12%", "Công nghệ": "Thoát ẩm, khô nhanh", "Dáng": "Vừa người", "Giặt": "Máy, nước lạnh"}, "fit": 0.2},
  {"slug": "race-singlet", "cat": "ao-ba-lo", "name": "Race Singlet", "type": "apparel", "sport": "chay-bo", "category": "Áo ba lỗ", "gender": "nam", "price": 520000, "salePrice": null, "flag": "Mới", "rank": 40, "rating": 4.8, "reviews": 18, "colors": [{"name": "Xanh ngọc", "family": "green"}], "img": "1590545495809-90d4c3a8d571", "gallery": ["1553012547-284d8c582b1f", "1452626038306-9aae5e071dd3"], "sizes": {"S": 0, "M": 2, "L": 8, "XL": 8}, "desc": "Áo ba lỗ thi đấu, khoét nách rộng, có vòng cài số đeo.", "specs": {"Chất liệu": "Polyester lưới 100%", "Công nghệ": "Thoát ẩm, khô nhanh", "Dáng": "Vừa người", "Giặt": "Máy, nước lạnh"}, "fit": 0.2},
  {"slug": "stride-short", "cat": "quan-short", "name": "Stride Short 5\"", "type": "apparel", "sport": "chay-bo", "category": "Quần short", "gender": "nam", "price": 590000, "salePrice": null, "flag": null, "rank": 44, "rating": 4.1, "reviews": 141, "colors": [{"name": "Đỏ", "family": "pink"}], "img": "1789110519851-2c2ceaa1bc6e", "gallery": ["1452626038306-9aae5e071dd3", "1590333748338-d629e4564ad9"], "sizes": {"S": 1, "M": 1, "L": 3, "XL": 0}, "desc": "Quần short chạy 5 inch có lót trong, túi sau đựng chìa khoá.", "specs": {"Chất liệu": "Polyester 100%", "Công nghệ": "Thoát ẩm, khô nhanh", "Dáng": "Vừa người", "Giặt": "Máy, nước lạnh"}, "fit": 0.2},
  {"slug": "stride-short-green", "cat": "quan-short", "name": "Stride Short 5\" xanh rêu", "type": "apparel", "sport": "tap-luyen", "category": "Quần short", "gender": "nam", "price": 590000, "salePrice": 450000, "flag": "Sale", "rank": 48, "rating": 4.2, "reviews": 103, "colors": [{"name": "Xanh rêu", "family": "green"}], "img": "1789110519919-ac034e2a0986", "gallery": ["1599058917212-d750089bc07e", "1758875570600-8daf8d2f05f3"], "sizes": {"S": 0, "M": 1, "L": 8, "XL": 8}, "desc": "Short tập đa năng, lưng thun dây rút, vải co giãn 4 chiều.", "specs": {"Chất liệu": "Polyester 90%, spandex 10%", "Công nghệ": "Thoát ẩm, khô nhanh", "Dáng": "Vừa người", "Giặt": "Máy, nước lạnh"}, "fit": 0.2},
  {"slug": "flow-short", "cat": "quan-short", "name": "Flow Short nữ", "type": "apparel", "sport": "tap-luyen", "category": "Quần short", "gender": "nu", "price": 550000, "salePrice": null, "flag": null, "rank": 52, "rating": 4.5, "reviews": 37, "colors": [{"name": "Hồng", "family": "pink"}], "img": "1789110519868-a11366e894ca", "gallery": ["1758875570600-8daf8d2f05f3", "1599058917212-d750089bc07e"], "sizes": {"XS": 5, "S": 2, "M": 3, "L": 2}, "desc": "Short nữ lưng cao, nhẹ như không, hợp yoga và chạy nhẹ.", "specs": {"Chất liệu": "Polyester 92%, spandex 8%", "Công nghệ": "Thoát ẩm, khô nhanh", "Dáng": "Vừa người", "Giặt": "Máy, nước lạnh"}, "fit": 0.1},
  {"slug": "core-tight", "cat": "quan-legging", "name": "Core Tight", "type": "apparel", "sport": "tap-luyen", "category": "Quần legging", "gender": "nu", "price": 890000, "salePrice": null, "flag": "Bán chạy", "rank": 56, "rating": 4.8, "reviews": 18, "colors": [{"name": "Đen", "family": "black"}], "img": "1789110853564-23a6b1d206e7", "gallery": ["1599058917212-d750089bc07e"], "sizes": {"XS": 5, "S": 12, "M": 1, "L": 3}, "desc": "Legging lưng cao nâng đỡ, vải dày không lộ khi squat.", "specs": {"Chất liệu": "Nylon 78%, spandex 22%", "Công nghệ": "Thoát ẩm, khô nhanh", "Dáng": "Vừa người", "Giặt": "Máy, nước lạnh"}, "fit": 0.1},
  {"slug": "club-hoodie", "cat": "hoodie", "name": "Club Hoodie", "type": "apparel", "sport": "lifestyle", "category": "Hoodie & áo nỉ", "gender": "unisex", "price": 1190000, "salePrice": null, "flag": "Mới", "rank": 51, "rating": 4.2, "reviews": 24, "colors": [{"name": "Trắng ngà", "family": "white"}], "img": "1620799140188-3b2a02fd9a77", "gallery": ["1765710307817-db7fd96602d4", "1645557194575-4086233e70ff"], "sizes": {"S": 0, "M": 2, "L": 2, "XL": 12}, "desc": "Hoodie nỉ bông 380 gsm, mũ hai lớp, túi kangaroo.", "specs": {"Chất liệu": "Cotton 80%, polyester 20%", "Công nghệ": "Thoát ẩm, khô nhanh", "Dáng": "Vừa người", "Giặt": "Máy, nước lạnh"}, "fit": -0.3},
  {"slug": "club-hoodie-grey", "cat": "hoodie", "name": "Club Hoodie xám", "type": "apparel", "sport": "lifestyle", "category": "Hoodie & áo nỉ", "gender": "nam", "price": 1190000, "salePrice": 890000, "flag": "Sale", "rank": 55, "rating": 4.8, "reviews": 37, "colors": [{"name": "Xám", "family": "grey"}], "img": "1556821840-3a63f95609a7", "gallery": ["1645557194575-4086233e70ff", "1765710307817-db7fd96602d4"], "sizes": {"S": 0, "M": 8, "L": 3, "XL": 0}, "desc": "Hoodie xám cổ điển cho buổi chạy sáng se lạnh hay đi máy bay.", "specs": {"Chất liệu": "Cotton 80%, polyester 20%", "Công nghệ": "Thoát ẩm, khô nhanh", "Dáng": "Vừa người", "Giặt": "Máy, nước lạnh"}, "fit": 0.1},
  {"slug": "studio-crew", "cat": "hoodie", "name": "Studio Crew", "type": "apparel", "sport": "lifestyle", "category": "Hoodie & áo nỉ", "gender": "unisex", "price": 990000, "salePrice": null, "flag": null, "rank": 59, "rating": 4.1, "reviews": 18, "colors": [{"name": "Đen", "family": "black"}], "img": "1499971856191-1a420a42b498", "gallery": ["1765710307817-db7fd96602d4", "1645557194575-4086233e70ff"], "sizes": {"S": 3, "M": 12, "L": 0, "XL": 8}, "desc": "Áo nỉ cổ tròn dáng rộng, bo tay dày.", "specs": {"Chất liệu": "Cotton 70%, polyester 30%", "Công nghệ": "Thoát ẩm, khô nhanh", "Dáng": "Vừa người", "Giặt": "Máy, nước lạnh"}, "fit": 0.1},
  {"slug": "wind-bomber", "cat": "ao-khoac", "name": "Wind Bomber", "type": "apparel", "sport": "lifestyle", "category": "Áo khoác", "gender": "nam", "price": 1490000, "salePrice": null, "flag": null, "rank": 63, "rating": 4.8, "reviews": 12, "colors": [{"name": "Nâu gỉ sét", "family": "brown"}], "img": "1591047139829-d91aecb6caea", "gallery": ["1645557194575-4086233e70ff", "1765710307817-db7fd96602d4"], "sizes": {"S": 5, "M": 0, "L": 5, "XL": 3}, "desc": "Áo khoác bomber chống gió nhẹ, lót lưới thoáng.", "specs": {"Chất liệu": "Nylon 100%", "Công nghệ": "Thoát ẩm, khô nhanh", "Dáng": "Vừa người", "Giặt": "Máy, nước lạnh"}, "fit": 0},
  {"slug": "retro-windbreaker", "cat": "ao-khoac", "name": "Retro Windbreaker", "type": "apparel", "sport": "chay-bo", "category": "Áo khoác", "gender": "unisex", "price": 1390000, "salePrice": null, "flag": "Mới", "rank": 67, "rating": 4.6, "reviews": 6, "colors": [{"name": "Phối màu", "family": "blue"}], "img": "1571867424485-369464ed33cc", "gallery": ["1553012547-284d8c582b1f", "1452626038306-9aae5e071dd3"], "sizes": {"S": 1, "M": 3, "L": 0, "XL": 3}, "desc": "Áo gió phối màu thập niên 90, gấp gọn vào túi ngực.", "specs": {"Chất liệu": "Nylon ripstop 100%", "Công nghệ": "Thoát ẩm, khô nhanh", "Dáng": "Vừa người", "Giặt": "Máy, nước lạnh"}, "fit": 0.2},
  {"slug": "track-jacket", "cat": "ao-khoac", "name": "Track Jacket", "type": "apparel", "sport": "tap-luyen", "category": "Áo khoác", "gender": "nam", "price": 1290000, "salePrice": null, "flag": null, "rank": 50, "rating": 4.6, "reviews": 141, "colors": [{"name": "Xanh navy", "family": "blue"}], "img": "1780396508935-94e234affc92", "gallery": ["1599058917212-d750089bc07e"], "sizes": {"S": 8, "M": 3, "L": 1, "XL": 1}, "desc": "Áo khoác thể thao khoá kéo toàn thân, vải tricot bóng nhẹ.", "specs": {"Chất liệu": "Polyester 100%", "Công nghệ": "Thoát ẩm, khô nhanh", "Dáng": "Vừa người", "Giặt": "Máy, nước lạnh"}, "fit": -0.1},
  {"slug": "run-crew-socks", "cat": "tat", "name": "Run Crew Socks, gói 3 đôi", "type": "accessories", "sport": "chay-bo", "category": "Tất", "gender": "unisex", "price": 290000, "salePrice": null, "flag": "Bán chạy", "rank": 54, "rating": 4.1, "reviews": 141, "colors": [{"name": "Phối màu", "family": "grey"}], "img": "1615486364462-ef6363adbc18", "gallery": ["1590333748338-d629e4564ad9", "1452626038306-9aae5e071dd3"], "sizes": {"S": 2, "M": 2, "L": 1}, "desc": "Tất chạy cổ vừa, đệm gót và mũi, sợi thoát ẩm chống phồng rộp.", "specs": {"Chất liệu": "Polyester 75%, nylon 22%, spandex 3%", "Kích thước": "Một size", "Bảo hành": "30 ngày"}, "fit": 0},
  {"slug": "rib-socks", "cat": "tat", "name": "Rib Socks", "type": "accessories", "sport": "lifestyle", "category": "Tất", "gender": "unisex", "price": 190000, "salePrice": null, "flag": null, "rank": 58, "rating": 4.6, "reviews": 76, "colors": [{"name": "Xanh rêu", "family": "green"}], "img": "1640026199235-c24aa417b552", "gallery": ["1645557194575-4086233e70ff", "1765710307817-db7fd96602d4"], "sizes": {"S": 8, "M": 5, "L": 3}, "desc": "Tất gân cổ cao, cotton dày, màu xanh rêu.", "specs": {"Chất liệu": "Cotton 80%, nylon 18%, spandex 2%", "Kích thước": "Một size", "Bảo hành": "30 ngày"}, "fit": 0},
  {"slug": "run-cap", "cat": "mu", "name": "Run Cap", "type": "accessories", "sport": "chay-bo", "category": "Mũ", "gender": "unisex", "price": 350000, "salePrice": null, "flag": null, "rank": 62, "rating": 4.7, "reviews": 24, "colors": [{"name": "Vàng", "family": "brown"}], "img": "1645266729222-17cd32e06fd0", "gallery": ["1553012547-284d8c582b1f", "1452626038306-9aae5e071dd3"], "sizes": {"OS": 12}, "desc": "Mũ chạy vải lưới nhẹ, vành mềm gập được, khoá dán sau.", "specs": {"Chất liệu": "Polyester 100%", "Kích thước": "Một size", "Bảo hành": "30 ngày"}, "fit": 0},
  {"slug": "trucker-cap", "cat": "mu", "name": "Trucker Cap", "type": "accessories", "sport": "lifestyle", "category": "Mũ", "gender": "unisex", "price": 390000, "salePrice": null, "flag": "Mới", "rank": 66, "rating": 4.7, "reviews": 18, "colors": [{"name": "Trắng", "family": "white"}], "img": "1588850561407-ed78c282e89b", "gallery": ["1645557194575-4086233e70ff", "1765710307817-db7fd96602d4"], "sizes": {"OS": 3}, "desc": "Mũ lưỡi trai trắng, thân lưới sau thoáng khí.", "specs": {"Chất liệu": "Cotton / polyester", "Kích thước": "Một size", "Bảo hành": "30 ngày"}, "fit": 0},
  {"slug": "washed-cap", "cat": "mu", "name": "Washed Cap", "type": "accessories", "sport": "lifestyle", "category": "Mũ", "gender": "unisex", "price": 350000, "salePrice": 250000, "flag": "Sale", "rank": 70, "rating": 4.9, "reviews": 18, "colors": [{"name": "Xám than", "family": "grey"}], "img": "1521369909029-2afed882baee", "gallery": ["1765710307817-db7fd96602d4", "1645557194575-4086233e70ff"], "sizes": {"OS": 3}, "desc": "Mũ cotton wash bạc màu, đội lâu không bí.", "specs": {"Chất liệu": "Cotton 100%", "Kích thước": "Một size", "Bảo hành": "30 ngày"}, "fit": 0},
  {"slug": "commuter-pack", "cat": "balo", "name": "Commuter Pack 20L", "type": "accessories", "sport": "lifestyle", "category": "Balo", "gender": "unisex", "price": 1290000, "salePrice": null, "flag": null, "rank": 74, "rating": 4.6, "reviews": 37, "colors": [{"name": "Đen", "family": "black"}], "img": "1680039211156-66c721b87625", "gallery": ["1645557194575-4086233e70ff", "1765710307817-db7fd96602d4"], "sizes": {"OS": 1}, "desc": "Balo 20 lít có ngăn laptop 15\", chống nước nhẹ, quai đeo êm.", "specs": {"Chất liệu": "Polyester phủ PU", "Kích thước": "Một size", "Bảo hành": "30 ngày"}, "fit": 0},
  {"slug": "thermo-bottle", "cat": "binh-nuoc", "name": "Thermo Bottle 750 ml", "type": "accessories", "sport": "tap-luyen", "category": "Bình nước", "gender": "unisex", "price": 450000, "salePrice": null, "flag": "Bán chạy", "rank": 57, "rating": 4.2, "reviews": 76, "colors": [{"name": "Xanh rêu", "family": "green"}], "img": "1602143407151-7111542de6e8", "gallery": ["1599058917212-d750089bc07e", "1758875570600-8daf8d2f05f3"], "sizes": {"OS": 12}, "desc": "Bình giữ lạnh 24 giờ, thép không gỉ hai lớp, nắp chống tràn.", "specs": {"Chất liệu": "Thép không gỉ 304", "Kích thước": "Một size", "Bảo hành": "30 ngày"}, "fit": 0},
  {"slug": "grip-gloves", "cat": "gang-tay", "name": "Grip Gloves", "type": "accessories", "sport": "tap-luyen", "category": "Găng tay tập", "gender": "unisex", "price": 350000, "salePrice": null, "flag": null, "rank": 69, "rating": 4.9, "reviews": 141, "colors": [{"name": "Đen", "family": "black"}], "img": "1576722938199-5ff2809e8160", "gallery": ["1599058917212-d750089bc07e", "1758875570600-8daf8d2f05f3"], "sizes": {"S": 1, "M": 3, "L": 2}, "desc": "Găng tay tập tạ hở ngón, lòng bàn tay chống trượt.", "specs": {"Chất liệu": "Da tổng hợp, neoprene", "Kích thước": "Một size", "Bảo hành": "30 ngày"}, "fit": 0},
  {"slug": "kids-short", "cat": "quan-short", "name": "Play Short trẻ em", "type": "apparel", "sport": "lifestyle", "category": "Quần short", "gender": "tre-em", "price": 290000, "salePrice": 220000, "flag": "Sale", "rank": 66, "rating": 4.4, "reviews": 9, "colors": [{"name": "Hồng", "family": "pink"}], "img": "1789110519868-a11366e894ca", "gallery": ["1777489689168-497290f235d5"], "sizes": {"XS": 6, "S": 4}, "desc": "Short thun mềm lưng chun, có túi, cho trẻ chạy nhảy cả ngày.", "specs": {"Chất liệu": "Cotton 95%, spandex 5%", "Công nghệ": "Thoát ẩm, khô nhanh", "Dáng": "Vừa người", "Giặt": "Máy, nước lạnh"}, "fit": 0},
  {"slug": "kids-hoodie", "cat": "hoodie", "name": "Club Hoodie trẻ em", "type": "apparel", "sport": "lifestyle", "category": "Hoodie & áo nỉ", "gender": "tre-em", "price": 690000, "salePrice": null, "flag": null, "rank": 68, "rating": 4.7, "reviews": 11, "colors": [{"name": "Trắng ngà", "family": "white"}], "img": "1620799140188-3b2a02fd9a77", "gallery": ["1777489689168-497290f235d5"], "sizes": {"XS": 3, "S": 5}, "desc": "Hoodie nỉ bông cho trẻ, mũ một lớp an toàn, không dây rút.", "specs": {"Chất liệu": "Cotton 80%, polyester 20%", "Công nghệ": "Nỉ bông mềm", "Dáng": "Rộng vừa", "Giặt": "Máy, nước lạnh"}, "fit": 0}
];

CAD.labels = {
  gender: { nam: 'Nam', nu: 'Nữ', unisex: 'Unisex', 'tre-em': 'Trẻ em' },
  sport: { 'chay-bo': 'Chạy bộ', trail: 'Trail', lifestyle: 'Mặc hằng ngày', 'tap-luyen': 'Tập luyện', 'bong-da': 'Bóng đá' },
  type: { shoes: 'Giày', apparel: 'Quần áo', accessories: 'Phụ kiện' },
  /* Danh mục (category) theo thứ tự hiển thị trong bộ lọc */
  category: {
    'giay-chay-bo': 'Giày chạy bộ', 'giay-trail': 'Giày chạy địa hình', 'giay-tap': 'Giày tập luyện',
    'giay-lifestyle': 'Giày lifestyle', 'giay-bong-da': 'Giày bóng đá',
    'ao-thun': 'Áo thun & áo tập', 'ao-ba-lo': 'Áo ba lỗ', 'ao-bra': 'Áo bra thể thao', 'quan-short': 'Quần short',
    'quan-legging': 'Quần legging', 'hoodie': 'Hoodie & áo nỉ', 'ao-khoac': 'Áo khoác',
    'tat': 'Tất', 'mu': 'Mũ & băng đô', 'balo': 'Balo & túi', 'binh-nuoc': 'Bình nước', 'bong': 'Bóng', 'gang-tay': 'Găng tay tập'
  }
};

CAD.formatPrice = function (vnd) { return new Intl.NumberFormat('vi-VN').format(vnd) + ' ₫'; };
CAD.unitPrice = function (p) { return p.salePrice || p.price; };

/* ---------- Tìm kiếm: không phân biệt dấu tiếng Việt + từ đồng nghĩa ----------
   Ở hệ thống thật việc này do search engine (OpenSearch/Meilisearch) làm với bộ lọc ASCII folding. */
CAD.norm = function (s) {
  return (s || '').toLowerCase().normalize('NFD').replace(/[̀-ͯ]/g, '').replace(/đ/g, 'd').trim();
};
CAD.SYNONYMS = {
  sneaker: 'giay', sneakers: 'giay', shoe: 'giay', shoes: 'giay', 'giay the thao': 'giay',
  tee: 'ao thun', 'ao phong': 'ao thun', tshirt: 'ao thun', 't-shirt': 'ao thun', hoodie: 'ao ni', sweater: 'ao ni',
  running: 'chay bo', run: 'chay', gym: 'tap luyen', training: 'tap luyen', women: 'nu', men: 'nam'
};
CAD.search = function (query) {
  var q = CAD.norm(query);
  if (!q) return [];
  Object.keys(CAD.SYNONYMS).forEach(function (k) { if ((' ' + q + ' ').indexOf(' ' + k + ' ') > -1) q = (' ' + q + ' ').replace(' ' + k + ' ', ' ' + CAD.SYNONYMS[k] + ' ').trim(); });
  var tokens = q.split(/\s+/).filter(Boolean);
  return CAD.products.map(function (p) {
    var name = CAD.norm(p.name);
    var hay = CAD.norm([p.name, p.category, CAD.labels.sport[p.sport], CAD.labels.gender[p.gender], CAD.labels.type[p.type],
      p.colors.map(function (c) { return c.name; }).join(' ')].join(' '));
    /* Mỗi từ khoá phải khớp ĐẦU một từ (tránh "ni" khớp "organic") */
    var words = hay.split(/[^a-z0-9]+/);
    if (!tokens.every(function (t) { return words.some(function (w) { return w.indexOf(t) === 0; }); })) return null;
    var score = (name.indexOf(tokens[0]) === 0 ? 3 : 0) + (name.indexOf(q) > -1 ? 2 : 0) + (p.flag === 'Mới' ? 0.5 : 0) - p.rank / 100;
    return { p: p, score: score };
  }).filter(Boolean).sort(function (a, b) { return b.score - a.score; }).map(function (r) { return r.p; });
};
/* Tô đậm phần khớp, không phân biệt dấu (mỗi ký tự tiếng Việt chuẩn hoá thành đúng 1 ký tự) */
CAD.highlight = function (text, query) {
  var q = CAD.norm(query);
  if (!q) return text;
  var n = Array.prototype.map.call(text, function (ch) { return CAD.norm(ch) || ch; }).join('');
  var i = n.indexOf(q);
  if (i < 0 || n.length !== text.length) return text;
  return text.slice(0, i) + '<mark>' + text.slice(i, i + q.length) + '</mark>' + text.slice(i + q.length);
};

/* ---------- Tồn kho dùng chung với trang Admin ----------
   Nếu Admin đã khởi tạo tồn kho, storefront tính "còn hàng" (ATS) giống backend:
   ATS = on_hand − reserved − safety_stock; reserved = hàng của đơn chưa xuất kho. */
CAD.skuKey = function (slug, size) { return slug + '|' + size; };
CAD.reservedMap = function () {
  var map = {};
  var read = function (k) { try { return JSON.parse(localStorage.getItem(k) || '[]'); } catch (e) { return []; } };
  read('cad-orders').concat(read('cad-admin-orders-v3')).forEach(function (o) {
    var open = o.status !== 'CANCELLED' && o.payment && o.payment.status !== 'EXPIRED' &&
      (!o.fulfillment || o.fulfillment === 'UNFULFILLED' || o.fulfillment === 'PACKED');
    if (!open) return;
    (o.lines || []).forEach(function (l) { var k = CAD.skuKey(l.slug, l.size); map[k] = (map[k] || 0) + l.qty; });
  });
  return map;
};
(function applyInventory() {
  var inv;
  try { inv = JSON.parse(localStorage.getItem('cad-inventory-v3') || 'null'); } catch (e) { inv = null; }
  if (!inv) return;
  var res = CAD.reservedMap();
  CAD.products.forEach(function (p) {
    Object.keys(p.sizes).forEach(function (s) {
      var row = inv[CAD.skuKey(p.slug, s)];
      if (row) p.sizes[s] = Math.max(0, row.onHand - row.safety - (res[CAD.skuKey(p.slug, s)] || 0));
    });
  });
})();

/* ---------- Thay đổi sản phẩm từ Admin (tên, giá) áp dụng ra storefront ----------
   Nhờ vậy có thể thử: đổi giá trong Admin → giỏ hàng báo "Giá đã thay đổi". */
(function applyProductMeta() {
  var meta;
  try { meta = JSON.parse(localStorage.getItem('cad-product-meta-v3') || 'null'); } catch (e) { meta = null; }
  if (!meta) return;
  CAD.products.forEach(function (p) {
    var m = meta[p.slug]; if (!m) return;
    if (m.name) p.name = m.name;
    if (m.price) p.price = m.price;
    p.salePrice = m.salePrice || null;
    if (m.desc) p.desc = m.desc;
  });
})();
