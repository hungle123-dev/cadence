/* Trang giỏ hàng: kiểm tra lại giỏ mỗi lần hiển thị (hết size, đổi giá, vượt tồn kho),
   đổi size/số lượng, voucher, tổng tiền tính lại toàn bộ (như backend). */
(function () {
  var $ = function (s, r) { return (r || document).querySelector(s); };
  var MAX_PER_LINE = 5;
  var ICON_MINUS = '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M6 12h12"/></svg>';
  var ICON_PLUS = '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M6 12h12M12 6v12"/></svg>';
  var ICON_TRASH = '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4.5 7h15M9.5 7V4.5h5V7M6.5 7l1 13h9l1-13M10 11v5.5M14 11v5.5"/></svg>';
  var SIZE_ORDER = ['XS', 'S', 'M', 'L', 'XL', '4', '5', '35', '36', '37', '38', '39', '40', '41', '42', '43', '44', 'OS'];
  /* Size một cỡ (phụ kiện) hiển thị là "Một size" */
  function sizeLabel(sz, lower) { return sz === 'OS' ? (lower ? 'một size' : 'Một size') : (lower ? 'size ' : 'Size ') + sz; }


  /* ---------- Dữ liệu demo theo ?demo= ---------- */
  var demo = new URLSearchParams(location.search).get('demo');
  if (demo) {
    var seeds = {
      empty: [],
      normal: [
        { slug: 'tempo-2', color: 'Ô liu / Trắng', size: '41', qty: 1, priceAtAdd: 3290000 },
        { slug: 'daily-run-tee', color: 'Trắng', size: 'M', qty: 2, priceAtAdd: 690000 }
      ],
      warnings: [
        { slug: 'tempo-2', color: 'Ô liu / Trắng', size: '41', qty: 1, priceAtAdd: 3490000 },   /* giá đã giảm so với lúc thêm */
        { slug: 'mudline-trail', color: 'Xám / Vàng', size: '41', qty: 1, priceAtAdd: 2290000 },      /* size 41 vừa hết hàng */
        { slug: 'mudline-trail', color: 'Xám / Vàng', size: '44', qty: 3, priceAtAdd: 2290000 },      /* chỉ còn 2 đôi */
        { slug: 'daily-run-tee', color: 'Trắng', size: 'M', qty: 2, priceAtAdd: 690000 }
      ]
    };
    if (seeds[demo]) CAD.setCart(seeds[demo]);
    document.querySelectorAll('[data-demo]').forEach(function (a) { if (a.getAttribute('data-demo') === demo) a.setAttribute('aria-current', 'true'); });
  }

  var voucher = CAD.store.get('cad-voucher', null);
  var voucherMsg = '';

  function sizesOf(p) { return SIZE_ORDER.filter(function (s) { return s in p.sizes; }); }

  /* ---------- Kiểm tra lại giỏ: điều chỉnh SL vượt tồn, gắn cảnh báo ---------- */
  function revalidate(items) {
    var notes = {};
    items.forEach(function (it, idx) {
      var p = CAD.find(it.slug); if (!p) return;
      var stock = p.sizes[it.size] || 0, unit = CAD.unitPrice(p);
      if (stock === 0) notes[idx] = { kind: 'error', blocked: true, html: '<strong>' + sizeLabel(it.size) + ' vừa hết hàng.</strong><span>Chọn size khác hoặc xoá sản phẩm này để tiếp tục thanh toán.</span>' };
      else if (it.qty > stock) {
        notes[idx] = { kind: 'warn', html: '<strong>Chỉ còn ' + stock + ' sản phẩm ' + sizeLabel(it.size, true) + '.</strong><span>Chúng tôi đã giảm số lượng từ ' + it.qty + ' xuống ' + stock + '.</span>' };
        it.qty = stock;
      } else if (it.priceAtAdd && it.priceAtAdd !== unit) {
        var down = unit < it.priceAtAdd;
        notes[idx] = { kind: down ? 'success' : 'warn', html: '<strong>Giá đã ' + (down ? 'giảm' : 'tăng') + ' từ ' + CAD.formatPrice(it.priceAtAdd) + ' xuống ' + CAD.formatPrice(unit) + '.</strong>'
          .replace(' xuống ', down ? ' xuống ' : ' lên ') + '<button class="link" type="button" data-ack="' + idx + '">Đã hiểu</button>' };
      }
    });
    return notes;
  }

  function lineHtml(it, idx, note) {
    var p = CAD.find(it.slug);
    var stock = p.sizes[it.size] || 0;
    var unit = CAD.unitPrice(p);
    var maxQty = Math.max(1, Math.min(stock, MAX_PER_LINE));
    var sizeOpts = sizesOf(p).map(function (s) {
      var n = p.sizes[s];
      return '<option value="' + s + '"' + (s === it.size ? ' selected' : '') + (n === 0 && s !== it.size ? ' disabled' : '') + '>' + sizeLabel(s) + (n === 0 ? ' (hết)' : '') + '</option>';
    }).join('');
    var price = p.salePrice
      ? '<span class="sale">' + CAD.formatPrice(unit * it.qty) + '</span><span class="was">' + CAD.formatPrice(p.price * it.qty) + '</span>'
      : CAD.formatPrice(unit * it.qty);
    return '<article class="citem' + (note && note.blocked ? ' is-blocked' : '') + '" data-idx="' + idx + '">' +
      '<a class="citem__img" href="pdp.html?p=' + p.slug + '" tabindex="-1" aria-hidden="true"><img src="' + CAD.img(p.img, 360) + '" alt="" width="360" height="360" decoding="async"></a>' +
      '<div class="citem__body">' +
        '<div class="citem__top"><div class="citem__info"><h2 class="citem__name"><a href="pdp.html?p=' + p.slug + '">' + p.name + '</a></h2>' +
        '<p class="citem__meta">' + p.category + '</p><p class="citem__meta">' + it.color + '</p>' +
        (it.qty > 1 ? '<p class="citem__meta">' + CAD.formatPrice(unit) + ' / sản phẩm</p>' : '') + '</div>' +
        '<p class="citem__price">' + price + '</p></div>' +
        '<div class="citem__row">' +
          '<label><span class="sr-only">Size</span><select class="select" data-act="size">' + sizeOpts + '</select></label>' +
          '<div class="qty" role="group" aria-label="Số lượng">' +
            '<button type="button" data-act="dec" aria-label="Giảm số lượng"' + (it.qty <= 1 ? ' disabled' : '') + '>' + ICON_MINUS + '</button>' +
            '<output aria-live="polite">' + it.qty + '</output>' +
            '<button type="button" data-act="inc" aria-label="Tăng số lượng"' + (it.qty >= maxQty || stock === 0 ? ' disabled' : '') + '>' + ICON_PLUS + '</button>' +
          '</div>' +
          '<div class="citem__actions">' +
            '<button type="button" data-act="wish" aria-label="Chuyển ' + p.name + ' vào yêu thích" title="Chuyển vào yêu thích"><svg aria-hidden="true"><use href="#i-heart"/></svg></button>' +
            '<button type="button" data-act="remove" aria-label="Xoá ' + p.name + ' khỏi giỏ" title="Xoá khỏi giỏ">' + ICON_TRASH + '</button>' +
          '</div>' +
        '</div>' +
        (note ? '<div class="notice notice--' + note.kind + '">' + note.html + '</div>' : '') +
        (!note && stock > 0 && stock <= 2 ? '<p class="citem__low">Chỉ còn ' + stock + ' sản phẩm ' + sizeLabel(it.size, true) + '.</p>' : '') +
      '</div></article>';
  }

  function render() {
    var items = CAD.getCart();
    var notes = revalidate(items);
    CAD.store.set('cad-cart', items);
    CAD.renderCart();
    var root = $('#cart-root');
    var count = items.reduce(function (s, i) { return s + i.qty; }, 0);
    $('#cart-count-title').textContent = count ? count + ' sản phẩm' : '';

    if (!items.length) {
      root.innerHTML = '<div class="cart-empty"><h2 class="h3">Giỏ hàng đang trống</h2>' +
        '<p>Bạn chưa thêm sản phẩm nào. Xem hàng mới về hoặc giày chạy bộ bán chạy.</p>' +
        '<div class="cart-empty__actions"><a class="btn" href="plp.html?sort=new">Hàng mới về</a><a class="btn btn--outline" href="plp.html?s=chay-bo">Giày chạy bộ</a></div></div>';
      $('#mobile-checkout').hidden = true;
      return;
    }

    var blocked = Object.keys(notes).some(function (k) { return notes[k].blocked; });
    var t = CAD.totals(items, { voucher: voucher && voucher.code });
    if (voucher && !t.voucher) { voucherMsg = t.voucherError; }

    var remain = Math.max(0, CAD.FREE_SHIP - t.subtotal);
    var voucherBlock = t.voucher
      ? '<div class="voucher-applied"><span><strong>' + voucher.code + '</strong> · ' + t.voucher.label + '</span><button type="button" id="voucher-remove">Bỏ mã</button></div>'
      : '<details class="promo-code"' + (voucherMsg || voucher ? ' open' : '') + '><summary>Bạn có mã giảm giá?<svg class="icon icon--sm" aria-hidden="true"><use href="#i-chev-d"/></svg></summary>' +
        '<div class="promo-code__body"><form class="voucher" id="voucher-form" novalidate><label class="sr-only" for="voucher-input">Mã giảm giá</label>' +
        '<input class="input" id="voucher-input" placeholder="Nhập mã" autocomplete="off" value="' + (voucher ? voucher.code : '') + '"' + (voucherMsg ? ' aria-invalid="true" aria-describedby="voucher-err"' : '') + '><button class="btn btn--outline" type="submit">Áp dụng</button></form>' +
        (voucherMsg ? '<p class="form-error" id="voucher-err" role="alert">' + voucherMsg + '</p>' : '') + '</div></details>';

    root.innerHTML =
      '<div class="cart-page">' +
        '<section aria-label="Sản phẩm trong giỏ"><div class="cart-items">' +
          items.map(function (it, i) { return lineHtml(it, i, notes[i]); }).join('') +
        '</div><p class="cart-note">Sản phẩm trong giỏ chưa được giữ hàng. Hàng được giữ cho bạn khi đặt hàng.</p></section>' +
        '<aside class="summary" aria-labelledby="sum-title">' +
          '<h2 class="h3" id="sum-title">Tóm tắt đơn hàng</h2>' +
          '<div class="ship-meter"><span>' + (remain > 0 ? 'Thêm <strong>' + CAD.formatPrice(remain) + '</strong> để được miễn phí giao hàng' : 'Đơn của bạn được miễn phí giao hàng tiêu chuẩn') + '</span>' +
            '<div class="ship-meter__bar"><span style="width:' + Math.min(100, t.subtotal / CAD.FREE_SHIP * 100) + '%"></span></div></div>' +
          '<div class="summary__rows">' +
            '<div class="summary__row"><span>Tạm tính (' + t.count + ' sản phẩm)</span><span>' + CAD.formatPrice(t.subtotal) + '</span></div>' +
            (t.discount ? '<div class="summary__row summary__row--discount"><span>Mã giảm giá</span><span>−' + CAD.formatPrice(t.discount) + '</span></div>' : '') +
            '<div class="summary__row"><span>Giao hàng tiêu chuẩn</span><span>' + (t.shippingFee - t.shippingDiscount === 0 ? 'Miễn phí' : CAD.formatPrice(t.shippingFee)) + '</span></div>' +
          '</div>' +
          voucherBlock +
          '<div class="summary__total"><span>Tổng cộng</span><span>' + CAD.formatPrice(t.total) + '</span></div>' +
          '<p class="summary__vat">Đã gồm VAT ' + CAD.formatPrice(t.vat) + (t.savings ? ' · Bạn tiết kiệm ' + CAD.formatPrice(t.savings + t.discount) : '') + '</p>' +
          (blocked ? '<div class="notice notice--error"><strong>Chưa thể thanh toán</strong><span>Có sản phẩm đã hết size. Chọn size khác hoặc xoá sản phẩm đó.</span></div>' : '') +
          '<div class="summary__ctas">' +
            (blocked ? '<button class="btn btn--lg btn--block" type="button" disabled>Thanh toán không cần tài khoản</button>'
                     : '<a class="btn btn--lg btn--block" href="checkout.html">Thanh toán không cần tài khoản</a>') +
            (blocked ? '<button class="btn btn--outline btn--block" type="button" disabled>Đăng nhập để thanh toán</button>' : '<a class="btn btn--outline btn--block" href="account.html?next=checkout.html">Đăng nhập để thanh toán</a>') +
          '</div>' +
          '<ul class="summary__trust">' +
            '<li><svg class="icon" aria-hidden="true"><use href="#i-truck"/></svg><span>Thanh toán khi nhận hàng (COD) hoặc VNPay.</span></li>' +
            '<li><svg class="icon" aria-hidden="true"><use href="#i-return"/></svg><span>Đổi size miễn phí trong 30 ngày.</span></li>' +
          '</ul>' +
        '</aside>' +
      '</div>';

    $('#mobile-checkout').hidden = false;
    $('#mc-total').textContent = CAD.formatPrice(t.total);
    var mc = $('#mc-btn');
    if (blocked) { mc.removeAttribute('href'); mc.setAttribute('aria-disabled', 'true'); }
    else { mc.setAttribute('href', 'checkout.html'); mc.removeAttribute('aria-disabled'); }
  }

  /* ---------- Sự kiện ---------- */
  var root = $('#cart-root');
  root.addEventListener('click', function (e) {
    var ack = e.target.closest('[data-ack]');
    if (ack) {
      var items = CAD.getCart(), it = items[+ack.getAttribute('data-ack')];
      it.priceAtAdd = CAD.unitPrice(CAD.find(it.slug)); CAD.setCart(items); render(); return;
    }
    if (e.target.id === 'voucher-remove') { voucher = null; voucherMsg = ''; CAD.store.set('cad-voucher', null); render(); return; }
    var btn = e.target.closest('[data-act]'); if (!btn || btn.tagName === 'SELECT') return;
    var row = btn.closest('.citem'), idx = +row.getAttribute('data-idx');
    var list = CAD.getCart(), item = list[idx];
    var act = btn.getAttribute('data-act');
    if (act === 'inc') item.qty += 1;
    if (act === 'dec') item.qty = Math.max(1, item.qty - 1);
    if (act === 'remove' || act === 'wish') {
      list.splice(idx, 1);
      if (act === 'wish') {
        var w = CAD.store.get('cad-wish', []);
        if (w.indexOf(item.slug) === -1) CAD.store.set('cad-wish', w.concat(item.slug));
        CAD.toast('Đã chuyển vào danh sách yêu thích');
      } else CAD.toast('Đã xoá ' + CAD.find(item.slug).name + ' khỏi giỏ');
    }
    CAD.setCart(list); render();
    var again = root.querySelector('.citem[data-idx="' + idx + '"] [data-act="' + act + '"]');
    if (again && !again.disabled) again.focus();
  });
  root.addEventListener('change', function (e) {
    if (!e.target.matches('[data-act="size"]')) return;
    var idx = +e.target.closest('.citem').getAttribute('data-idx');
    var list = CAD.getCart(), item = list[idx];
    item.size = e.target.value;
    /* Gộp nếu đã có dòng cùng sản phẩm + màu + size */
    var dup = list.filter(function (x, i) { return i !== idx && x.slug === item.slug && x.color === item.color && x.size === item.size; })[0];
    if (dup) { dup.qty += item.qty; list.splice(idx, 1); }
    CAD.setCart(list); render();
  });
  root.addEventListener('submit', function (e) {
    if (e.target.id !== 'voucher-form') return;
    e.preventDefault();
    var code = $('#voucher-input').value.trim().toUpperCase();
    if (!code) { voucherMsg = 'Nhập mã giảm giá.'; render(); return; }
    var t = CAD.totals(CAD.getCart(), { voucher: code });
    if (t.voucher) { voucher = { code: code }; voucherMsg = ''; CAD.store.set('cad-voucher', voucher); CAD.toast('Đã áp dụng mã ' + code); }
    else { voucher = null; voucherMsg = t.voucherError; CAD.store.set('cad-voucher', null); }
    render();
  });

  render();
})();
