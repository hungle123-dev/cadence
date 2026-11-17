/* Checkout 1 trang: liên hệ → địa chỉ → giao hàng → thanh toán → hoá đơn.
   Mô phỏng hành vi backend khi Đặt hàng: idempotency key, tính lại giá, giữ hàng, tạo đơn snapshot. */
(function () {
  var $ = function (s, r) { return (r || document).querySelector(s); };
  var $$ = function (s, r) { return Array.prototype.slice.call((r || document).querySelectorAll(s)); };
  var demo = new URLSearchParams(location.search).get('demo') || '';
  $$('[data-demo]').forEach(function (a) { if (a.getAttribute('data-demo') === demo) a.setAttribute('aria-current', 'true'); });

  var items = CAD.getCart();
  var voucher = CAD.store.get('cad-voucher', null);
  var draft = CAD.store.get('cad-checkout-draft', {}); /* lưu nháp form để khách quay lại không phải nhập lại */
  var idemKey = 'idem-' + Date.now() + '-' + Math.random().toString(36).slice(2, 8); /* 1 key cho 1 lần thử đặt */
  var submitting = false;
  /* Size một cỡ (phụ kiện) hiển thị là "Một size" */
  function sizeLabel(sz, lower) { return sz === 'OS' ? (lower ? 'một size' : 'Một size') : (lower ? 'size ' : 'Size ') + sz; }
  var root = $('#co-root');

  if (!items.length) {
    root.innerHTML = '<div class="cart-empty"><h1 class="h2">Giỏ hàng đang trống</h1><p>Thêm sản phẩm vào giỏ trước khi thanh toán.</p>' +
      '<div class="cart-empty__actions"><a class="btn" href="plp.html">Mua sắm</a><a class="btn btn--outline" href="cart.html?demo=normal">Dùng giỏ hàng mẫu</a></div></div>';
    return;
  }

  function field(id, label, opts) {
    opts = opts || {};
    var v = draft[id] || opts.value || '';
    var input = opts.select
      ? '<select class="select" id="' + id + '" name="' + id + '"' + (opts.required ? ' required' : '') + '><option value="">Chọn ' + label.toLowerCase() + '</option>' +
        opts.select.map(function (o) { return '<option' + (o === v ? ' selected' : '') + '>' + o + '</option>'; }).join('') + '</select>'
      : opts.textarea
        ? '<textarea class="textarea" id="' + id + '" name="' + id + '" placeholder="' + (opts.placeholder || '') + '">' + v + '</textarea>'
        : '<input class="input" id="' + id + '" name="' + id + '" type="' + (opts.type || 'text') + '"' + (opts.required ? ' required' : '') +
          (opts.autocomplete ? ' autocomplete="' + opts.autocomplete + '"' : '') + (opts.inputmode ? ' inputmode="' + opts.inputmode + '"' : '') +
          (opts.list ? ' list="' + opts.list + '"' : '') + (opts.placeholder ? ' placeholder="' + opts.placeholder + '"' : '') +
          ' value="' + v.replace(/"/g, '&quot;') + '"' + (opts.describedby ? ' aria-describedby="' + opts.describedby + '"' : '') + '>';
    return '<div class="field' + (opts.span ? ' span-2' : '') + '"><label for="' + id + '">' + label + (opts.required ? '<span class="req" aria-hidden="true">*</span>' : ' <span class="opt-tag">(không bắt buộc)</span>') + '</label>' +
      input + (opts.hint ? '<span class="field__hint" id="' + id + '-hint">' + opts.hint + '</span>' : '') +
      '<span class="field__error" id="' + id + '-err">' + (opts.error || 'Vui lòng nhập ' + label.toLowerCase() + '.') + '</span></div>';
  }

  var session = CAD.store.get('cad-session', null);
  var CASH = '<span class="choice__mark" aria-hidden="true"><svg viewBox="0 0 24 24"><rect x="3" y="6.5" width="18" height="11" rx="1.5"/><circle cx="12" cy="12" r="2.5"/><path d="M6.5 9.5v5M17.5 9.5v5"/></svg></span>';
  var QR = '<span class="choice__mark" aria-hidden="true"><svg viewBox="0 0 24 24"><path d="M4 4h6v6H4zM14 4h6v6h-6zM4 14h6v6H4zM14 14h2.5v2.5H14zM17.5 17.5H20V20h-2.5zM14 18.5v1.5M18.5 14H20"/></svg></span>';

  root.innerHTML =
    '<div class="checkout">' +
      '<form class="co-form" id="co-form" novalidate>' +
        '<div class="co-intro"><h1 class="h2">Thanh toán</h1>' +
          (session ? '<p>Đang đặt hàng với tài khoản ' + session.email + '.</p>'
                   : '<p>Không cần tài khoản: chỉ cần email và số điện thoại. Đã là hội viên? <a href="account.html?next=checkout.html">Đăng nhập</a></p>') +
        '</div>' +
        '<div id="form-alert"></div>' +

        '<section class="co-section" aria-labelledby="s1"><div class="co-section__head"><h2 class="h3" id="s1"><span class="co-num" aria-hidden="true">1</span>Thông tin liên hệ</h2>' +
          '<span class="co-section__aside">Các ô có <span class="req">*</span> là bắt buộc</span></div>' +
          '<div class="form-grid form-grid--2">' +
            field('email', 'Email', { type: 'email', required: true, autocomplete: 'email', value: session ? session.email : '', hint: 'Để gửi xác nhận đơn và hoá đơn.', describedby: 'email-hint email-err', error: 'Nhập email hợp lệ, ví dụ ten@gmail.com.' }) +
            field('phone', 'Số điện thoại', { type: 'tel', required: true, autocomplete: 'tel', inputmode: 'numeric', hint: 'Shipper sẽ gọi số này khi giao.', describedby: 'phone-hint phone-err', error: 'Nhập số điện thoại 10 số, bắt đầu bằng 0.' }) +
          '</div>' +
          '<label class="check"><input type="checkbox" id="marketing"' + (draft.marketing ? ' checked' : '') + '><span>Gửi cho tôi tin ra mắt và ưu đãi qua email. Bạn có thể huỷ bất cứ lúc nào.</span></label>' +
        '</section>' +

        '<section class="co-section" aria-labelledby="s2"><div class="co-section__head"><h2 class="h3" id="s2"><span class="co-num" aria-hidden="true">2</span>Địa chỉ giao hàng</h2></div>' +
          '<div class="form-grid form-grid--2">' +
            field('name', 'Họ và tên người nhận', { required: true, autocomplete: 'name', span: true, value: session ? session.name : '' }) +
            field('province', 'Tỉnh / Thành phố', { required: true, select: CAD.PROVINCES, error: 'Chọn tỉnh / thành phố.' }) +
            field('ward', 'Phường / Xã', { required: true, list: 'ward-list', autocomplete: 'off', hint: 'Theo địa giới mới từ 01/07/2025, không còn quận/huyện.', describedby: 'ward-hint ward-err' }) +
            field('street', 'Số nhà, tên đường', { required: true, autocomplete: 'address-line1', span: true, placeholder: 'Ví dụ: 12 Nguyễn Huệ' }) +
            field('note', 'Ghi chú cho shipper', { textarea: true, span: true, placeholder: 'Ví dụ: gọi trước khi giao, giao giờ hành chính' }) +
          '</div><datalist id="ward-list"></datalist>' +
        '</section>' +

        '<section class="co-section" aria-labelledby="s3"><div class="co-section__head"><h2 class="h3" id="s3"><span class="co-num" aria-hidden="true">3</span>Giao hàng</h2></div>' +
          '<div id="ship-options" role="radiogroup" aria-labelledby="s3"></div>' +
        '</section>' +

        '<section class="co-section" aria-labelledby="s4"><div class="co-section__head"><h2 class="h3" id="s4"><span class="co-num" aria-hidden="true">4</span>Thanh toán</h2></div>' +
          '<div role="radiogroup" aria-labelledby="s4">' +
            '<label class="choice"><input type="radio" name="payment" value="cod"' + (draft.payment !== 'vnpay' ? ' checked' : '') + '><div><div class="choice__title">Thanh toán khi nhận hàng (COD)</div>' +
              '<div class="choice__desc">Trả tiền mặt cho shipper. Chúng tôi sẽ gọi hoặc nhắn Zalo để xác nhận đơn trước khi giao.</div></div>' + CASH + '</label>' +
            '<label class="choice"><input type="radio" name="payment" value="vnpay"' + (draft.payment === 'vnpay' ? ' checked' : '') + '><div><div class="choice__title">VNPay</div>' +
              '<div class="choice__desc">Quét QR bằng app ngân hàng, thẻ ATM nội địa hoặc thẻ quốc tế. Bạn có 15 phút để hoàn tất, hàng được giữ trong thời gian đó.</div></div>' + QR + '</label>' +
          '</div>' +
        '</section>' +

        '<section class="co-section" aria-labelledby="s5"><div class="co-section__head"><h2 class="h3" id="s5"><span class="co-num" aria-hidden="true">5</span>Hoá đơn</h2></div>' +
          '<label class="check"><input type="checkbox" id="invoice-toggle"' + (draft.invoice ? ' checked' : '') + ' aria-controls="invoice-fields"><span>Xuất hoá đơn điện tử cho công ty</span></label>' +
          '<div class="form-grid form-grid--2" id="invoice-fields"' + (draft.invoice ? '' : ' hidden') + '>' +
            field('company', 'Tên công ty', { span: true, error: 'Nhập tên công ty.' }) +
            field('taxcode', 'Mã số thuế', { inputmode: 'numeric', error: 'Mã số thuế gồm 10 hoặc 13 số.' }) +
            field('invoice_email', 'Email nhận hoá đơn', { type: 'email', error: 'Nhập email hợp lệ để nhận hoá đơn.' }) +
          '</div>' +
        '</section>' +

        '<p class="legal">Bằng việc đặt hàng, bạn đồng ý với <a href="#">Điều khoản</a> và <a href="#">Chính sách bảo mật</a> của Cadence. Giá đã bao gồm VAT.</p>' +
        '<div class="place-order">' +
          '<button class="btn btn--lg btn--block" type="submit" id="place-btn">Đặt hàng</button>' +
        '</div>' +
      '</form>' +

      '<div class="summary-wrap" id="summary-wrap">' +
        '<button class="summary-toggle" type="button" id="summary-toggle" aria-expanded="false" aria-controls="co-summary"><span class="summary-toggle__label"><span>Xem tóm tắt đơn hàng</span><svg class="icon" aria-hidden="true"><use href="#i-chev-d"/></svg></span><span class="summary-toggle__total" id="st-total"></span></button>' +
        '<aside class="summary" id="co-summary" aria-label="Tóm tắt đơn hàng"></aside>' +
      '</div>' +
    '</div>';

  /* ---------- Phường/xã gợi ý theo tỉnh ---------- */
  function updWards() {
    var prov = $('#province').value;
    $('#ward-list').innerHTML = (CAD.WARDS_SAMPLE[prov] || []).map(function (w) { return '<option value="' + w + '">'; }).join('');
  }

  /* ---------- Phương thức giao hàng (hoả tốc chỉ cho HCM/HN) ---------- */
  var shipping = draft.shipping || 'standard';
  function renderShip(t) {
    var prov = $('#province').value;
    $('#ship-options').innerHTML = CAD.SHIPPING.map(function (m) {
      var off = m.cities && m.cities.indexOf(prov) === -1;
      if (off && shipping === m.id) shipping = 'standard';
      var free = m.freeOver && t.subtotal >= m.freeOver;
      var aside = free ? '<s>' + CAD.formatPrice(m.fee) + '</s>Miễn phí' : CAD.formatPrice(m.fee);
      if (t.voucher && t.voucher.type === 'shipping') aside = '<s>' + CAD.formatPrice(m.fee) + '</s>Miễn phí';
      return '<label class="choice' + (off ? ' is-disabled' : '') + '"><input type="radio" name="shipping" value="' + m.id + '"' + (shipping === m.id ? ' checked' : '') + (off ? ' disabled' : '') + '>' +
        '<div><div class="choice__title">' + m.name + '</div><div class="choice__desc">' + m.eta + (off ? ' · Chỉ có ở TP. Hồ Chí Minh và Hà Nội' : '') + '</div></div>' +
        '<span class="choice__aside">' + aside + '</span></label>';
    }).join('');
  }

  /* ---------- Tóm tắt đơn ---------- */
  function renderSummary() {
    var t = CAD.totals(items, { voucher: voucher && voucher.code, shipping: shipping });
    renderShip(t);
    t = CAD.totals(items, { voucher: voucher && voucher.code, shipping: shipping });
    $('#co-summary').innerHTML =
      '<h2 class="h3">Đơn hàng (' + t.count + ')</h2>' +
      '<div class="co-lines">' + t.lines.map(function (l) {
        return '<div class="co-line"><div class="co-line__img"><img src="' + CAD.img(l.product.img, 160) + '" alt="" width="160" height="160" decoding="async"><span class="co-line__qty" aria-label="Số lượng ' + l.item.qty + '">' + l.item.qty + '</span></div>' +
          '<div><div class="co-line__name">' + l.product.name + '</div><div class="co-line__meta">' + l.item.color + ' · ' + sizeLabel(l.item.size) + '</div></div>' +
          '<div class="co-line__price">' + CAD.formatPrice(l.total) + '</div></div>';
      }).join('') + '</div>' +
      (t.voucher ? '<div class="voucher-applied"><span><strong>' + voucher.code + '</strong> · ' + t.voucher.label + '</span><a href="cart.html">Sửa</a></div>'
                 : '<p class="form-note"><a class="link" href="cart.html">Có mã giảm giá? Nhập ở giỏ hàng</a></p>') +
      '<div class="summary__rows">' +
        '<div class="summary__row"><span>Tạm tính</span><span>' + CAD.formatPrice(t.subtotal) + '</span></div>' +
        (t.discount ? '<div class="summary__row summary__row--discount"><span>Mã giảm giá</span><span>−' + CAD.formatPrice(t.discount) + '</span></div>' : '') +
        '<div class="summary__row"><span>Giao hàng · ' + t.shipping.name + '</span><span>' + (t.shippingFee - t.shippingDiscount === 0 ? 'Miễn phí' : CAD.formatPrice(t.shippingFee)) + '</span></div>' +
      '</div>' +
      '<div class="summary__total"><span>Tổng cộng</span><span>' + CAD.formatPrice(t.total) + '</span></div>' +
      '<p class="summary__vat">Đã gồm VAT ' + CAD.formatPrice(t.vat) + '</p>';
    $('#st-total').textContent = CAD.formatPrice(t.total);
    $('#place-btn').textContent = 'Đặt hàng · ' + CAD.formatPrice(t.total);
    return t;
  }

  /* ---------- Validate ---------- */
  var rules = {
    email: function (v) { return /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(v); },
    phone: function (v) { return /^0\d{9}$/.test(v.replace(/[\s.]/g, '')); },
    name: function (v) { return v.trim().length >= 2; },
    province: function (v) { return !!v; },
    ward: function (v) { return v.trim().length >= 2; },
    street: function (v) { return v.trim().length >= 3; },
    taxcode: function (v) { return !$('#invoice-toggle').checked || /^\d{10}(\d{3})?$/.test(v.replace(/-/g, '')); },
    company: function (v) { return !$('#invoice-toggle').checked || v.trim().length > 1; },
    invoice_email: function (v) { return !$('#invoice-toggle').checked || /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(v); }
  };
  function check(id) {
    var el = $('#' + id); if (!el || !rules[id]) return true;
    var ok = rules[id](el.value);
    el.closest('.field').classList.toggle('is-invalid', !ok);
    el.setAttribute('aria-invalid', !ok);
    return ok;
  }

  /* ---------- Lưu nháp ---------- */
  function saveDraft() {
    var d = {};
    ['email', 'phone', 'name', 'province', 'ward', 'street', 'note', 'company', 'taxcode', 'invoice_email'].forEach(function (k) { var el = $('#' + k); if (el) d[k] = el.value; });
    d.marketing = $('#marketing').checked; d.invoice = $('#invoice-toggle').checked;
    d.shipping = shipping; d.payment = ($('input[name=payment]:checked') || {}).value;
    CAD.store.set('cad-checkout-draft', d);
  }

  /* ---------- Sự kiện ---------- */
  var form = $('#co-form');
  form.addEventListener('focusout', function (e) {
    if (e.target.id && rules[e.target.id] && e.target.value) check(e.target.id);
  });
  form.addEventListener('input', function (e) {
    var f = e.target.closest('.field');
    if (f && f.classList.contains('is-invalid')) check(e.target.id);
    saveDraft();
  });
  form.addEventListener('change', function (e) {
    if (e.target.id === 'province') { updWards(); renderSummary(); }
    if (e.target.name === 'shipping') { shipping = e.target.value; renderSummary(); }
    if (e.target.id === 'invoice-toggle') $('#invoice-fields').hidden = !e.target.checked;
    saveDraft();
  });
  $('#summary-toggle').addEventListener('click', function () {
    var w = $('#summary-wrap'), open = !w.classList.contains('is-open');
    w.classList.toggle('is-open', open);
    this.setAttribute('aria-expanded', open);
    this.querySelector('.summary-toggle__label span').textContent = open ? 'Ẩn tóm tắt đơn hàng' : 'Xem tóm tắt đơn hàng';
  });

  function alertBox(kind, title, body) {
    $('#form-alert').innerHTML = '<div class="notice notice--' + kind + '" role="alert" tabindex="-1" style="margin-bottom:24px"><strong>' + title + '</strong><span>' + body + '</span></div>';
    var n = $('#form-alert .notice'); n.focus(); n.scrollIntoView({ block: 'center' });
  }

  form.addEventListener('submit', function (e) {
    e.preventDefault();
    if (submitting) return; /* chặn bấm 2 lần; backend còn chặn thêm bằng idempotency key */
    var ids = ['email', 'phone', 'name', 'province', 'ward', 'street', 'company', 'taxcode', 'invoice_email'];
    var bad = ids.filter(function (id) { return !check(id); });
    if (bad.length) {
      alertBox('error', 'Kiểm tra lại ' + bad.length + ' thông tin', 'Các ô bị đánh dấu đỏ cần được sửa trước khi đặt hàng.');
      $('#' + bad[0]).focus();
      return;
    }
    submitting = true;
    var btn = $('#place-btn');
    btn.classList.add('is-loading'); btn.textContent = 'Đang đặt hàng…';

    setTimeout(function () {
      /* Backend tính lại giá + giữ hàng. Prototype mô phỏng lỗi nghiệp vụ theo ?demo= */
      if (demo === 'price-changed') {
        submitting = false; btn.classList.remove('is-loading'); renderSummary();
        alertBox('warn', 'Giá đã thay đổi', 'Giá của Cadence Tempo 2 vừa được cập nhật từ 3.490.000 ₫ thành 3.290.000 ₫. Kiểm tra lại tổng tiền rồi bấm Đặt hàng lần nữa.');
        demo = ''; return;
      }
      if (demo === 'out-of-stock') {
        submitting = false; btn.classList.remove('is-loading'); renderSummary();
        alertBox('error', 'Một sản phẩm vừa hết hàng', 'Size bạn chọn của ' + CAD.find(items[0].slug).name + ' vừa được người khác đặt hết. Chưa có khoản tiền nào bị trừ. <a class="link" href="cart.html">Quay lại giỏ hàng để đổi size</a>');
        return;
      }
      var t = CAD.totals(items, { voucher: voucher && voucher.code, shipping: shipping });
      var payment = $('input[name=payment]:checked').value;
      var now = Date.now();
      var order = {
        number: CAD.newOrderNumber(), idempotencyKey: idemKey, createdAt: now,
        /* Snapshot: tên, màu, size, giá, phần giảm được phân bổ — không tham chiếu giá hiện tại */
        lines: t.lines.map(function (l) {
          return { slug: l.product.slug, name: l.product.name, img: l.product.img, color: l.item.color, size: l.item.size, qty: l.item.qty, unit: l.unit, allocatedDiscount: l.allocated || 0 };
        }),
        totals: { subtotal: t.subtotal, discount: t.discount, shipping: t.shippingFee - t.shippingDiscount, total: t.total, vat: t.vat },
        voucher: t.voucher ? voucher.code : null,
        contact: { email: $('#email').value, phone: $('#phone').value },
        address: { name: $('#name').value, street: $('#street').value, ward: $('#ward').value, province: $('#province').value, note: $('#note').value },
        shipping: { id: t.shipping.id, name: t.shipping.name, eta: t.shipping.eta },
        payment: { method: payment, status: payment === 'cod' ? 'UNPAID' : 'PENDING', expiresAt: payment === 'vnpay' ? now + 15 * 60 * 1000 : null },
        status: payment === 'cod' ? 'PENDING_CONFIRMATION' : 'PENDING_PAYMENT',
        fulfillment: 'UNFULFILLED',
        invoice: $('#invoice-toggle').checked ? { company: $('#company').value, taxcode: $('#taxcode').value, email: $('#invoice_email').value } : null
      };
      CAD.saveOrder(order);
      CAD.setCart([]); CAD.store.set('cad-voucher', null); CAD.store.set('cad-checkout-draft', {});
      location.href = payment === 'cod' ? 'order-result.html?order=' + order.number : 'gateway.html?order=' + order.number;
    }, 1100);
  });

  updWards();
  renderSummary();
})();
