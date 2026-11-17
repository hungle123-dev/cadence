/* Tài khoản: đăng nhập / tạo tài khoản (mô phỏng), tổng quan Cadence Club, đơn hàng,
   gắn đơn mua khi chưa đăng nhập (cần xác minh email), yêu thích, sổ địa chỉ. */
(function () {
  var $ = function (s, r) { return (r || document).querySelector(s); };
  var $$ = function (s, r) { return Array.prototype.slice.call((r || document).querySelectorAll(s)); };
  var root = $('#acct-root');
  var EMAIL = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/;

  function session() { return CAD.store.get('cad-session', null); }

  /* =================== Chưa đăng nhập =================== */
  function renderAuth() {
    var wantRegister = new URLSearchParams(location.search).get('tab') === 'register';
    document.title = 'Đăng nhập | Cadence';
    root.innerHTML =
      '<div class="auth">' +
        '<div class="auth-tabs" role="tablist">' +
          '<button type="button" role="tab" id="tab-login" aria-controls="p-login" aria-selected="' + !wantRegister + '">Đăng nhập</button>' +
          '<button type="button" role="tab" id="tab-reg" aria-controls="p-reg" aria-selected="' + wantRegister + '">Tạo tài khoản</button>' +
        '</div>' +
        '<section class="auth__panel" id="p-login" role="tabpanel" aria-labelledby="tab-login"' + (wantRegister ? ' hidden' : '') + '>' +
          '<div class="auth__head"><h1 class="h2">Đăng nhập</h1><p>Dành cho hội viên Cadence Club. Mua hàng không cần tài khoản vẫn được.</p></div>' +
          '<form class="form-grid" id="login-form" novalidate>' +
            '<div id="login-alert"></div>' +
            '<div class="field"><label for="li-email">Email</label><input class="input" id="li-email" type="email" autocomplete="email" required><span class="field__error">Nhập email hợp lệ, ví dụ ten@gmail.com.</span></div>' +
            '<div class="field"><label for="li-pw">Mật khẩu</label><div class="pw-wrap"><input class="input" id="li-pw" type="password" autocomplete="current-password" required>' +
              '<button class="pw-toggle" type="button" data-pw="li-pw" aria-label="Hiện mật khẩu">Hiện</button></div><span class="field__error">Mật khẩu có ít nhất 8 ký tự.</span></div>' +
            '<button class="link" type="button" id="forgot" style="justify-self:start">Quên mật khẩu?</button>' +
            '<button class="btn btn--block" type="submit">Đăng nhập</button>' +
          '</form>' +
          '<p class="auth__alt">Đã mua không cần tài khoản? <a class="link" href="order-lookup.html">Tra cứu đơn bằng mã đơn</a></p>' +
        '</section>' +
        '<section class="auth__panel" id="p-reg" role="tabpanel" aria-labelledby="tab-reg"' + (wantRegister ? '' : ' hidden') + '>' +
          '<div class="auth__head"><h2 class="h2">Tạo tài khoản</h2><p>Gia nhập Cadence Club miễn phí.</p></div>' +
          '<ul class="auth__perks"><li>Giao hàng miễn phí mọi đơn</li><li>Quyền bốc thăm hàng giới hạn</li><li>Giảm 10% trong tháng sinh nhật</li></ul>' +
          '<form class="form-grid" id="reg-form" novalidate>' +
            '<div class="field"><label for="rg-name">Họ và tên</label><input class="input" id="rg-name" autocomplete="name" required><span class="field__error">Nhập họ và tên.</span></div>' +
            '<div class="field"><label for="rg-email">Email</label><input class="input" id="rg-email" type="email" autocomplete="email" required><span class="field__error">Nhập email hợp lệ.</span></div>' +
            '<div class="field"><label for="rg-pw">Mật khẩu</label><div class="pw-wrap"><input class="input" id="rg-pw" type="password" autocomplete="new-password" required aria-describedby="pw-rules">' +
              '<button class="pw-toggle" type="button" data-pw="rg-pw" aria-label="Hiện mật khẩu">Hiện</button></div>' +
              '<ul class="pw-rules" id="pw-rules"><li data-rule="len">Ít nhất 8 ký tự</li><li data-rule="mix">Có cả chữ và số</li></ul><span class="field__error">Mật khẩu cần ít nhất 8 ký tự, có cả chữ và số.</span></div>' +
            '<label class="check"><input type="checkbox" id="rg-terms"><span>Tôi đồng ý với <a class="link" href="#">Điều khoản</a> và <a class="link" href="#">Chính sách bảo mật</a>.</span></label>' +
            '<p class="form-error" id="terms-err" hidden>Bạn cần đồng ý với điều khoản để tạo tài khoản.</p>' +
            '<label class="check"><input type="checkbox" id="rg-mkt"><span>Nhận tin ra mắt và ưu đãi qua email (không bắt buộc).</span></label>' +
            '<button class="btn btn--block" type="submit">Tạo tài khoản</button>' +
          '</form>' +
        '</section>' +
      '</div>';

    /* Tabs (chỉ hiện trên mobile; desktop hiển thị 2 cột) */
    $$('[role=tab]').forEach(function (t) {
      t.addEventListener('click', function () {
        $$('[role=tab]').forEach(function (x) { x.setAttribute('aria-selected', x === t); $('#' + x.getAttribute('aria-controls')).hidden = x !== t; });
      });
    });
    if (!renderAuth.bound) renderAuth.bound = true, root.addEventListener('click', function (e) {
      var pt = e.target.closest('[data-pw]');
      if (pt) { var i = $('#' + pt.getAttribute('data-pw')), show = i.type === 'password'; i.type = show ? 'text' : 'password'; pt.textContent = show ? 'Ẩn' : 'Hiện'; pt.setAttribute('aria-label', show ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'); }
      if (e.target.id === 'forgot') CAD.toast('Đã gửi liên kết đặt lại mật khẩu nếu email có trong hệ thống.');
    });
    $('#rg-pw').addEventListener('input', function () {
      var v = this.value;
      $('[data-rule=len]').classList.toggle('is-ok', v.length >= 8);
      $('[data-rule=mix]').classList.toggle('is-ok', /[a-zA-Z]/.test(v) && /\d/.test(v));
    });
    function mark(id, ok) { var el = $('#' + id); el.closest('.field').classList.toggle('is-invalid', !ok); el.setAttribute('aria-invalid', !ok); return ok; }

    $('#login-form').addEventListener('submit', function (e) {
      e.preventDefault();
      var ok = mark('li-email', EMAIL.test($('#li-email').value)) & mark('li-pw', $('#li-pw').value.length >= 8);
      if (!ok) { $('.is-invalid input', this).focus(); return; }
      var email = $('#li-email').value.trim().toLowerCase();
      login({ name: email.split('@')[0], email: email });
    });
    $('#reg-form').addEventListener('submit', function (e) {
      e.preventDefault();
      var pw = $('#rg-pw').value;
      var okPw = pw.length >= 8 && /[a-zA-Z]/.test(pw) && /\d/.test(pw);
      var ok = mark('rg-name', $('#rg-name').value.trim().length > 1) & mark('rg-email', EMAIL.test($('#rg-email').value)) & mark('rg-pw', okPw);
      $('#terms-err').hidden = $('#rg-terms').checked;
      if (!ok || !$('#rg-terms').checked) { var f = $('.is-invalid input', this) || $('#rg-terms'); f.focus(); return; }
      login({ name: $('#rg-name').value.trim(), email: $('#rg-email').value.trim().toLowerCase(), isNew: true });
    });
  }

  function login(user) {
    user.joinedAt = Date.now();
    CAD.store.set('cad-session', user);   /* chỉ lưu tên + email, KHÔNG lưu mật khẩu */
    var next = new URLSearchParams(location.search).get('next');
    if (next && /^[a-z-]+\.html$/.test(next)) { location.href = next; return; }   /* chỉ cho về trang nội bộ */
    location.hash = '#overview';
    render();
    CAD.toast(user.isNew ? 'Chào mừng bạn đến với Cadence Club' : 'Đã đăng nhập');
  }

  /* =================== Đã đăng nhập =================== */
  var SECTIONS = [['overview', 'Tổng quan'], ['orders', 'Đơn hàng'], ['wishlist', 'Yêu thích'], ['addresses', 'Sổ địa chỉ']];

  function ordersOf(u) {
    var linked = CAD.store.get('cad-linked', []);
    var all = CAD.store.get('cad-orders', []).filter(function (o) { return o.contact.email.toLowerCase() === u.email; });
    return {
      linked: all.filter(function (o) { return linked.indexOf(o.number) > -1; }),
      unlinked: all.filter(function (o) { return linked.indexOf(o.number) === -1; })
    };
  }

  function overview(u) {
    var o = ordersOf(u).linked;
    var earned = o.filter(function (x) { return x.fulfillment === 'DELIVERED'; }).reduce(function (s, x) { return s + Math.floor(x.totals.total / 10000); }, 0);
    var pending = o.filter(function (x) { return x.status !== 'CANCELLED' && x.fulfillment !== 'DELIVERED' && x.payment.status !== 'FAILED' && x.payment.status !== 'EXPIRED'; })
      .reduce(function (s, x) { return s + Math.floor(x.totals.total / 10000); }, 0);
    var spend = o.filter(function (x) { return x.status !== 'CANCELLED'; }).reduce(function (s, x) { return s + x.totals.total; }, 0);
    var goal = 5000000;
    return '<div class="acct-body__head"><h1 class="h1">Chào ' + u.name + '</h1><p>' + u.email + '</p></div>' +
      '<div class="club-card">' +
        '<div class="club-card__top"><span class="wordmark">Cadence Club</span><span class="club-card__tier">Hạng ' + (spend >= goal ? 'Pace' : 'Member') + '</span></div>' +
        '<div class="club-card__points"><strong>' + earned + '</strong><span>điểm' + (pending ? ' · ' + pending + ' điểm chờ ghi nhận' : '') + '</span></div>' +
        '<div class="club-card__goal"><div class="club-card__progress"><span style="width:' + Math.min(100, spend / goal * 100) + '%"></span></div>' +
        '<span>' + (spend >= goal ? 'Bạn đã đạt hạng Pace' : 'Chi tiêu thêm ' + CAD.formatPrice(goal - spend) + ' trong 12 tháng để lên hạng Pace') + '</span></div>' +
        '<p class="club-card__fine">Điểm được ghi nhận khi đơn giao thành công, và bị trừ lại nếu bạn trả hàng.</p>' +
      '</div>' +
      claimBanner(u) +
      '<section class="result__section" aria-labelledby="perk-title"><h2 class="h3" id="perk-title">Quyền lợi hội viên</h2><ul class="club__perks">' +
        '<li><span class="label">Giao hàng</span><span>Miễn phí cho mọi đơn khi đăng nhập.</span></li>' +
        '<li><span class="label">Ra mắt</span><span>Đăng ký bốc thăm Velocity Racer, Saigon Edition từ 20.12.</span></li>' +
        '<li><span class="label">Run Club</span><span>Chạy nhóm 5:30 sáng thứ Bảy tại Sài Gòn và Hà Nội.</span></li>' +
      '</ul></section>';
  }

  function claimBanner(u) {
    var un = ordersOf(u).unlinked;
    if (!un.length) return '';
    var sent = CAD.store.get('cad-claim-sent', false);
    return '<div class="notice notice--warn"><strong>Có ' + un.length + ' đơn đã mua bằng ' + u.email + ' khi chưa đăng nhập</strong>' +
      (sent ? '<span>Chúng tôi đã gửi email xác minh. Bấm vào liên kết trong email để gắn các đơn này vào tài khoản.</span><button class="link" type="button" data-claim="verify">Mô phỏng: tôi đã bấm liên kết xác minh</button>'
            : '<span>Để bảo vệ thông tin, bạn cần xác minh email trước khi các đơn này hiện trong tài khoản.</span><button class="link" type="button" data-claim="send">Gửi email xác minh</button>') +
    '</div>';
  }

  function ordersView(u) {
    var o = ordersOf(u).linked;
    return '<div class="acct-body__head"><h1 class="h1">Đơn hàng</h1></div>' + claimBanner(u) +
      (o.length ? '<div class="orders">' + o.map(function (x) {
        return '<a class="orow" href="#order/' + x.number + '"><div class="orow__main"><span class="orow__num">' + x.number + '</span>' +
          '<span class="orow__meta">' + CAD.fmtWhen(x.createdAt) + '</span>' +
          '<span class="axes">' + CAD.statusPill('order', x.status) + CAD.statusPill('ful', x.fulfillment || 'UNFULFILLED') + '</span>' +
          '<div class="orow__thumbs">' + x.lines.slice(0, 4).map(function (l) { return '<img src="' + CAD.img(l.img, 120) + '" alt="" width="120" height="120" decoding="async" loading="lazy">'; }).join('') + '</div></div>' +
          '<span class="orow__total">' + CAD.formatPrice(x.totals.total) + '<svg class="icon" aria-hidden="true"><use href="#i-chev-r"/></svg></span></a>';
      }).join('') + '</div>'
      : '<div class="acct-empty"><p>Bạn chưa có đơn hàng nào trong tài khoản.</p><a class="btn" href="plp.html">Bắt đầu mua sắm</a></div>');
  }

  function wishlistView() {
    var list = CAD.store.get('cad-wish', []).map(CAD.find).filter(Boolean);
    return '<div class="acct-body__head"><h1 class="h1">Yêu thích</h1></div>' +
      (list.length ? '<div class="grid">' + list.map(CAD.card).join('') + '</div>'
        : '<div class="acct-empty"><p>Bấm biểu tượng trái tim trên sản phẩm để lưu vào đây.</p><a class="btn" href="plp.html">Xem sản phẩm</a></div>');
  }

  function addressesView() {
    var list = CAD.store.get('cad-addresses', []);
    return '<div class="acct-body__head"><h1 class="h1">Sổ địa chỉ</h1></div><div class="addr-grid">' +
      list.map(function (a, i) {
        return '<div class="addr">' + (a.default ? '<span class="badge badge--accent addr__default">Mặc định</span>' : '') + '<strong>' + a.name + '</strong><span>' + a.phone + '</span>' +
          '<span class="muted">' + a.street + ', ' + a.ward + ', ' + a.province + '</span>' +
          '<div class="addr__actions">' + (a.default ? '' : '<button class="link" type="button" data-addr-default="' + i + '">Đặt mặc định</button>') +
          '<button class="link" type="button" data-addr-del="' + i + '">Xoá</button></div></div>';
      }).join('') +
      '<button class="addr addr--add" type="button" id="addr-add"><svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M12 5v14M5 12h14"/></svg>Thêm địa chỉ</button></div>';
  }

  function render() {
    var u = session();
    if (!u) { renderAuth(); return; }
    var hash = (location.hash || '#overview').slice(1);
    var sec = hash.indexOf('order/') === 0 ? 'orders' : hash;
    if (!SECTIONS.some(function (s) { return s[0] === sec; })) sec = 'overview';
    var body;
    if (hash.indexOf('order/') === 0) {
      var o = CAD.getOrder(hash.slice(6));
      body = o ? CAD.backLink('Tất cả đơn hàng', { href: '#orders' }) + CAD.orderDetailHtml(o) : ordersView(u);
    } else body = { overview: overview, orders: ordersView, wishlist: wishlistView, addresses: addressesView }[sec](u);

    document.title = SECTIONS.filter(function (s) { return s[0] === sec; })[0][1] + ' | Tài khoản | Cadence';
    root.innerHTML = '<div class="acct">' +
      '<nav class="acct-nav" aria-label="Tài khoản">' + SECTIONS.map(function (s) {
        return '<a href="#' + s[0] + '"' + (s[0] === sec ? ' aria-current="page"' : '') + '>' + s[1] + '</a>';
      }).join('') + '<a href="#" id="logout">Đăng xuất</a></nav>' +
      '<section id="acct-body" class="acct-body">' + body + '</section></div>';
  }

  /* ---------- Sự kiện khi đã đăng nhập ---------- */
  root.addEventListener('click', function (e) {
    var u = session(); if (!u) return;
    if (e.target.id === 'logout') { e.preventDefault(); CAD.store.set('cad-session', null); location.hash = ''; render(); CAD.toast('Đã đăng xuất'); return; }
    var c = e.target.closest('[data-claim]');
    if (c) {
      if (c.getAttribute('data-claim') === 'send') { CAD.store.set('cad-claim-sent', true); CAD.toast('Đã gửi email xác minh tới ' + u.email); }
      else {
        var nums = ordersOf(u).unlinked.map(function (o) { return o.number; });
        CAD.store.set('cad-linked', CAD.store.get('cad-linked', []).concat(nums));
        CAD.store.set('cad-claim-sent', false);
        CAD.toast('Đã gắn ' + nums.length + ' đơn vào tài khoản');
      }
      render(); return;
    }
    var act = e.target.closest('[data-order-act]');
    if (act) {
      var o = CAD.getOrder(location.hash.slice(7));
      if (act.getAttribute('data-order-act') === 'cancel') CAD.cancelDialog(o, function () { render(); CAD.toast('Đã huỷ đơn ' + o.number); });
      else CAD.toast('Luồng đổi size / trả hàng thuộc giai đoạn 2 của dự án.');
      return;
    }
    var list = CAD.store.get('cad-addresses', []);
    var del = e.target.closest('[data-addr-del]');
    if (del) { list.splice(+del.getAttribute('data-addr-del'), 1); if (list.length && !list.some(function (a) { return a.default; })) list[0].default = true; CAD.store.set('cad-addresses', list); render(); return; }
    var def = e.target.closest('[data-addr-default]');
    if (def) { list.forEach(function (a, i) { a.default = i === +def.getAttribute('data-addr-default'); }); CAD.store.set('cad-addresses', list); render(); return; }
    if (e.target.closest('#addr-add')) addressDialog();
  });

  function addressDialog() {
    var d = $('#addr-dialog');
    if (!d) {
      document.body.insertAdjacentHTML('beforeend',
        '<dialog class="dialog" id="addr-dialog" aria-labelledby="ad-title"><form class="dialog__body" id="addr-form" novalidate>' +
          '<h2 class="h3" id="ad-title">Thêm địa chỉ</h2>' +
          '<div class="field"><label for="ad-name">Họ và tên</label><input class="input" id="ad-name" autocomplete="name"><span class="field__error">Nhập họ và tên.</span></div>' +
          '<div class="field"><label for="ad-phone">Số điện thoại</label><input class="input" id="ad-phone" type="tel" inputmode="numeric" autocomplete="tel"><span class="field__error">Số điện thoại 10 số, bắt đầu bằng 0.</span></div>' +
          '<div class="field"><label for="ad-province">Tỉnh / Thành phố</label><select class="select" id="ad-province"><option value="">Chọn tỉnh / thành phố</option>' +
            CAD.PROVINCES.map(function (p) { return '<option>' + p + '</option>'; }).join('') + '</select><span class="field__error">Chọn tỉnh / thành phố.</span></div>' +
          '<div class="field"><label for="ad-ward">Phường / Xã</label><input class="input" id="ad-ward"><span class="field__error">Nhập phường / xã.</span></div>' +
          '<div class="field"><label for="ad-street">Số nhà, tên đường</label><input class="input" id="ad-street" autocomplete="address-line1"><span class="field__error">Nhập số nhà, tên đường.</span></div>' +
          '<div class="dialog__actions"><button class="btn btn--outline" type="button" id="ad-cancel">Huỷ</button><button class="btn" type="submit">Lưu địa chỉ</button></div>' +
        '</form></dialog>');
      d = $('#addr-dialog');
      $('#ad-cancel').addEventListener('click', function () { d.close(); });
      $('#addr-form').addEventListener('submit', function (e) {
        e.preventDefault();
        var v = function (id) { return $('#' + id).value.trim(); };
        var checks = { 'ad-name': v('ad-name').length > 1, 'ad-phone': /^0\d{9}$/.test(v('ad-phone')), 'ad-province': !!v('ad-province'), 'ad-ward': v('ad-ward').length > 1, 'ad-street': v('ad-street').length > 2 };
        var first = null;
        Object.keys(checks).forEach(function (k) { $('#' + k).closest('.field').classList.toggle('is-invalid', !checks[k]); $('#' + k).setAttribute('aria-invalid', !checks[k]); if (!checks[k] && !first) first = k; });
        if (first) { $('#' + first).focus(); return; }
        var list = CAD.store.get('cad-addresses', []);
        list.push({ name: v('ad-name'), phone: v('ad-phone'), province: v('ad-province'), ward: v('ad-ward'), street: v('ad-street'), default: !list.length });
        CAD.store.set('cad-addresses', list);
        d.close(); this.reset(); render(); CAD.toast('Đã lưu địa chỉ');
      });
    }
    d.showModal();
  }

  window.addEventListener('hashchange', render);
  render();
})();
