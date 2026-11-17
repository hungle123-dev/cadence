/* Hành vi dùng chung cho mọi trang prototype CADENCE */
(function () {
  var $ = function (s, r) { return (r || document).querySelector(s); };
  var $$ = function (s, r) { return Array.prototype.slice.call((r || document).querySelectorAll(s)); };
  var FREE_SHIP = 1500000;

  /* ---------- Trạng thái prototype (giỏ, yêu thích) ---------- */
  var store = {
    get: function (k, d) { try { var v = localStorage.getItem(k); return v ? JSON.parse(v) : d; } catch (e) { return d; } },
    set: function (k, v) { try { localStorage.setItem(k, JSON.stringify(v)); } catch (e) { /* bỏ qua */ } }
  };
  CAD.store = store;
  CAD.getCart = function () { return store.get('cad-cart', []); };
  /* Bỏ sản phẩm không còn trong danh mục khỏi giỏ và yêu thích (dữ liệu cũ trong localStorage) */
  (function () {
    var has = function (slug) { return CAD.products.some(function (p) { return p.slug === slug; }); };
    var c = store.get('cad-cart', []), c2 = c.filter(function (i) { return has(i.slug); });
    if (c2.length !== c.length) store.set('cad-cart', c2);
    var w = store.get('cad-wish', []), w2 = w.filter(has);
    if (w2.length !== w.length) store.set('cad-wish', w2);
  })();
  CAD.setCart = function (items) { store.set('cad-cart', items); renderCart(); };
  CAD.find = function (slug) { return CAD.products.filter(function (p) { return p.slug === slug; })[0]; };
  /* Thêm vào giỏ: cùng sản phẩm + màu + size thì cộng dồn số lượng */
  CAD.addToCart = function (slug, color, size, qty) {
    var items = CAD.getCart();
    var hit = items.filter(function (i) { return i.slug === slug && i.color === color && i.size === size; })[0];
    /* priceAtAdd: để giỏ hàng phát hiện "giá đã thay đổi" khi đọc lại (giá trong giỏ không phải nguồn sự thật) */
    var p = CAD.find(slug);
    if (hit) hit.qty += qty; else items.push({ slug: slug, color: color, size: size, qty: qty, priceAtAdd: p ? CAD.unitPrice(p) : null });
    CAD.setCart(items);
  };

  /* ---------- Toast ---------- */
  var toastTimer;
  CAD.toast = function (msg) {
    var t = $('#toast'); if (!t) return;
    t.textContent = msg; t.classList.add('is-visible');
    clearTimeout(toastTimer);
    toastTimer = setTimeout(function () { t.classList.remove('is-visible'); }, 2400);
  };

  /* ---------- Header: ẩn khi cuộn xuống, hiện khi cuộn lên ---------- */
  var header = $('.header'), lastY = window.scrollY;
  if (header) {
    window.addEventListener('scroll', function () {
      var y = window.scrollY;
      header.classList.toggle('is-hidden', y > lastY && y > 240 && !document.body.classList.contains('drawer-open'));
      lastY = y;
    }, { passive: true });
  }

  /* ---------- Drawers ---------- */
  var overlay = $('.overlay'), lastFocus = null;
  function openDrawer(d) {
    lastFocus = document.activeElement;
    d.classList.add('is-open'); d.removeAttribute('inert');
    overlay.classList.add('is-open');
    document.body.classList.add('drawer-open');
    document.body.style.overflow = 'hidden';
    var f = $('button, a', d); if (f) setTimeout(function () { f.focus(); }, 50);
  }
  function closeDrawers() {
    $$('.drawer.is-open').forEach(function (d) { d.classList.remove('is-open'); d.setAttribute('inert', ''); });
    if (overlay) overlay.classList.remove('is-open');
    document.body.classList.remove('drawer-open');
    document.body.style.overflow = '';
    if (lastFocus) lastFocus.focus();
  }
  CAD.openCart = function () { var d = $('#cart-drawer'); if (d) openDrawer(d); };
  CAD.openDrawer = function (id) { var d = $('#' + id); if (d) openDrawer(d); };
  $$('[data-open]').forEach(function (b) { b.addEventListener('click', function () { var d = $('#' + b.getAttribute('data-open')); if (d) openDrawer(d); }); });
  $$('[data-close]').forEach(function (b) { b.addEventListener('click', closeDrawers); });
  if (overlay) overlay.addEventListener('click', closeDrawers);
  document.addEventListener('keydown', function (e) { if (e.key === 'Escape') closeDrawers(); });

  /* ---------- Giỏ hàng mini ---------- */
  function renderCart() {
    var items = CAD.getCart();
    var count = items.reduce(function (s, i) { return s + i.qty; }, 0);
    $$('[data-cart-count]').forEach(function (el) {
      var prev = +el.getAttribute('data-count') || 0;
      el.textContent = count; el.setAttribute('data-count', count);
      if (count > prev) { el.classList.remove('is-bump'); void el.offsetWidth; el.classList.add('is-bump'); }
    });
    var body = $('#cart-body'), foot = $('#cart-foot');
    if (!body) return;
    if (!items.length) {
      body.innerHTML = '<div class="cart-empty"><p class="h3">Giỏ hàng của bạn đang trống</p>' +
        '<p class="muted">Sản phẩm bạn thêm vào sẽ hiện ở đây.</p>' +
        '<a class="btn" href="plp.html" style="margin-top:12px">Tiếp tục mua sắm</a></div>';
      if (foot) foot.hidden = true;
      return;
    }
    var subtotal = 0;
    body.innerHTML = items.map(function (i) {
      var p = CAD.find(i.slug); if (!p) return '';
      var unit = p.salePrice || p.price; subtotal += unit * i.qty;
      return '<div class="cart-line"><img src="' + CAD.img(p.img, 200) + '" alt="" width="200" height="250" decoding="async">' +
        '<div><div style="font-weight:600">' + p.name + '</div><div class="muted" style="font-size:.875rem">' + (i.color || p.category) + ' · ' + (i.size === 'OS' ? 'Một size' : 'Size ' + i.size) + ' · SL ' + i.qty + '</div></div>' +
        '<div class="mono" style="font-size:.875rem">' + CAD.formatPrice(unit * i.qty) + '</div></div>';
    }).join('');
    if (foot) {
      foot.hidden = false;
      var remain = Math.max(0, FREE_SHIP - subtotal);
      $('#cart-subtotal').textContent = CAD.formatPrice(subtotal);
      $('#ship-text').textContent = remain > 0 ? 'Thêm ' + CAD.formatPrice(remain) + ' để được miễn phí giao hàng' : 'Đơn của bạn được miễn phí giao hàng';
      $('#ship-bar').style.width = Math.min(100, subtotal / FREE_SHIP * 100) + '%';
    }
  }
  CAD.renderCart = renderCart;

  /* ---------- Thẻ sản phẩm ---------- */
  var BADGE = { 'Mới': 'badge--accent', 'Bán chạy': '', 'Sắp hết size': '', 'Thành viên': 'badge--dark' };
  CAD.badge = function (p) {
    if (!p.flag || p.flag === 'Sale') return '';
    return '<span class="badge ' + (BADGE[p.flag] || '') + '">' + (p.flag === 'Thành viên' ? 'Hội viên' : p.flag) + '</span>';
  };
  CAD.priceHtml = function (p) {
    if (!p.salePrice) return '<span class="price__now">' + CAD.formatPrice(p.price) + '</span>';
    var off = Math.round((1 - p.salePrice / p.price) * 100);
    return '<span class="price__now price__now--sale">' + CAD.formatPrice(p.salePrice) + '</span><span class="price__was"><span class="sr-only">Giá gốc </span>' + CAD.formatPrice(p.price) + '</span><span class="price__off">Giảm ' + off + '%</span>';
  };
  CAD.card = function (p) {
    var badge = CAD.badge(p);
    var sizes = Object.keys(p.sizes).map(function (s) {
      return p.sizes[s]
        ? '<button type="button" data-quick="' + s + '" aria-label="Thêm ' + p.name + ' size ' + s + ' vào giỏ">' + s + '</button>'
        : '<span class="is-out" aria-label="Size ' + s + ' hết hàng">' + s + '</span>';
    }).join('');
    var wished = store.get('cad-wish', []).indexOf(p.slug) > -1;
    var who = p.gender && p.gender !== 'unisex' ? ' ' + CAD.labels.gender[p.gender].toLowerCase() : '';
    return '<article class="card" data-slug="' + p.slug + '">' +
      '<div class="card__media">' + (badge ? '<div class="card__badges">' + badge + '</div>' : '') +
        '<img class="card__img" src="' + CAD.img(p.img, 700) + '" alt="' + p.name + '" loading="lazy" width="700" height="700"' + (p.pos ? ' style="object-position:' + p.pos + '"' : '') + '>' +
        (p.gallery && p.gallery[0] ? '<img class="card__img card__img--alt" src="' + CAD.img(p.gallery[0], 700) + '" alt="" loading="lazy" width="700" height="700">' : '') +
        '<div class="card__sizes" role="group" aria-label="Thêm nhanh vào giỏ, chọn size"><span class="card__sizes-label">Thêm nhanh</span>' + sizes + '</div>' +
      '</div>' +
      '<button class="card__wish" type="button" aria-pressed="' + wished + '" aria-label="Lưu ' + p.name + ' vào yêu thích"><svg class="icon icon--sm"><use href="#i-heart"/></svg></button>' +
      '<div class="card__body">' +
        '<h3 class="card__name"><a href="pdp.html?p=' + p.slug + '">' + p.name + '</a></h3>' +
        '<p class="card__meta">' + p.category + who + '</p>' +
        '<p class="card__meta">' + p.colors.length + ' màu</p>' +
        '<p class="price">' + CAD.priceHtml(p) + '</p>' +
      '</div></article>';
  };

  /* Ảnh thẻ: hiện dần khi tải xong (kể cả ảnh đã có trong cache) */
  function markLoaded(img) { img.classList.add('is-loaded'); }
  document.addEventListener('load', function (e) { if (e.target.classList && e.target.classList.contains('card__img')) markLoaded(e.target); }, true);
  new MutationObserver(function () {
    $$('.card__img:not(.is-loaded)').forEach(function (img) { if (img.complete && img.naturalWidth) markLoaded(img); });
  }).observe(document.body, { childList: true, subtree: true });

  /* Thêm nhanh từ thẻ sản phẩm: màu đầu tiên + size đã chọn */
  document.addEventListener('click', function (e) {
    var q = e.target.closest('[data-quick]');
    if (!q) return;
    var card = q.closest('.card'), p = CAD.find(card.getAttribute('data-slug'));
    CAD.addToCart(p.slug, p.colors[0].name, q.getAttribute('data-quick'), 1);
    q.classList.add('is-added');
    setTimeout(function () { q.classList.remove('is-added'); }, 900);
    CAD.toast('Đã thêm ' + p.name + ' · size ' + q.getAttribute('data-quick'));
  });

  document.addEventListener('click', function (e) {
    var wish = e.target.closest('.card__wish');
    if (!wish) return;
    var slug = wish.closest('.card').getAttribute('data-slug');
    var list = store.get('cad-wish', []);
    var on = list.indexOf(slug) === -1;
    store.set('cad-wish', on ? list.concat(slug) : list.filter(function (s) { return s !== slug; }));
    wish.setAttribute('aria-pressed', on);
    CAD.toast(on ? 'Đã lưu vào yêu thích' : 'Đã bỏ khỏi yêu thích');
  });

  /* ---------- Lưới / dải sản phẩm: lọc theo data-where="key:value;key:value", tab lọc theo loại ---------- */
  $$('[data-grid]').forEach(function (grid) {
    var limit = +grid.getAttribute('data-limit') || 8;
    var where = (grid.getAttribute('data-where') || '').split(';').filter(Boolean).map(function (c) { return c.split(':'); });
    var sortBy = grid.getAttribute('data-sort') || 'rank';
    function ok(p) {
      return where.every(function (w) {
        if (w[0] === 'sale') return !!p.salePrice;
        if (w[0] === 'gender') return p.gender === w[1] || (p.gender === 'unisex' && w[1] !== 'tre-em');
        return String(p[w[0]]) === w[1];
      });
    }
    var sorters = {
      rank: function (a, b) { return a.rank - b.rank; },
      new: function (a, b) { return (b.flag === 'Mới') - (a.flag === 'Mới') || a.rank - b.rank; },
      rating: function (a, b) { return b.rating - a.rating || a.rank - b.rank; }
    };
    function draw(type) {
      var list = CAD.products.filter(function (p) { return ok(p) && (type === 'all' || p.type === type); })
        .sort(sorters[sortBy] || sorters.rank).slice(0, limit);
      grid.innerHTML = list.map(CAD.card).join('');
    }
    var tabs = $$('[data-tab]', grid.closest('section') || grid.parentElement);
    tabs.forEach(function (t) {
      t.addEventListener('click', function () {
        tabs.forEach(function (x) { x.setAttribute('aria-selected', x === t); });
        draw(t.getAttribute('data-tab'));
      });
    });
    draw('all');
  });

  /* ---------- Newsletter ---------- */
  $$('[data-newsletter]').forEach(function (f) {
    f.addEventListener('submit', function (e) { e.preventDefault(); CAD.toast('Đã đăng ký. Kiểm tra email để nhận mã giảm 10%.'); f.reset(); });
  });

  /* ---------- Tìm kiếm (overlay) ---------- */
  var search = $('#search');
  if (search) {
    var input = $('#search-input'), results = $('#search-results'), searchReturn = null, timer;
    var POPULAR = ['Giày chạy bộ', 'Tempo', 'Áo thun', 'Trail', 'Giày lifestyle'];
    var CATS = [
      ['Giày chạy bộ', 'plp.html?s=chay-bo&type=shoes'], ['Giày trail', 'plp.html?s=trail'], ['Giày lifestyle', 'plp.html?s=lifestyle&type=shoes'],
      ['Áo thun & áo chạy', 'plp.html?type=apparel'], ['Tập luyện', 'plp.html?s=tap-luyen'], ['Nam', 'plp.html?g=nam'], ['Nữ', 'plp.html?g=nu'], ['Sale', 'plp.html?sale=1']
    ];
    var recent = function () { return store.get('cad-recent', []); };
    var remember = function (q) { q = q.trim(); if (!q) return; store.set('cad-recent', [q].concat(recent().filter(function (x) { return CAD.norm(x) !== CAD.norm(q); })).slice(0, 5)); };

    var prodHtml = function (p, q) {
      return '<a class="sprod" href="pdp.html?p=' + p.slug + '" data-sr-item><div class="sprod__img"><img src="' + CAD.img(p.img, 400) + '" alt="" width="400" height="500" decoding="async" loading="lazy"></div>' +
        '<span style="font-weight:600">' + CAD.highlight(p.name, q) + '</span><span class="muted">' + p.category + '</span><span class="mono">' + CAD.formatPrice(CAD.unitPrice(p)) + '</span></a>';
    };
    var renderSearch = function () {
      var q = input.value.trim();
      input.setAttribute('aria-expanded', 'true');
      if (!q) {
        var r = recent();
        results.innerHTML =
          '<div class="search__col">' +
            (r.length ? '<div style="display:flex;justify-content:space-between;align-items:baseline"><h2 class="label">Tìm gần đây</h2><button class="link" type="button" id="recent-clear">Xoá</button></div>' +
              '<div class="search__links">' + r.map(function (x) { return '<a href="plp.html?q=' + encodeURIComponent(x) + '" data-sr-item data-q="' + x + '">' + x + '</a>'; }).join('') + '</div>' : '') +
            '<h2 class="label">Tìm kiếm phổ biến</h2><div class="search__chips">' + POPULAR.map(function (x) { return '<button class="chip" type="button" data-fill="' + x + '" data-sr-item>' + x + '</button>'; }).join('') + '</div>' +
          '</div>' +
          '<div class="search__col"><h2 class="label">Bán chạy</h2><div class="search__products">' +
            CAD.products.slice().sort(function (a, b) { return a.rank - b.rank; }).slice(0, 4).map(function (p) { return prodHtml(p, ''); }).join('') + '</div></div>';
        return;
      }
      var hits = CAD.search(q);
      var nq = CAD.norm(q);
      var cats = CATS.filter(function (c) { return CAD.norm(c[0]).indexOf(nq) > -1 || nq.split(' ').some(function (t) { return t.length > 2 && CAD.norm(c[0]).indexOf(t) > -1; }); });
      if (!hits.length && !cats.length) {
        results.innerHTML = '<div class="search__col" style="grid-column:1/-1"><p class="h3">Không tìm thấy kết quả cho “' + q.replace(/</g, '&lt;') + '”</p>' +
          '<p class="muted">Kiểm tra lại chính tả, hoặc thử từ ngắn hơn như “giày”, “áo”, “trail”.</p>' +
          '<div class="search__chips">' + POPULAR.map(function (x) { return '<button class="chip" type="button" data-fill="' + x + '" data-sr-item>' + x + '</button>'; }).join('') + '</div></div>';
        return;
      }
      results.innerHTML =
        (cats.length ? '<div class="search__col"><h2 class="label">Danh mục</h2><div class="search__links">' +
          cats.map(function (c) { return '<a href="' + c[1] + '" data-sr-item>' + CAD.highlight(c[0], q) + '</a>'; }).join('') + '</div></div>' : '') +
        '<div class="search__col"' + (cats.length ? '' : ' style="grid-column:1/-1"') + '><h2 class="label">Sản phẩm · ' + hits.length + '</h2>' +
          (hits.length ? '<div class="search__products">' + hits.slice(0, 4).map(function (p) { return prodHtml(p, q); }).join('') + '</div>' +
            '<a class="btn btn--outline search__all" href="plp.html?q=' + encodeURIComponent(q) + '" data-sr-item>Xem tất cả ' + hits.length + ' kết quả</a>'
            : '<p class="muted" style="font-size:.875rem">Không có sản phẩm khớp, thử một danh mục bên trái.</p>') +
        '</div>';
    };
    CAD.openSearch = function () {
      searchReturn = document.activeElement;
      search.hidden = false;
      document.body.style.overflow = 'hidden';
      renderSearch();
      setTimeout(function () { input.focus(); input.select(); }, 30);
    };
    var closeSearch = function () {
      search.hidden = true; input.setAttribute('aria-expanded', 'false');
      document.body.style.overflow = '';
      if (searchReturn) searchReturn.focus();
    };
    $$('[data-search-open]').forEach(function (b) { b.addEventListener('click', CAD.openSearch); });
    search.addEventListener('click', function (e) {
      if (e.target.closest('[data-search-close]')) { closeSearch(); return; }
      var f = e.target.closest('[data-fill]');
      if (f) { input.value = f.getAttribute('data-fill'); renderSearch(); input.focus(); return; }
      if (e.target.id === 'recent-clear') { store.set('cad-recent', []); renderSearch(); input.focus(); return; }
      var a = e.target.closest('a[data-sr-item]');
      if (a) remember(a.getAttribute('data-q') || input.value);
    });
    input.addEventListener('input', function () { clearTimeout(timer); timer = setTimeout(renderSearch, 120); });
    $('#search-form').addEventListener('submit', function (e) {
      e.preventDefault();
      var q = input.value.trim(); if (!q) return;
      remember(q);
      location.href = 'plp.html?q=' + encodeURIComponent(q);
    });
    /* Bàn phím: Esc đóng, mũi tên lên/xuống đi qua kết quả, "/" mở tìm kiếm */
    search.addEventListener('keydown', function (e) {
      if (e.key === 'Escape') { e.stopPropagation(); closeSearch(); return; }
      if (e.key !== 'ArrowDown' && e.key !== 'ArrowUp') return;
      var items = $$('[data-sr-item]', results);
      if (!items.length) return;
      e.preventDefault();
      var i = items.indexOf(document.activeElement);
      var next = e.key === 'ArrowDown' ? (i + 1) % items.length : (i <= 0 ? -1 : i - 1);
      if (next === -1) input.focus(); else items[next].focus();
    });
    document.addEventListener('keydown', function (e) {
      if (e.key === '/' && search.hidden && !/INPUT|TEXTAREA|SELECT/.test(document.activeElement.tagName)) { e.preventDefault(); CAD.openSearch(); }
    });
    /* Bẫy focus trong overlay */
    search.addEventListener('keydown', function (e) {
      if (e.key !== 'Tab') return;
      var f = $$('a[href], button, input', search).filter(function (el) { return el.offsetParent !== null; });
      if (!f.length) return;
      if (e.shiftKey && document.activeElement === f[0]) { e.preventDefault(); f[f.length - 1].focus(); }
      else if (!e.shiftKey && document.activeElement === f[f.length - 1]) { e.preventDefault(); f[0].focus(); }
    });
  }

  renderCart();
})();
