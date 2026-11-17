/* Trang kết quả sau khi đặt hàng / quay về từ cổng thanh toán (Return URL).
   Trạng thái hiển thị ĐỌC từ đơn đã lưu, không tin tham số trên URL.
   5 trạng thái: COD chờ xác nhận · đã thanh toán · đang chờ IPN · thanh toán thất bại · hết hạn. */
(function () {
  var $ = function (s, r) { return (r || document).querySelector(s); };
  var q = new URLSearchParams(location.search);
  /* Size một cỡ (phụ kiện) hiển thị là "Một size" */
  function sizeLabel(sz, lower) { return sz === 'OS' ? (lower ? 'một size' : 'Một size') : (lower ? 'size ' : 'Size ') + sz; }
  var number = q.get('order');
  var order = number && CAD.getOrder(number);

  /* Không có đơn (mở trực tiếp): tạo đơn mẫu để xem giao diện */
  if (!order) {
    var t0 = CAD.totals([{ slug: 'tempo-2', color: 'Ô liu / Trắng', size: '41', qty: 1 }, { slug: 'daily-run-tee', color: 'Trắng', size: 'M', qty: 2 }], {});
    order = {
      number: CAD.newOrderNumber(), createdAt: Date.now(),
      lines: t0.lines.map(function (l) { return { slug: l.product.slug, name: l.product.name, img: l.product.img, color: l.item.color, size: l.item.size, qty: l.item.qty, unit: l.unit, allocatedDiscount: 0 }; }),
      totals: { subtotal: t0.subtotal, discount: 0, shipping: 0, total: t0.total, vat: t0.vat },
      contact: { email: 'ban@example.com', phone: '0901234567' },
      address: { name: 'Nguyễn Văn An', street: '12 Nguyễn Huệ', ward: 'Phường Sài Gòn', province: 'TP. Hồ Chí Minh' },
      shipping: { id: 'standard', name: 'Tiêu chuẩn', eta: '2–4 ngày' },
      payment: { method: 'cod', status: 'UNPAID' }, status: 'PENDING_CONFIRMATION', fulfillment: 'UNFULFILLED'
    };
    CAD.saveOrder(order);
    history.replaceState(null, '', 'order-result.html?order=' + order.number);
  }

  /* Thanh demo: ép trạng thái cho đơn hiện tại (chỉ có trong prototype) */
  var force = q.get('force');
  var setState = {
    cod: function () { order.payment = { method: 'cod', status: 'UNPAID' }; order.status = 'PENDING_CONFIRMATION'; },
    success: function () { order.payment = { method: 'vnpay', status: 'PAID', paidAt: Date.now() }; order.status = 'PLACED'; },
    pending: function () { order.payment = { method: 'vnpay', status: 'PENDING', expiresAt: Date.now() + 15 * 60000, ipnDelayUntil: Date.now() + 6000 }; order.status = 'PENDING_PAYMENT'; },
    failed: function () { order.payment = { method: 'vnpay', status: 'FAILED', expiresAt: Date.now() + 12 * 60000 }; order.status = 'PENDING_PAYMENT'; },
    expired: function () { order.payment = { method: 'vnpay', status: 'EXPIRED' }; order.status = 'CANCELLED'; order.cancelReason = 'Hết hạn thanh toán'; }
  };
  if (force && setState[force]) { setState[force](); CAD.saveOrder(order); history.replaceState(null, '', 'order-result.html?order=' + order.number); }
  $('#demo-links').insertAdjacentHTML('beforeend', [['cod', 'COD chờ xác nhận'], ['success', 'Đã thanh toán'], ['pending', 'Chờ IPN'], ['failed', 'Thất bại'], ['expired', 'Hết hạn']]
    .map(function (s) { return '<a href="order-result.html?order=' + order.number + '&force=' + s[0] + '" data-state="' + s[0] + '">' + s[1] + '</a>'; }).join(''));
  function markDemo() {
    var p = order.payment, cur = p.method === 'cod' ? 'cod' : p.status === 'PAID' ? 'success' : p.status === 'PENDING' ? 'pending' : p.status === 'FAILED' ? 'failed' : 'expired';
    document.querySelectorAll('#demo-links [data-state]').forEach(function (a) {
      if (a.getAttribute('data-state') === cur) a.setAttribute('aria-current', 'true'); else a.removeAttribute('aria-current');
    });
  }

  function fmtTime(ts) { var d = new Date(ts); return ('0' + d.getHours()).slice(-2) + ':' + ('0' + d.getMinutes()).slice(-2); }
  function fmtDate(ts) { var d = new Date(ts); return ('0' + d.getDate()).slice(-2) + '.' + ('0' + (d.getMonth() + 1)).slice(-2) + '.' + d.getFullYear(); }

  function view() {
    var p = order.payment;
    var created = fmtTime(order.createdAt);
    if (p.method === 'cod') return {
      status: ['success', 'Đặt hàng thành công'],
      title: 'Cảm ơn bạn. Đơn hàng đã được ghi nhận.',
      body: 'Chúng tôi sẽ gọi hoặc nhắn Zalo tới <strong class="mono">' + order.contact.phone + '</strong> để xác nhận đơn trong khoảng 30 phút (8:00–22:00). Email xác nhận đã được gửi tới ' + order.contact.email + '.',
      actions: '<a class="btn" href="plp.html">Tiếp tục mua sắm</a><a class="btn btn--outline" href="order-lookup.html?order=' + order.number + '">Theo dõi đơn hàng</a>',
      steps: [['Đặt hàng', created, 'done'], ['Xác nhận đơn', 'Đang chờ', 'current'], ['Đóng gói', '', ''], ['Giao cho đơn vị vận chuyển', '', ''], ['Đã giao · thanh toán khi nhận', '', '']]
    };
    if (p.status === 'PAID') return {
      status: ['success', 'Thanh toán thành công'],
      title: 'Cảm ơn bạn. Đơn hàng đã được thanh toán.',
      body: 'Đã nhận ' + CAD.formatPrice(order.totals.total) + ' qua VNPay lúc ' + fmtTime(p.paidAt || Date.now()) + '. Email xác nhận và hoá đơn sẽ được gửi tới ' + order.contact.email + '.',
      actions: '<a class="btn" href="plp.html">Tiếp tục mua sắm</a><a class="btn btn--outline" href="order-lookup.html?order=' + order.number + '">Theo dõi đơn hàng</a>',
      steps: [['Đặt hàng', created, 'done'], ['Thanh toán', fmtTime(p.paidAt || Date.now()), 'done'], ['Đóng gói', 'Đang chuẩn bị', 'current'], ['Giao cho đơn vị vận chuyển', '', ''], ['Đã giao', '', '']]
    };
    if (p.status === 'PENDING') return {
      status: ['pending', 'Đang chờ xác nhận thanh toán', true],
      title: 'Chúng tôi đang chờ ngân hàng xác nhận.',
      body: 'Việc này thường mất vài giây. <strong>Đừng thanh toán lại</strong> để tránh bị trừ tiền hai lần. Trang sẽ tự cập nhật; bạn cũng sẽ nhận email khi thanh toán được xác nhận.',
      actions: p.ipnDelayUntil ? '' : '<a class="btn" href="gateway.html?order=' + order.number + '">Tôi chưa thanh toán, tiếp tục thanh toán</a>',
      steps: [['Đặt hàng', created, 'done'], ['Thanh toán', 'Đang xác nhận', 'current'], ['Đóng gói', '', ''], ['Giao cho đơn vị vận chuyển', '', ''], ['Đã giao', '', '']]
    };
    if (p.status === 'FAILED') return {
      status: ['error', 'Thanh toán chưa thành công'],
      title: 'Giao dịch không hoàn tất. Bạn chưa bị trừ tiền.',
      body: 'Đơn hàng và sản phẩm vẫn được giữ cho bạn đến <strong class="mono">' + fmtTime(p.expiresAt || Date.now() + 600000) + '</strong>. Bạn có thể thử lại hoặc chuyển sang thanh toán khi nhận hàng.',
      actions: '<button class="btn" type="button" data-act="retry">Thử thanh toán lại</button><button class="btn btn--outline" type="button" data-act="to-cod">Chuyển sang COD</button>',
      steps: [['Đặt hàng', created, 'done'], ['Thanh toán', 'Chưa thành công', 'current'], ['Đóng gói', '', 'off'], ['Giao cho đơn vị vận chuyển', '', 'off'], ['Đã giao', '', 'off']]
    };
    return {
      status: ['error', 'Đơn hàng đã hết hạn'],
      title: 'Đơn hàng đã bị huỷ vì quá thời gian thanh toán.',
      body: 'Bạn chưa bị trừ tiền. Sản phẩm đã được trả lại kho cho khách khác. Nếu ngân hàng vẫn trừ tiền, chúng tôi sẽ tự động hoàn lại trong 1–3 ngày làm việc.',
      actions: '<button class="btn" type="button" data-act="reorder">Đặt lại đơn này</button><a class="btn btn--outline" href="plp.html">Tiếp tục mua sắm</a>',
      steps: [['Đặt hàng', created, 'done'], ['Thanh toán', 'Hết hạn', 'off'], ['Đơn bị huỷ', 'Hàng đã trả về kho', 'off']]
    };
  }

  function render() {
    var v = view();
    var a = order.address;
    var count = order.lines.reduce(function (n, l) { return n + l.qty; }, 0);
    $('#result').innerHTML =
      '<header class="result__head">' +
        '<h1 class="h1">' + v.title + '</h1>' +
        '<div class="result__meta">' +
          '<p class="status status--' + v.status[0] + (v.status[2] ? ' status--pulse' : '') + '" role="status">' + v.status[1] + '</p>' +
          '<p class="result__number">Mã đơn <strong>' + order.number + '</strong>' +
            '<button class="copy-btn" type="button" data-act="copy">Sao chép</button></p>' +
        '</div>' +
        '<p class="result__body">' + v.body + '</p>' +
        (v.actions ? '<div class="result__actions">' + v.actions + '</div>' : '') +
      '</header>' +
      '<div class="result__grid">' +
        '<div class="result__col">' +
          '<section class="result__section" aria-labelledby="tl-title"><h2 class="h3" id="tl-title">Tiến trình đơn hàng</h2>' +
            '<ol class="timeline">' + v.steps.map(function (s) {
              return '<li class="is-' + (s[2] || 'todo') + '"><span>' + s[0] + '</span>' + (s[1] ? '<span>' + s[1] + '</span>' : '') + '</li>';
            }).join('') + '</ol></section>' +
          '<section class="result__section" aria-labelledby="dl-title"><h2 class="h3" id="dl-title">Giao hàng và thanh toán</h2>' +
            '<dl class="kv">' +
              '<div><dt>Giao tới</dt><dd>' + a.name + '<br>' + a.street + ', ' + a.ward + ', ' + a.province + '</dd></div>' +
              '<div><dt>Liên hệ</dt><dd>' + order.contact.phone + '<br>' + order.contact.email + '</dd></div>' +
              '<div><dt>Giao hàng</dt><dd>' + order.shipping.name + ' · ' + order.shipping.eta + '</dd></div>' +
              '<div><dt>Thanh toán</dt><dd>' + (order.payment.method === 'cod' ? 'Thanh toán khi nhận hàng (COD)' : 'VNPay') + '</dd></div>' +
              '<div><dt>Ngày đặt</dt><dd>' + fmtTime(order.createdAt) + ' · ' + fmtDate(order.createdAt) + '</dd></div>' +
            '</dl></section>' +
        '</div>' +
        '<section class="result__section result__summary order-box" aria-labelledby="od-title">' +
          '<h2 class="h3" id="od-title">Đơn hàng (' + count + ')</h2>' +
          '<div class="co-lines">' + order.lines.map(function (l) {
            return '<div class="co-line"><div class="co-line__img"><img src="' + CAD.img(l.img, 160) + '" alt="" width="160" height="160" decoding="async"><span class="co-line__qty" aria-label="Số lượng ' + l.qty + '">' + l.qty + '</span></div>' +
              '<div><div class="co-line__name">' + l.name + '</div><div class="co-line__meta">' + l.color + ' · ' + sizeLabel(l.size) + '</div></div>' +
              '<div class="co-line__price">' + CAD.formatPrice(l.unit * l.qty) + '</div></div>';
          }).join('') + '</div>' +
          '<div class="summary__rows">' +
            '<div class="summary__row"><span>Tạm tính</span><span>' + CAD.formatPrice(order.totals.subtotal) + '</span></div>' +
            (order.totals.discount ? '<div class="summary__row summary__row--discount"><span>Mã giảm giá' + (order.voucher ? ' · ' + order.voucher : '') + '</span><span>−' + CAD.formatPrice(order.totals.discount) + '</span></div>' : '') +
            '<div class="summary__row"><span>Giao hàng</span><span>' + (order.totals.shipping ? CAD.formatPrice(order.totals.shipping) : 'Miễn phí') + '</span></div>' +
          '</div>' +
          '<div class="summary__total"><span>Tổng cộng</span><span>' + CAD.formatPrice(order.totals.total) + '</span></div>' +
          '<p class="summary__vat">Đã gồm VAT ' + CAD.formatPrice(order.totals.vat) + '</p>' +
          '<p class="result__help">Cần hỗ trợ? Chat với chúng tôi 8:00–22:00, hoặc <a class="link" href="order-lookup.html?order=' + order.number + '">tra cứu đơn</a> bằng mã đơn và số điện thoại.</p>' +
        '</section>' +
      '</div>';
    document.title = view().status[1] + ' · ' + order.number + ' | Cadence';
    markDemo();
  }

  /* Mô phỏng IPN đến muộn: trang tự hỏi lại trạng thái (polling) cho tới khi có kết quả */
  setInterval(function () {
    var p = order.payment;
    if (p.status === 'PENDING' && p.ipnDelayUntil && Date.now() > p.ipnDelayUntil) {
      p.status = 'PAID'; p.paidAt = Date.now(); delete p.ipnDelayUntil; order.status = 'PLACED';
      CAD.saveOrder(order); render(); CAD.toast('Ngân hàng đã xác nhận thanh toán');
    }
  }, 2000);

  $('#result').addEventListener('click', function (e) {
    var b = e.target.closest('[data-act]'); if (!b) return;
    var act = b.getAttribute('data-act');
    if (act === 'copy') {
      var done = function () { b.textContent = 'Đã sao chép'; setTimeout(function () { b.textContent = 'Sao chép'; }, 1600); };
      if (navigator.clipboard) navigator.clipboard.writeText(order.number).then(done, function () { CAD.toast('Mã đơn: ' + order.number); });
      else CAD.toast('Mã đơn: ' + order.number);
      return;
    }
    if (act === 'retry') { order.payment.status = 'PENDING'; delete order.payment.ipnDelayUntil; CAD.saveOrder(order); location.href = 'gateway.html?order=' + order.number; }
    if (act === 'to-cod') { order.payment = { method: 'cod', status: 'UNPAID' }; order.status = 'PENDING_CONFIRMATION'; CAD.saveOrder(order); render(); CAD.toast('Đã chuyển sang thanh toán khi nhận hàng'); }
    if (act === 'reorder') {
      CAD.setCart(order.lines.map(function (l) { return { slug: l.slug, color: l.color, size: l.size, qty: l.qty, priceAtAdd: l.unit }; }));
      location.href = 'cart.html';
    }
  });

  render();
})();
