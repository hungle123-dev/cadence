/* Logic "phía server" giả lập cho prototype: tính tiền, voucher, phí giao hàng, phân bổ giảm giá, lưu đơn.
   Ở hệ thống thật, mọi phép tính này nằm ở backend (Pricing/Promotion/Checkout) — FE chỉ hiển thị kết quả. */
window.CAD = window.CAD || {};

(function () {
  var FREE_SHIP = 1500000;

  CAD.FREE_SHIP = FREE_SHIP;

  /* Voucher mẫu (dùng để demo trên prototype) */
  CAD.VOUCHERS = {
    CADENCE10: { label: 'Giảm 10%, tối đa 200.000 ₫ cho đơn từ 1.000.000 ₫', type: 'percent', value: 10, max: 200000, min: 1000000 },
    FREESHIP: { label: 'Miễn phí giao hàng', type: 'shipping' },
    RUN150: { label: 'Giảm 150.000 ₫ cho đơn từ 2.000.000 ₫', type: 'fixed', value: 150000, min: 2000000 }
  };

  /* Phương thức giao hàng (phí giả lập; GĐ2 lấy từ API hãng vận chuyển) */
  CAD.SHIPPING = [
    { id: 'standard', name: 'Tiêu chuẩn', eta: '2–4 ngày', fee: 30000, freeOver: FREE_SHIP },
    { id: 'express', name: 'Nhanh', eta: '1–2 ngày', fee: 45000 },
    { id: 'sameday', name: 'Hoả tốc trong ngày', eta: 'Trong 4 giờ, đặt trước 14:00', fee: 60000, cities: ['TP. Hồ Chí Minh', 'Hà Nội'] }
  ];

  CAD.lines = function (items) {
    return items.map(function (i) {
      var p = CAD.find(i.slug);
      if (!p) return null;
      return { item: i, product: p, unit: CAD.unitPrice(p), list: p.price, total: CAD.unitPrice(p) * i.qty, stock: p.sizes[i.size] || 0 };
    }).filter(Boolean);
  };

  /* Kiểm tra voucher: trả về { ok, error } */
  CAD.checkVoucher = function (code, subtotal) {
    var v = CAD.VOUCHERS[(code || '').trim().toUpperCase()];
    if (!v) return { ok: false, error: 'Mã không tồn tại hoặc đã hết hạn.' };
    if (v.min && subtotal < v.min) return { ok: false, error: 'Mã áp dụng cho đơn từ ' + CAD.formatPrice(v.min) + '. Bạn cần mua thêm ' + CAD.formatPrice(v.min - subtotal) + '.' };
    return { ok: true, voucher: v };
  };

  /* Tính tổng đơn theo đúng thứ tự: dòng → giảm cấp đơn → phí ship → giảm phí ship.
     Giảm cấp đơn được phân bổ xuống từng dòng (largest remainder) để tổng khớp tới từng đồng. */
  CAD.totals = function (items, opts) {
    opts = opts || {};
    var lines = CAD.lines(items);
    var subtotal = lines.reduce(function (s, l) { return s + l.total; }, 0);
    var savings = lines.reduce(function (s, l) { return s + (l.list - l.unit) * l.item.qty; }, 0);
    var discount = 0, voucher = null, voucherError = null;
    if (opts.voucher) {
      var chk = CAD.checkVoucher(opts.voucher, subtotal);
      if (chk.ok) {
        voucher = chk.voucher;
        if (voucher.type === 'percent') discount = Math.min(Math.floor(subtotal * voucher.value / 100), voucher.max);
        if (voucher.type === 'fixed') discount = Math.min(voucher.value, subtotal);
      } else voucherError = chk.error;
    }
    /* Phân bổ */
    if (discount > 0) {
      var raw = lines.map(function (l) { return discount * l.total / subtotal; });
      var floor = raw.map(Math.floor);
      var rest = discount - floor.reduce(function (a, b) { return a + b; }, 0);
      raw.map(function (r, i) { return { i: i, frac: r - floor[i] }; })
        .sort(function (a, b) { return b.frac - a.frac; })
        .slice(0, rest).forEach(function (x) { floor[x.i] += 1; });
      lines.forEach(function (l, i) { l.allocated = floor[i]; });
    }
    var method = CAD.SHIPPING.filter(function (m) { return m.id === (opts.shipping || 'standard'); })[0] || CAD.SHIPPING[0];
    var shippingFee = lines.length ? method.fee : 0;
    var shippingDiscount = 0;
    if (method.freeOver && subtotal >= method.freeOver) shippingDiscount = shippingFee;
    if (voucher && voucher.type === 'shipping') shippingDiscount = shippingFee;
    var total = subtotal - discount + shippingFee - shippingDiscount;
    return {
      lines: lines, subtotal: subtotal, savings: savings, discount: discount, voucher: voucher, voucherError: voucherError,
      shipping: method, shippingFee: shippingFee, shippingDiscount: shippingDiscount, total: total,
      vat: Math.round(total - total / 1.1), /* giá đã gồm VAT 10% */
      count: lines.reduce(function (s, l) { return s + l.item.qty; }, 0)
    };
  };

  /* ---------- Đơn hàng (snapshot) ---------- */
  CAD.newOrderNumber = function () {
    var d = new Date();
    return 'CAD-' + String(d.getFullYear()).slice(2) + ('0' + (d.getMonth() + 1)).slice(-2) + ('0' + d.getDate()).slice(-2) + '-' + Math.floor(1000 + Math.random() * 9000);
  };
  CAD.saveOrder = function (order) {
    var all = CAD.store.get('cad-orders', []);
    all = all.filter(function (o) { return o.number !== order.number; });
    all.unshift(order);
    CAD.store.set('cad-orders', all.slice(0, 20));
  };
  CAD.getOrder = function (number) {
    return CAD.store.get('cad-orders', []).filter(function (o) { return o.number === number; })[0];
  };

  /* ---------- Địa giới hành chính VN sau sắp xếp 01/07/2025: 34 tỉnh/thành ---------- */
  CAD.PROVINCES = ['TP. Hồ Chí Minh', 'Hà Nội', 'Hải Phòng', 'Đà Nẵng', 'Huế', 'Cần Thơ',
    'An Giang', 'Bắc Ninh', 'Cà Mau', 'Cao Bằng', 'Đắk Lắk', 'Điện Biên', 'Đồng Nai', 'Đồng Tháp', 'Gia Lai',
    'Hà Tĩnh', 'Hưng Yên', 'Khánh Hòa', 'Lai Châu', 'Lâm Đồng', 'Lạng Sơn', 'Lào Cai', 'Nghệ An', 'Ninh Bình',
    'Phú Thọ', 'Quảng Ngãi', 'Quảng Ninh', 'Quảng Trị', 'Sơn La', 'Tây Ninh', 'Thái Nguyên', 'Thanh Hóa',
    'Tuyên Quang', 'Vĩnh Long'];
  /* Gợi ý phường/xã mẫu (hệ thống thật lấy từ API danh mục hành chính) */
  CAD.WARDS_SAMPLE = {
    'TP. Hồ Chí Minh': ['Phường Sài Gòn', 'Phường Bến Thành', 'Phường Tân Định', 'Phường Bàn Cờ', 'Phường Xuân Hòa', 'Phường Chợ Lớn', 'Phường Thủ Đức'],
    'Hà Nội': ['Phường Hoàn Kiếm', 'Phường Cửa Nam', 'Phường Ba Đình', 'Phường Giảng Võ', 'Phường Đống Đa', 'Phường Cầu Giấy']
  };
})();
