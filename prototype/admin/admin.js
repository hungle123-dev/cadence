/* CADENCE Admin — khung giao diện, dữ liệu mẫu, tồn kho, phân quyền.
   Mọi "backend" ở đây là giả lập bằng localStorage để prototype chạy độc lập. */
(function () {
  var $ = function (s, r) { return (r || document).querySelector(s); };
  var A = window.ADM = {};
  CAD.FREE_SHIP = CAD.FREE_SHIP || 1500000;

  /* =================== Phân quyền (RBAC) =================== */
  A.ROLES = {
    staff: { name: 'Linh Trần', title: 'Vận hành', initials: 'LT', can: { editPrice: false, refundOver: 2000000, adjustStock: true } },
    admin: { name: 'Minh Phạm', title: 'Quản trị', initials: 'MP', can: { editPrice: true, refundOver: Infinity, adjustStock: true } }
  };
  A.role = function () { return CAD.store.get('cad-admin-role', 'staff'); };
  A.user = function () { return A.ROLES[A.role()]; };
  A.can = function (k) { return A.user().can[k]; };

  /* =================== Khung trang =================== */
  var NAV = [
    ['index.html', 'Tổng quan', 'dash', 'a-home'], ['orders.html', 'Đơn hàng', 'orders', 'a-receipt'], ['products.html', 'Sản phẩm', 'products', 'a-tag'],
    ['inventory.html', 'Tồn kho', 'inventory', 'a-stack']
  ];
  var SOON = [['Khuyến mãi', 'a-percent'], ['Đổi trả', 'i-return'], ['Khách hàng', 'a-users'], ['Đối soát COD', 'a-cash']];

  /* Icon riêng của admin: cùng nét 1.5 với sprite storefront */
  var SPRITE = '<svg xmlns="http://www.w3.org/2000/svg" style="display:none" aria-hidden="true">' +
    '<symbol id="a-home" viewBox="0 0 24 24"><path d="M4 10.5 12 4l8 6.5V20h-5.5v-5.5h-5V20H4z"/></symbol>' +
    '<symbol id="a-receipt" viewBox="0 0 24 24"><path d="M6 3.5h12v17l-2-1.4-2 1.4-2-1.4-2 1.4-2-1.4-2 1.4z"/><path d="M9 8.5h6M9 12h6M9 15.5h3"/></symbol>' +
    '<symbol id="a-tag" viewBox="0 0 24 24"><path d="M3.5 12.3V4.5a1 1 0 0 1 1-1h7.8l8.2 8.2a1 1 0 0 1 0 1.4l-7.4 7.4a1 1 0 0 1-1.4 0z"/><circle cx="8" cy="8" r="1.4"/></symbol>' +
    '<symbol id="a-stack" viewBox="0 0 24 24"><path d="M3.5 8 12 4l8.5 4-8.5 4z"/><path d="m3.5 12 8.5 4 8.5-4M3.5 16l8.5 4 8.5-4"/></symbol>' +
    '<symbol id="a-percent" viewBox="0 0 24 24"><path d="M18 6 6 18"/><circle cx="7.5" cy="7.5" r="2"/><circle cx="16.5" cy="16.5" r="2"/></symbol>' +
    '<symbol id="a-users" viewBox="0 0 24 24"><circle cx="9" cy="8.5" r="3"/><path d="M3.5 19c0-3.2 2.4-5 5.5-5s5.5 1.8 5.5 5"/><path d="M15.5 5.8a3 3 0 0 1 0 5.4M17.5 14.3c1.9.6 3 2.1 3 4.7"/></symbol>' +
    '<symbol id="a-cash" viewBox="0 0 24 24"><rect x="3" y="6.5" width="18" height="11" rx="1.5"/><circle cx="12" cy="12" r="2.4"/><path d="M6.5 9.5v5M17.5 9.5v5"/></symbol>' +
    '<symbol id="a-plus" viewBox="0 0 24 24"><path d="M12 5v14M5 12h14"/></symbol>' +
    '<symbol id="a-up" viewBox="0 0 24 24"><path d="M12 19V5M6 11l6-6 6 6"/></symbol>' +
    '<symbol id="a-down" viewBox="0 0 24 24"><path d="M12 5v14M6 13l6 6 6-6"/></symbol>' +
    '<symbol id="a-external" viewBox="0 0 24 24"><path d="M14 4.5h5.5V10M19.5 4.5 11 13"/><path d="M18 14v5.5H4.5V6H10"/></symbol>' +
    '<symbol id="a-image" viewBox="0 0 24 24"><rect x="3.5" y="4.5" width="17" height="15" rx="1.5"/><circle cx="9" cy="10" r="1.8"/><path d="m3.5 17 5-4.5 4 3.5 3-2.5 5 4"/></symbol>' +
    '</svg>';

  /* Bảng cuộn ngang: bóng cạnh cột dính khi có nội dung trượt bên dưới */
  function syncWrap(w) {
    var max = w.scrollWidth - w.clientWidth;
    w.classList.toggle('is-scrolled-x', w.scrollLeft > 1);
    w.classList.toggle('has-more-end', max - w.scrollLeft > 1);
  }
  var raf = 0;
  function syncAll() { cancelAnimationFrame(raf); raf = requestAnimationFrame(function () { document.querySelectorAll('.table-wrap').forEach(syncWrap); }); }
  A.syncTables = syncAll;

  A.shell = function (active) {
    var pending = A.orders().filter(function (o) { return o.status === 'PENDING_CONFIRMATION'; }).length;
    var u = A.user();
    var ico = function (id) { return '<svg class="icon" aria-hidden="true"><use href="#' + id + '"/></svg>'; };
    var html = SPRITE +
      '<a class="skip-link" href="#content">Bỏ qua tới nội dung chính</a>' +
      '<div class="shell">' +
        '<aside class="side" id="side" aria-label="Điều hướng quản trị">' +
          '<div class="side__brand"><a class="wordmark" href="index.html">Cadence</a><span class="side__tag">Admin</span>' +
            '<button class="icon-btn side__close" type="button" id="side-close" aria-label="Đóng menu"><svg class="icon icon--md" aria-hidden="true"><use href="#i-x"/></svg></button></div>' +
          '<nav class="side__nav" aria-label="Chức năng">' + NAV.map(function (n) {
            return '<a href="' + n[0] + '"' + (n[2] === active ? ' aria-current="page"' : '') + '>' + ico(n[3]) + '<span>' + n[1] + '</span>' +
              (n[2] === 'orders' && pending ? '<span class="n" aria-label="' + pending + ' đơn chờ xác nhận">' + pending + '</span>' : '') + '</a>';
          }).join('') +
          '<span class="side__label">Giai đoạn 2</span>' +
          SOON.map(function (s) { return '<a class="is-soon" aria-disabled="true">' + ico(s[1]) + '<span>' + s[0] + '</span><span class="side__soon">Sắp có</span></a>'; }).join('') +
          '</nav>' +
          '<div class="side__foot"><a href="../index.html"><svg class="icon icon--sm" aria-hidden="true"><use href="#i-arrow"/></svg>Về cửa hàng</a><span>Prototype · dữ liệu mẫu</span></div>' +
        '</aside>' +
        '<div class="main">' +
          '<header class="top">' +
            '<button class="top__menu" type="button" id="menu-btn" aria-label="Mở menu" aria-controls="side" aria-expanded="false"><svg class="icon" aria-hidden="true"><use href="#i-menu"/></svg></button>' +
            '<form class="top__search" role="search" action="orders.html"><svg class="icon icon--sm" aria-hidden="true"><use href="#i-search"/></svg>' +
              '<label class="sr-only" for="top-q">Tìm đơn hàng</label><input id="top-q" name="q" placeholder="Tìm mã đơn, số điện thoại, tên khách…" autocomplete="off"></form>' +
            '<div class="top__user">' +
              '<label class="sr-only" for="role">Vai trò (mô phỏng phân quyền)</label>' +
              '<select id="role" title="Prototype: đổi vai trò để xem phân quyền"><option value="staff"' + (A.role() === 'staff' ? ' selected' : '') + '>Vai trò: Vận hành</option><option value="admin"' + (A.role() === 'admin' ? ' selected' : '') + '>Vai trò: Quản trị</option></select>' +
              '<span class="avatar" aria-hidden="true">' + u.initials + '</span><span class="hide-sm">' + u.name + '</span>' +
            '</div>' +
          '</header>' +
          '<main class="content" id="content"></main>' +
        '</div>' +
      '</div>' +
      '<div class="overlay" id="side-overlay" hidden></div>' +
      '<div class="toast" id="toast" role="status" aria-live="polite"></div>';
    document.body.insertAdjacentHTML('beforeend', html);
    $('#role').addEventListener('change', function () { CAD.store.set('cad-admin-role', this.value); location.reload(); });
    var side = $('#side'), ov = $('#side-overlay'), mb = $('#menu-btn');
    function setSide(open) {
      var was = side.classList.contains('is-open');
      side.classList.toggle('is-open', open); ov.hidden = !open; mb.setAttribute('aria-expanded', open);
      if (open) $('#side-close').focus(); else if (was) mb.focus();
    }
    mb.addEventListener('click', function () { setSide(true); });
    ov.addEventListener('click', function () { setSide(false); });
    $('#side-close').addEventListener('click', function () { setSide(false); });
    document.addEventListener('keydown', function (e) { if (e.key === 'Escape') setSide(false); });

    var content = $('#content');
    document.addEventListener('scroll', function (e) { var t = e.target; if (t.classList && t.classList.contains('table-wrap')) syncWrap(t); }, true);
    window.addEventListener('resize', syncAll);
    new MutationObserver(syncAll).observe(content, { childList: true, subtree: true });
    return content;
  };

  /* =================== Dữ liệu mẫu: đơn hàng =================== */
  var seed = 7;
  function rnd() { seed = (seed * 16807) % 2147483647; return (seed - 1) / 2147483646; }
  function pick(a) { return a[Math.floor(rnd() * a.length)]; }
  var NAMES = ['Nguyễn Minh Anh', 'Trần Quốc Bảo', 'Lê Thu Hà', 'Phạm Gia Huy', 'Võ Ngọc Lan', 'Đặng Hoàng Long', 'Bùi Khánh Linh', 'Hồ Đức Thịnh',
    'Ngô Thanh Tâm', 'Dương Mai Phương', 'Lý Tuấn Kiệt', 'Đỗ Bảo Ngọc', 'Huỳnh Nhật Nam', 'Phan Thảo Vy', 'Vũ Đình Khoa', 'Trịnh Hải Yến'];
  var STREETS = ['12 Nguyễn Huệ', '88 Lê Lợi', '5 Pasteur', '210 Điện Biên Phủ', '41 Trần Hưng Đạo', '17 Hàng Bài', '9 Lý Thường Kiệt', '66 Bạch Đằng'];

  function makeSeed() {
    var now = Date.now(), day = 86400000, out = [];
    var plan = [
      ['confirm', 3], ['pack', 4], ['packed', 2], ['shipped', 4], ['delivered', 6], ['delivered-old', 14], ['cancelled', 2], ['pay-stale', 1], ['expired', 1], ['failed', 1]
    ];
    var kinds = [];
    plan.forEach(function (p) { for (var i = 0; i < p[1]; i++) kinds.push(p[0]); });
    kinds.forEach(function (kind, i) {
      var ageDays = { confirm: 0, pack: 1, packed: 1, shipped: 3, 'pay-stale': 0 }[kind];
      if (ageDays === undefined) ageDays = 2 + Math.floor(rnd() * 5);
      /* Đơn đã giao của tuần trước: để so sánh 7 ngày ra con số hợp lý */
      if (kind === 'delivered-old') { ageDays = 7 + Math.floor(rnd() * 6); kind = 'delivered'; }
      var created = now - ageDays * day - Math.floor(rnd() * 9 * 3600000) - 30 * 60000;
      var lines = [];
      var n = 1 + Math.floor(rnd() * 2);
      for (var j = 0; j < n; j++) {
        var p = CAD.products[Math.floor(rnd() * CAD.products.length)];
        var sizes = Object.keys(p.sizes);
        lines.push({ slug: p.slug, name: p.name, img: p.img, color: p.colors[0].name, size: pick(sizes), qty: rnd() > 0.85 ? 2 : 1, unit: CAD.unitPrice(p), allocatedDiscount: 0 });
      }
      var subtotal = lines.reduce(function (s, l) { return s + l.unit * l.qty; }, 0);
      var ship = subtotal >= CAD.FREE_SHIP ? 0 : 30000;
      var name = pick(NAMES);
      var cod = kind === 'confirm' || (kind !== 'pay-stale' && kind !== 'expired' && kind !== 'failed' && rnd() > 0.5);
      var d = new Date(created);
      var o = {
        number: 'CAD-' + String(d.getFullYear()).slice(2) + ('0' + (d.getMonth() + 1)).slice(-2) + ('0' + d.getDate()).slice(-2) + '-' + (1000 + Math.floor(rnd() * 9000)),
        createdAt: created, seeded: true, lines: lines,
        totals: { subtotal: subtotal, discount: 0, shipping: ship, total: subtotal + ship, vat: Math.round((subtotal + ship) - (subtotal + ship) / 1.1) },
        contact: { email: CAD.norm(name).split(' ').slice(-1)[0] + (10 + i) + '@example.com', phone: '09' + String(10000000 + Math.floor(rnd() * 89999999)) },
        address: { name: name, street: pick(STREETS), ward: pick(['Phường Sài Gòn', 'Phường Bến Thành', 'Phường Ba Đình', 'Phường Cửa Nam', 'Phường Hải Châu']), province: pick(['TP. Hồ Chí Minh', 'Hà Nội', 'Đà Nẵng', 'Cần Thơ', 'Hải Phòng']) },
        shipping: { id: 'standard', name: 'Tiêu chuẩn', eta: '2–4 ngày' },
        payment: { method: cod ? 'cod' : 'vnpay', status: cod ? 'UNPAID' : 'PAID' },
        status: 'PLACED', fulfillment: 'UNFULFILLED',
        events: [{ at: created, text: 'Đặt hàng', by: 'Khách hàng' }]
      };
      if (!cod && kind !== 'pay-stale' && kind !== 'expired' && kind !== 'failed') o.events.push({ at: created + 40000, text: 'Thanh toán thành công qua VNPay (IPN)', by: 'Hệ thống' });
      var t = created + 3600000;
      if (kind === 'confirm') { o.status = 'PENDING_CONFIRMATION'; }
      if (cod && kind !== 'confirm') o.events.push({ at: t, text: 'Đã xác nhận đơn qua điện thoại', by: 'Linh Trần' });
      if (['packed', 'shipped', 'delivered'].indexOf(kind) > -1) { o.fulfillment = 'PACKED'; o.events.push({ at: t + 3600000, text: 'Kho đã đóng gói', by: 'Kho HCM' }); }
      if (['shipped', 'delivered'].indexOf(kind) > -1) { o.fulfillment = 'SHIPPED'; o.tracking = 'VN' + (1000000000 + Math.floor(rnd() * 8999999999)); o.events.push({ at: t + 7200000, text: 'Đã giao cho đơn vị vận chuyển · ' + o.tracking, by: 'Kho HCM' }); }
      if (kind === 'delivered') {
        o.fulfillment = 'DELIVERED'; o.events.push({ at: t + 2 * day, text: cod ? 'Đã giao · shipper đã thu ' + CAD.formatPrice(o.totals.total) : 'Đã giao', by: 'Đơn vị vận chuyển' });
        if (cod) o.payment.status = 'PAID';
        if (ageDays > 10) o.status = 'COMPLETED';
      }
      if (kind === 'cancelled') { o.status = 'CANCELLED'; o.cancelReason = pick(['Khách đặt nhầm size', 'Không liên lạc được khách']); o.events.push({ at: t, text: 'Huỷ đơn: ' + o.cancelReason, by: 'Linh Trần' }); }
      if (kind === 'pay-stale') { o.status = 'PENDING_PAYMENT'; o.payment = { method: 'vnpay', status: 'PENDING', expiresAt: created + 15 * 60000 }; }
      if (kind === 'expired') { o.status = 'CANCELLED'; o.cancelReason = 'Hết hạn thanh toán'; o.payment = { method: 'vnpay', status: 'EXPIRED' }; o.events.push({ at: created + 15 * 60000, text: 'Hết hạn thanh toán, trả hàng về kho', by: 'Hệ thống' }); }
      if (kind === 'failed') { o.status = 'PENDING_PAYMENT'; o.payment = { method: 'vnpay', status: 'FAILED', expiresAt: created + 15 * 60000 }; o.events.push({ at: created + 120000, text: 'Ngân hàng từ chối giao dịch', by: 'Hệ thống' }); }
      out.push(o);
    });
    return out.sort(function (a, b) { return b.createdAt - a.createdAt; });
  }
  if (!CAD.store.get('cad-admin-orders-v3', null)) CAD.store.set('cad-admin-orders-v3', makeSeed());

  A.orders = function () {
    return CAD.store.get('cad-orders', []).concat(CAD.store.get('cad-admin-orders-v3', []))
      .sort(function (a, b) { return b.createdAt - a.createdAt; });
  };
  A.order = function (n) { return A.orders().filter(function (o) { return o.number === n; })[0]; };
  A.saveOrder = function (o) {
    var key = o.seeded ? 'cad-admin-orders-v3' : 'cad-orders';
    var list = CAD.store.get(key, []).map(function (x) { return x.number === o.number ? o : x; });
    CAD.store.set(key, list);
  };
  A.event = function (o, text) {
    o.events = o.events || [{ at: o.createdAt, text: 'Đặt hàng', by: 'Khách hàng' }];
    o.events.push({ at: Date.now(), text: text, by: A.user().name });
  };

  /* =================== Tồn kho + sổ cái biến động =================== */
  A.inventory = function () {
    var inv = CAD.store.get('cad-inventory-v3', null);
    if (!inv) {
      /* Khởi tạo: tồn thực tế = còn bán được + giữ cho đơn + tồn an toàn */
      inv = {}; var ledger = [];
      var res = CAD.reservedMap();
      CAD.products.forEach(function (p) {
        Object.keys(p.sizes).forEach(function (s) {
          var k = CAD.skuKey(p.slug, s), safety = 1;
          var onHand = p.sizes[s] + (res[k] || 0) + safety;
          inv[k] = { onHand: onHand, safety: safety, location: 'Kho HCM' };
          ledger.push({ at: Date.now() - 20 * 86400000, sku: k, type: 'RECEIVE', qty: onHand, reason: 'Nhập hàng đầu mùa · PO-2611-001', by: 'Kho HCM' });
        });
      });
      CAD.store.set('cad-inventory-v3', inv); CAD.store.set('cad-ledger-v3', ledger);
    }
    return inv;
  };
  A.ledger = function () { return CAD.store.get('cad-ledger-v3', []); };
  A.move = function (key, type, qty, reason) {
    var inv = A.inventory();
    if (type === 'RECEIVE' || type === 'ADJUST' || type === 'RETURN_RESTOCK') inv[key].onHand += qty;
    if (type === 'SHIP') inv[key].onHand -= qty;
    CAD.store.set('cad-inventory-v3', inv);
    var l = A.ledger(); l.push({ at: Date.now(), sku: key, type: type, qty: qty, reason: reason, by: A.user().name }); CAD.store.set('cad-ledger-v3', l);
  };
  A.skuCode = function (slug, size) { return slug.toUpperCase().replace(/-/g, '') + '-' + size; };
  A.stockRows = function () {
    var inv = A.inventory(), res = CAD.reservedMap();
    var rows = [];
    CAD.products.forEach(function (p) {
      Object.keys(p.sizes).forEach(function (s) {
        var k = CAD.skuKey(p.slug, s), r = inv[k];
        if (!r) return;
        var reserved = res[k] || 0;
        rows.push({ key: k, p: p, size: s, sku: A.skuCode(p.slug, s), onHand: r.onHand, reserved: reserved, safety: r.safety, ats: r.onHand - reserved - r.safety, location: r.location });
      });
    });
    return rows;
  };

  /* =================== Nhãn trạng thái =================== */
  var L = {
    order: { PENDING_CONFIRMATION: ['Chờ xác nhận', 'warn'], PENDING_PAYMENT: ['Chờ thanh toán', 'warn'], PLACED: ['Đã xác nhận', 'info'], COMPLETED: ['Hoàn tất', 'ok'], CANCELLED: ['Đã huỷ', 'muted'] },
    pay: { UNPAID: ['COD · chưa thu', 'muted'], PENDING: ['Đang chờ', 'warn'], PAID: ['Đã thanh toán', 'ok'], FAILED: ['Thất bại', 'bad'], EXPIRED: ['Hết hạn', 'muted'], REFUND_PENDING: ['Chờ hoàn tiền', 'warn'], REFUNDED: ['Đã hoàn tiền', 'muted'] },
    ful: { UNFULFILLED: ['Chưa xử lý', 'muted'], PACKED: ['Đã đóng gói', 'info'], SHIPPED: ['Đang giao', 'info'], DELIVERED: ['Đã giao', 'ok'], FAILED_DELIVERY: ['Giao thất bại', 'bad'] }
  };
  A.LABELS = L;
  A.pill = function (axis, v) { var x = L[axis][v] || [v, 'muted']; return '<span class="pill pill--' + x[1] + '">' + x[0] + '</span>'; };
  A.when = function (ts) { var d = new Date(ts); return ('0' + d.getDate()).slice(-2) + '.' + ('0' + (d.getMonth() + 1)).slice(-2) + ' ' + ('0' + d.getHours()).slice(-2) + ':' + ('0' + d.getMinutes()).slice(-2); };
  A.short = function (vnd) { return vnd >= 1e6 ? (Math.round(vnd / 1e5) / 10).toString().replace('.', ',') + ' tr' : Math.round(vnd / 1000) + 'k'; };
  A.esc = function (s) { return String(s == null ? '' : s).replace(/[&<>"]/g, function (c) { return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' }[c]; }); };

  /* Đơn VNPay đang chờ quá 15 phút: cần đối soát (hỏi lại cổng thanh toán) */
  A.isStalePayment = function (o) { return o.payment.method === 'vnpay' && o.payment.status === 'PENDING' && Date.now() - o.createdAt > 15 * 60000 && o.status !== 'CANCELLED'; };
})();
