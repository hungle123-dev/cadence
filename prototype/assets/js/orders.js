/* Hiển thị chi tiết & theo dõi đơn (dùng chung cho Tra cứu đơn và Tài khoản).
   Đơn có 3 trục trạng thái tách biệt: Đơn hàng · Thanh toán · Giao hàng (đúng mô hình domain). */
window.CAD = window.CAD || {};

(function () {
  var ORDER = { PENDING_CONFIRMATION: 'Chờ xác nhận', PENDING_PAYMENT: 'Chờ thanh toán', PLACED: 'Đã xác nhận', COMPLETED: 'Hoàn tất', CANCELLED: 'Đã huỷ' };
  var PAY = { UNPAID: 'Trả khi nhận hàng', PENDING: 'Đang chờ', PAID: 'Đã thanh toán', FAILED: 'Thất bại', EXPIRED: 'Hết hạn', REFUND_PENDING: 'Đang hoàn tiền', REFUNDED: 'Đã hoàn tiền' };
  var FUL = { UNFULFILLED: 'Chưa xử lý', PACKED: 'Đã đóng gói', SHIPPED: 'Đang giao', DELIVERED: 'Đã giao' };
  CAD.ORDER_LABELS = { order: ORDER, pay: PAY, ful: FUL };
  /* Size một cỡ (phụ kiện) hiển thị là "Một size" */
  function sizeLabel(sz, lower) { return sz === 'OS' ? (lower ? 'một size' : 'Một size') : (lower ? 'size ' : 'Size ') + sz; }
  /* Sắc thái cho viên trạng thái: success · pending · error · info · neutral */
  var TONE = {
    order: { PENDING_CONFIRMATION: 'pending', PENDING_PAYMENT: 'pending', PLACED: 'success', COMPLETED: 'success', CANCELLED: 'error' },
    pay: { UNPAID: 'neutral', PENDING: 'pending', PAID: 'success', FAILED: 'error', EXPIRED: 'error', REFUND_PENDING: 'pending', REFUNDED: 'neutral' },
    ful: { UNFULFILLED: 'neutral', PACKED: 'info', SHIPPED: 'info', DELIVERED: 'success' }
  };
  CAD.statusPill = function (axis, key) {
    return '<span class="status status--' + (TONE[axis][key] || 'neutral') + '">' + CAD.ORDER_LABELS[axis][key] + '</span>';
  };
  CAD.backLink = function (label, attrs) {
    return '<' + (attrs.href ? 'a' : 'button type="button"') + ' class="odetail__back"' + (attrs.href ? ' href="' + attrs.href + '"' : '') + (attrs.id ? ' id="' + attrs.id + '"' : '') + '>' +
      '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="m15 6-6 6 6 6"/></svg>' + label + '</' + (attrs.href ? 'a' : 'button') + '>';
  };

  function t(ts) { var d = new Date(ts); return ('0' + d.getHours()).slice(-2) + ':' + ('0' + d.getMinutes()).slice(-2) + ' · ' + ('0' + d.getDate()).slice(-2) + '.' + ('0' + (d.getMonth() + 1)).slice(-2); }
  CAD.fmtWhen = t;

  /* Ghi sự kiện vào timeline của đơn (audit trail) */
  CAD.orderEvent = function (order, text) {
    order.events = order.events || [{ at: order.createdAt, text: 'Đặt hàng' }];
    order.events.push({ at: Date.now(), text: text });
  };

  CAD.canCancel = function (o) { return o.status !== 'CANCELLED' && o.status !== 'COMPLETED' && (o.fulfillment || 'UNFULFILLED') === 'UNFULFILLED'; };

  CAD.orderDetailHtml = function (o) {
    var events = o.events || [{ at: o.createdAt, text: 'Đặt hàng' }];
    if (!o.events && o.payment.paidAt) events.push({ at: o.payment.paidAt, text: 'Thanh toán thành công qua VNPay' });
    var ful = o.fulfillment || 'UNFULFILLED';
    var a = o.address;
    var deliveredAt = (events.filter(function (e) { return /^Đã giao( ·|$)/.test(e.text); })[0] || {}).at;
    var returnUntil = deliveredAt ? new Date(deliveredAt + 30 * 86400000) : null;

    var actions = '';
    /* Hành động chính (nút đen) đứng đầu */
    if (o.payment.method === 'vnpay' && (o.payment.status === 'FAILED' || o.payment.status === 'PENDING') && o.status !== 'CANCELLED')
      actions += '<a class="btn" href="gateway.html?order=' + o.number + '">Tiếp tục thanh toán</a>';
    if (ful === 'DELIVERED') actions += '<button class="btn btn--outline" type="button" data-order-act="return">Đổi size / trả hàng</button>';
    actions += '<a class="btn btn--outline" href="#">Chat với hỗ trợ</a>';
    if (CAD.canCancel(o)) actions += '<button class="btn btn--outline" type="button" data-order-act="cancel">Huỷ đơn</button>';

    var count = o.lines.reduce(function (n, l) { return n + l.qty; }, 0);
    return '<div class="odetail">' +
      '<header class="odetail__head">' +
        '<h1 class="h2">Đơn ' + o.number + '</h1>' +
        '<p class="odetail__meta">Đặt lúc ' + t(o.createdAt) + ' · ' + count + ' sản phẩm · ' + CAD.formatPrice(o.totals.total) + '</p>' +
        '<div class="axes">' +
          '<span class="axis"><b>Đơn hàng</b>' + CAD.statusPill('order', o.status) + '</span>' +
          '<span class="axis"><b>Thanh toán</b>' + CAD.statusPill('pay', o.payment.status) + '</span>' +
          '<span class="axis"><b>Giao hàng</b>' + CAD.statusPill('ful', ful) + '</span>' +
        '</div>' +
        (o.cancelReason ? '<div class="notice notice--error"><strong>Đơn đã huỷ</strong><span>Lý do: ' + o.cancelReason + '.' +
          (o.payment.status === 'REFUND_PENDING' ? ' Tiền sẽ được hoàn về tài khoản VNPay của bạn trong 3–7 ngày làm việc.' : '') + '</span></div>' : '') +
        (o.tracking ? '<div class="track-box"><span>Mã vận đơn</span><span class="track-box__code">' + o.tracking + '</span><span class="track-box__sub">Đơn vị vận chuyển (giả lập) · ' + o.shipping.name + '</span></div>' : '') +
        (returnUntil ? '<p class="odetail__meta">Bạn có thể đổi size hoặc trả hàng đến hết ngày ' + ('0' + returnUntil.getDate()).slice(-2) + '.' + ('0' + (returnUntil.getMonth() + 1)).slice(-2) + '.' + returnUntil.getFullYear() + '.</p>' : '') +
        '<div class="odetail__actions">' + actions + '</div>' +
      '</header>' +
      '<div class="odetail__grid">' +
        '<div class="result__col">' +
          '<section class="result__section" aria-labelledby="hist-title"><h2 class="h3" id="hist-title">Lịch sử đơn hàng</h2><ol class="timeline">' +
            events.slice().reverse().map(function (e, i) { return '<li class="' + (i === 0 ? 'is-current' : 'is-done') + '"><span>' + e.text + '</span><span>' + t(e.at) + '</span></li>'; }).join('') +
          '</ol></section>' +
          '<section class="result__section" aria-labelledby="ship-title"><h2 class="h3" id="ship-title">Giao hàng và thanh toán</h2>' +
            '<dl class="kv">' +
              '<div><dt>Giao tới</dt><dd>' + a.name + '<br>' + a.street + ', ' + a.ward + ', ' + a.province + '</dd></div>' +
              '<div><dt>Liên hệ</dt><dd>' + o.contact.phone + '<br>' + o.contact.email + '</dd></div>' +
              '<div><dt>Giao hàng</dt><dd>' + o.shipping.name + ' · ' + o.shipping.eta + '</dd></div>' +
              '<div><dt>Thanh toán</dt><dd>' + (o.payment.method === 'cod' ? 'Thanh toán khi nhận hàng (COD)' : 'VNPay') + '</dd></div>' +
            '</dl></section>' +
        '</div>' +
        '<section class="result__section order-box" aria-labelledby="items-title"><h2 class="h3" id="items-title">Sản phẩm (' + count + ')</h2>' +
          '<div class="co-lines">' + o.lines.map(function (l) {
            return '<div class="co-line"><div class="co-line__img"><img src="' + CAD.img(l.img, 160) + '" alt="" width="160" height="160" decoding="async"><span class="co-line__qty" aria-label="Số lượng ' + l.qty + '">' + l.qty + '</span></div>' +
              '<div><div class="co-line__name">' + l.name + '</div><div class="co-line__meta">' + l.color + ' · ' + sizeLabel(l.size) + '</div></div>' +
              '<div class="co-line__price">' + CAD.formatPrice(l.unit * l.qty) + '</div></div>';
          }).join('') + '</div>' +
          '<div class="summary__rows">' +
            '<div class="summary__row"><span>Tạm tính</span><span>' + CAD.formatPrice(o.totals.subtotal) + '</span></div>' +
            (o.totals.discount ? '<div class="summary__row summary__row--discount"><span>Mã giảm giá' + (o.voucher ? ' · ' + o.voucher : '') + '</span><span>−' + CAD.formatPrice(o.totals.discount) + '</span></div>' : '') +
            '<div class="summary__row"><span>Giao hàng</span><span>' + (o.totals.shipping ? CAD.formatPrice(o.totals.shipping) : 'Miễn phí') + '</span></div>' +
          '</div>' +
          '<div class="summary__total"><span>Tổng cộng</span><span>' + CAD.formatPrice(o.totals.total) + '</span></div>' +
        '</section>' +
      '</div>' +
    '</div>';
  };

  /* Hộp thoại huỷ đơn (dùng <dialog> gốc: có focus trap và Esc sẵn) */
  CAD.cancelDialog = function (order, done) {
    var d = document.getElementById('cancel-dialog');
    if (!d) {
      document.body.insertAdjacentHTML('beforeend',
        '<dialog class="dialog" id="cancel-dialog" aria-labelledby="cd-title"><form method="dialog" class="dialog__body">' +
          '<h2 class="h3" id="cd-title">Huỷ đơn hàng?</h2>' +
          '<p class="muted" style="font-size:.875rem;line-height:1.5" id="cd-desc"></p>' +
          '<div class="field"><label for="cd-reason">Lý do huỷ</label><select class="select" id="cd-reason">' +
            '<option>Tôi muốn đổi size hoặc màu</option><option>Tôi đặt nhầm sản phẩm</option><option>Thời gian giao hàng quá lâu</option><option>Tôi tìm được giá tốt hơn</option><option>Lý do khác</option></select></div>' +
          '<div class="dialog__actions"><button class="btn btn--outline" type="button" data-dlg="keep">Giữ đơn</button><button class="btn" type="button" data-dlg="cancel">Huỷ đơn</button></div>' +
        '</form></dialog>');
      d = document.getElementById('cancel-dialog');
      /* Xử lý ngay khi bấm nút (không dựa vào sự kiện close, vốn không ổn định giữa các trình duyệt) */
      d.addEventListener('click', function (e) {
        var b = e.target.closest('[data-dlg]');
        if (!b) { if (e.target === d) d.close(); return; }
        d.close();
        if (b.getAttribute('data-dlg') === 'cancel' && d._onConfirm) d._onConfirm();
      });
    }
    document.getElementById('cd-desc').textContent = order.payment.status === 'PAID'
      ? 'Đơn đã thanh toán qua VNPay. Sau khi huỷ, ' + CAD.formatPrice(order.totals.total) + ' sẽ được hoàn về tài khoản của bạn trong 3–7 ngày làm việc.'
      : 'Sản phẩm sẽ được trả lại kho. Thao tác này không hoàn tác được.';
    d._onConfirm = function () {
      order.status = 'CANCELLED';
      order.cancelReason = document.getElementById('cd-reason').value;
      if (order.payment.status === 'PAID') order.payment.status = 'REFUND_PENDING';
      CAD.orderEvent(order, 'Khách huỷ đơn: ' + order.cancelReason);
      if (order.payment.status === 'REFUND_PENDING') CAD.orderEvent(order, 'Tạo yêu cầu hoàn tiền ' + CAD.formatPrice(order.totals.total));
      CAD.saveOrder(order);
      done();
    };
    d.showModal();
  };
})();
