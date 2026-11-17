/* PDP: gallery, chọn màu, chọn size (khoá size hết, báo khi có hàng), bảng size,
   độ vừa size, thêm vào giỏ (bắt buộc chọn size), thanh mua dính đáy, sản phẩm liên quan */
(function () {
  var $ = function (s, r) { return (r || document).querySelector(s); };
  var $$ = function (s, r) { return Array.prototype.slice.call((r || document).querySelectorAll(s)); };
  var L = CAD.labels;
  var slug = new URLSearchParams(location.search).get('p') || 'tempo-2';
  var p = CAD.find(slug) || CAD.products[0];
  var colorIdx = 0, size = null;
  var isShoe = p.type === 'shoes';
  var SIZE_ORDER = ['XS', 'S', 'M', 'L', 'XL', '4', '5', '35', '36', '37', '38', '39', '40', '41', '42', '43', '44', 'OS'];

  document.title = p.name + ' | Cadence';

  /* ---------- Breadcrumb ---------- */
  $('#crumbs').innerHTML = '<a href="index.html">Trang chủ</a><span aria-hidden="true">/</span>' +
    '<a href="plp.html?s=' + p.sport + '">' + L.sport[p.sport] + '</a><span aria-hidden="true">/</span>' +
    '<span aria-current="page">' + p.name + '</span>';

  /* ---------- Gallery ---------- */
  var imgs = [p.img].concat(p.gallery);
  $('#gallery').innerHTML = imgs.map(function (id, i) {
    return '<figure class="gallery__item"><img src="' + CAD.img(id, i === 0 ? 1400 : 1000) + '" alt="' + p.name + (i === 0 ? '' : ', ảnh ' + (i + 1)) + '"' + (i === 0 && p.pos ? ' style="object-position:' + p.pos + '"' : '') + (i ? ' loading="lazy"' : ' fetchpriority="high"') + '></figure>';
  }).join('');
  var gc = $('#gallery-count'), active = 0;
  var items = $$('.gallery__item');
  /* Mobile: vuốt ngang, đếm "1 / 3". Desktop: chọn ảnh bằng cột ảnh nhỏ (chỉ hiện ảnh đang chọn). */
  function updCount() {
    var t = $('#gallery');
    var i = Math.round(t.scrollLeft / Math.max(1, t.clientWidth));
    gc.textContent = (i + 1) + ' / ' + imgs.length;
  }
  $('#gallery').addEventListener('scroll', updCount, { passive: true });
  updCount();
  if (imgs.length < 2) gc.hidden = true;

  var thumbs = $('#thumbs');
  thumbs.innerHTML = imgs.map(function (id, i) {
    return '<button class="thumb" type="button" data-i="' + i + '" aria-label="Xem ảnh ' + (i + 1) + '"><img src="' + CAD.img(id, 160) + '" alt="" width="160" height="200" decoding="async"' + (i === 0 && p.pos ? ' style="object-position:' + p.pos + '"' : '') + '></button>';
  }).join('');
  thumbs.hidden = imgs.length < 2;
  function setActive(i) {
    active = (i + imgs.length) % imgs.length;
    items.forEach(function (f, k) { f.classList.toggle('is-active', k === active); f.classList.remove('is-zoom'); });
    $$('.thumb', thumbs).forEach(function (t, k) { t.setAttribute('aria-current', k === active); });
  }
  thumbs.addEventListener('click', function (e) { var b = e.target.closest('.thumb'); if (b) setActive(+b.getAttribute('data-i')); });
  setActive(0);

  /* Phóng to khi rê chuột (desktop có chuột), bấm để xem toàn màn hình */
  var canHover = window.matchMedia('(hover: hover) and (min-width: 1024px)');
  items.forEach(function (f) {
    var img = f.querySelector('img');
    f.addEventListener('mousemove', function (e) {
      if (!canHover.matches) return;
      var r = f.getBoundingClientRect();
      img.style.transformOrigin = ((e.clientX - r.left) / r.width * 100) + '% ' + ((e.clientY - r.top) / r.height * 100) + '%';
      f.classList.add('is-zoom');
    });
    f.addEventListener('mouseleave', function () { f.classList.remove('is-zoom'); });
    f.addEventListener('click', function () { if (canHover.matches) openLightbox(); });
  });
  var lb = $('#lightbox'), lbFrom = null;
  /* Ảnh lightbox tạo bằng JS để HTML không có <img> rỗng src trước khi mở */
  var lbImg = document.createElement('img'); lbImg.id = 'lb-img'; lbImg.alt = '';
  lb.insertBefore(lbImg, $$('.lightbox__nav', lb)[1]);
  function showLb() { lbImg.src = CAD.img(imgs[active], 1800); lbImg.alt = p.name + ', ảnh ' + (active + 1); }
  function openLightbox() { lbFrom = document.activeElement; showLb(); lb.hidden = false; document.body.style.overflow = 'hidden'; $('[data-lb-close]', lb).focus(); }
  function closeLightbox() { lb.hidden = true; document.body.style.overflow = ''; if (lbFrom) lbFrom.focus(); }
  lb.addEventListener('click', function (e) {
    var n = e.target.closest('[data-dir]');
    if (n) { setActive(active + +n.getAttribute('data-dir')); showLb(); return; }
    if (e.target === lb || e.target.closest('[data-lb-close]')) closeLightbox();
  });
  document.addEventListener('keydown', function (e) {
    if (lb.hidden) return;
    if (e.key === 'Escape') closeLightbox();
    if (e.key === 'ArrowRight') { setActive(active + 1); showLb(); }
    if (e.key === 'ArrowLeft') { setActive(active - 1); showLb(); }
  });
  if (imgs.length < 2) $$('.lightbox__nav', lb).forEach(function (b) { b.hidden = true; });

  /* ---------- Thông tin chính ---------- */
  $('#p-name').textContent = p.name;
  /* Dòng meta dưới tên: danh mục + đối tượng. Nhãn "Mới"… đặt trên ảnh, không đặt phía trên tiêu đề */
  $('#p-meta').textContent = p.category + (p.gender !== 'unisex' ? ' ' + L.gender[p.gender].toLowerCase() : '');
  var badge = CAD.badge(p);
  if (badge) $('.gallery').insertAdjacentHTML('beforeend', '<div class="gallery__badge">' + badge + '</div>');
  $('#p-rating').innerHTML = '<span class="stars" aria-hidden="true"><span style="width:' + (p.rating / 5 * 100) + '%"></span></span>' +
    '<span class="mono">' + p.rating.toFixed(1) + '</span><span class="muted" style="text-decoration:underline;text-underline-offset:3px">' + p.reviews + ' đánh giá</span>';
  $('#p-rating').setAttribute('aria-label', 'Đánh giá ' + p.rating + ' trên 5, ' + p.reviews + ' lượt. Xem đánh giá');
  $('#p-price').innerHTML = CAD.priceHtml(p);
  $('#p-desc').innerHTML = '<p>' + p.desc + '</p>';
  $('#p-specs').innerHTML = Object.keys(p.specs).map(function (k) {
    return '<div class="spec__row"><dt>' + k + '</dt><dd>' + p.specs[k] + '</dd></div>';
  }).join('');

  /* ---------- Màu ---------- */
  function renderColors() {
    $('#p-colors').innerHTML = p.colors.map(function (c, i) {
      return '<button class="swatch-btn" type="button" aria-pressed="' + (i === colorIdx) + '" aria-label="' + c.name + '" title="' + c.name + '"><span style="background:' + CAD.colorFamilies[c.family].hex + '"></span></button>';
    }).join('');
    $('#p-color-name').textContent = p.colors[colorIdx].name;
  }
  $('#p-colors').addEventListener('click', function (e) {
    var b = e.target.closest('.swatch-btn'); if (!b) return;
    colorIdx = $$('.swatch-btn', $('#p-colors')).indexOf(b);
    renderColors();
  });
  renderColors();

  /* ---------- Size ---------- */
  $('#size-system').textContent = isShoe ? '(EU)' : '';
  var sizeKeys = SIZE_ORDER.filter(function (s) { return s in p.sizes; });
  /* Phụ kiện: không có độ vừa size và bảng size */
  var isAcc = p.type === 'accessories';
  if (isAcc) { $('#p-fit').hidden = true; $('#size-guide-btn').hidden = true; }
  /* Phụ kiện một size: chọn sẵn nếu còn hàng */
  if (sizeKeys.length === 1 && sizeKeys[0] === 'OS' && p.sizes.OS > 0) size = 'OS';
  /* Chia cột sao cho hàng cuối không lẻ loi: chọn 4–6 cột có số dư lớn nhất (hoặc chia hết) */
  (function () {
    var n = sizeKeys.length, best = Math.min(n, 6), bestRem = -1;
    if (n > 6) {
      [6, 5, 4].forEach(function (c) { var rem = n % c === 0 ? c : n % c; if (rem > bestRem) { bestRem = rem; best = c; } });
    }
    $('#p-sizes').style.gridTemplateColumns = 'repeat(' + best + ', 1fr)';
  })();
  function renderSizes() {
    $('#p-sizes').innerHTML = sizeKeys.map(function (s) {
      var stock = p.sizes[s];
      return '<button type="button" class="size-btn" data-size="' + s + '" aria-pressed="' + (size === s) + '"' +
        (stock ? '' : ' aria-disabled="true"') + ' aria-label="' + (s === 'OS' ? 'Một size' : 'Size ' + s) + (stock ? '' : ', hết hàng') + '">' + (s === 'OS' ? 'Một size' : s) + '</button>';
    }).join('');
  }
  var msg = $('#size-msg');
  function setMsg(text, kind) { msg.className = 'field-msg' + (kind ? ' field-msg--' + kind : ''); msg.innerHTML = text || ''; }
  $('#p-sizes').addEventListener('click', function (e) {
    var b = e.target.closest('.size-btn'); if (!b) return;
    var s = b.getAttribute('data-size');
    $('#p-sizes').classList.remove('has-error');
    if (!p.sizes[s]) {
      setMsg('Size ' + s + ' đã hết hàng. <button class="link" type="button" id="notify-btn">Báo cho tôi khi có hàng</button>', 'warn');
      return;
    }
    size = s; renderSizes();
    var stock = p.sizes[s];
    setMsg(stock <= 2 ? 'Chỉ còn ' + stock + (isShoe ? ' đôi' : ' sản phẩm') + ' size ' + s + '.' : '', stock <= 2 ? 'warn' : '');
    var nb = $('[data-size="' + s + '"]'); if (nb) nb.focus();
  });
  msg.addEventListener('click', function (e) {
    if (e.target.id === 'notify-btn') { CAD.toast('Đã đăng ký. Chúng tôi sẽ gửi email khi size này có hàng lại.'); setMsg(''); }
  });
  renderSizes();

  /* ---------- Độ vừa size (tổng hợp từ đánh giá) ---------- */
  var fitText = p.fit < -0.15 ? 'Hơi chật. Nên chọn lớn hơn nửa size.' : p.fit > 0.15 ? 'Hơi rộng. Cân nhắc chọn nhỏ hơn nửa size.' : 'Vừa đúng size thường mặc.';
  $('#p-fit').innerHTML = '<div class="field-head" style="margin:0"><strong>Độ vừa size</strong><span class="muted">' + fitText + '</span></div>' +
    '<div class="fit__scale" role="img" aria-label="' + fitText + '"><span style="left:' + ((p.fit + 1) / 2 * 100) + '%"></span></div>' +
    '<div class="fit__labels"><span>Chật</span><span>Vừa</span><span>Rộng</span></div>';

  /* ---------- Thêm vào giỏ ---------- */
  function add() {
    if (!size) {
      $('#p-sizes').classList.add('has-error');
      setMsg('Vui lòng chọn size trước khi thêm vào giỏ.', 'error');
      $('#p-sizes').scrollIntoView({ block: 'center', behavior: matchMedia('(prefers-reduced-motion: reduce)').matches ? 'auto' : 'smooth' });
      var first = $('.size-btn:not([aria-disabled])', $('#p-sizes')); if (first) first.focus({ preventScroll: true });
      return;
    }
    CAD.addToCart(p.slug, p.colors[colorIdx].name, size, 1);
    CAD.openCart();
  }
  $('#add-btn').addEventListener('click', add);
  $('#sb-add').addEventListener('click', add);

  /* ---------- Yêu thích ---------- */
  var wb = $('#wish-btn');
  wb.setAttribute('aria-pressed', CAD.store.get('cad-wish', []).indexOf(p.slug) > -1);
  wb.addEventListener('click', function () {
    var list = CAD.store.get('cad-wish', []), on = list.indexOf(p.slug) === -1;
    CAD.store.set('cad-wish', on ? list.concat(p.slug) : list.filter(function (s) { return s !== p.slug; }));
    wb.setAttribute('aria-pressed', on);
    CAD.toast(on ? 'Đã lưu vào yêu thích' : 'Đã bỏ khỏi yêu thích');
  });

  /* ---------- Ngày giao dự kiến ---------- */
  var days = ['Chủ nhật', 'Thứ 2', 'Thứ 3', 'Thứ 4', 'Thứ 5', 'Thứ 6', 'Thứ 7'];
  var d = new Date(); d.setDate(d.getDate() + 2);
  $('#eta').textContent = 'Dự kiến ' + days[d.getDay()] + ', ' + ('0' + d.getDate()).slice(-2) + '.' + ('0' + (d.getMonth() + 1)).slice(-2);

  /* ---------- Bảng size ---------- */
  var shoeRows = [['38', '5.5', '5', '24'], ['39', '6.5', '6', '24.5'], ['40', '7', '6.5', '25'], ['41', '8', '7', '26'], ['42', '8.5', '8', '26.5'], ['43', '9.5', '9', '27.5'], ['44', '10', '9.5', '28']];
  var appRows = [['XS', '82–88', '66–72'], ['S', '88–94', '72–78'], ['M', '94–100', '78–84'], ['L', '100–106', '84–90'], ['XL', '106–112', '90–96']];
  $('#size-guide').innerHTML = isShoe
    ? '<p class="muted" style="font-size:.875rem;margin-bottom:16px">Đo chiều dài bàn chân vào buổi chiều, từ gót tới ngón dài nhất, rồi chọn size có chiều dài bằng hoặc lớn hơn gần nhất.</p>' +
      '<table class="size-table"><thead><tr><th>EU</th><th>US</th><th>UK</th><th>Chiều dài chân (cm)</th></tr></thead><tbody>' +
      shoeRows.map(function (r) { return '<tr><td>' + r.join('</td><td>') + '</td></tr>'; }).join('') + '</tbody></table>' +
      '<p class="muted" style="font-size:.75rem;margin-top:16px">Bảng tham khảo cho giày nam. Giữa hai size, chọn size lớn hơn.</p>'
    : '<p class="muted" style="font-size:.875rem;margin-bottom:16px">Đo vòng ngực ở chỗ rộng nhất và vòng eo ở chỗ nhỏ nhất, thước dây ôm sát nhưng không siết.</p>' +
      '<table class="size-table"><thead><tr><th>Size</th><th>Vòng ngực (cm)</th><th>Vòng eo (cm)</th></tr></thead><tbody>' +
      appRows.map(function (r) { return '<tr><td>' + r.join('</td><td>') + '</td></tr>'; }).join('') + '</tbody></table>';
  $('#size-guide-btn').addEventListener('click', function () { CAD.openDrawer('size-drawer'); });

  /* ---------- Đánh giá (dữ liệu mẫu) ---------- */
  var samples = [
    { name: 'Minh T.', city: 'TP. HCM', stars: 5, size: isShoe ? '42' : 'M',
      text: isShoe ? 'Chạy 21 km cuối tuần vẫn êm, không bị nóng chân. Mình chọn đúng size thường đi là vừa.'
                   : 'Vải mỏng, khô nhanh. Chạy buổi trưa Sài Gòn vẫn không bị dính người. Size M vừa với mình (1m72, 65 kg).' },
    { name: 'Lan P.', city: 'Hà Nội', stars: 4, size: isShoe ? '38' : 'S', text: 'Chất lượng tốt, giao nhanh. Trừ một sao vì màu ngoài đời đậm hơn ảnh một chút.' },
    { name: 'Quang N.', city: 'Đà Nẵng', stars: 5, size: isShoe ? '43' : 'L', text: 'Đã đổi size một lần, được giữ size mới ngay khi gửi yêu cầu nên không lo hết hàng.' }
  ];
  $('#reviews').innerHTML =
    '<div style="display:grid;gap:12px;align-content:start">' +
      '<h2 class="h2" id="rev-title">Đánh giá</h2>' +
      '<div style="display:flex;align-items:baseline;gap:12px"><span class="big-rating">' + p.rating.toFixed(1) + '</span><span class="stars" aria-hidden="true"><span style="width:' + (p.rating / 5 * 100) + '%"></span></span></div>' +
      '<p class="muted" style="font-size:.875rem">' + p.reviews + ' đánh giá đã mua hàng</p>' +
      (isAcc ? '' : '<div class="fit" style="margin-top:12px">' + $('#p-fit').innerHTML + '</div>') +
      '<p class="muted" style="font-size:.75rem">Đánh giá mẫu cho prototype.</p>' +
    '</div>' +
    '<div>' + samples.map(function (r) {
      return '<article class="review"><span class="stars" aria-label="' + r.stars + ' trên 5 sao"><span style="width:' + (r.stars / 5 * 100) + '%"></span></span>' +
        '<p>' + r.text + '</p><p class="review__meta">' + r.name + ' · ' + r.city + ' · Size ' + r.size + ' · Đã mua hàng</p></article>';
    }).join('') + '</div>';

  /* ---------- Sản phẩm liên quan ---------- */
  var related = CAD.products.filter(function (x) { return x.slug !== p.slug && (x.sport === p.sport || x.type === p.type); })
    .sort(function (a, b) { return a.rank - b.rank; }).slice(0, 4);
  $('#related').innerHTML = related.map(CAD.card).join('');

  /* ---------- Thanh mua dính đáy (mobile) ---------- */
  $('#sb-name').textContent = p.name;
  $('#sb-price').textContent = CAD.formatPrice(CAD.unitPrice(p));
  var sb = $('#sticky-buy');
  if ('IntersectionObserver' in window) {
    new IntersectionObserver(function (en) {
      var show = !en[0].isIntersecting && en[0].boundingClientRect.top < 0;
      sb.classList.toggle('is-visible', show);
      sb.setAttribute('aria-hidden', !show);
      $('#sb-add').tabIndex = show ? 0 : -1;
    }).observe($('#buy-actions'));
  }

  /* ---------- Structured data cho SEO (Next.js sẽ render phía server) ---------- */
  var ld = document.createElement('script');
  ld.type = 'application/ld+json';
  ld.textContent = JSON.stringify({
    '@context': 'https://schema.org', '@type': 'Product', name: p.name, description: p.desc,
    image: imgs.map(function (id) { return CAD.img(id, 1200); }), brand: { '@type': 'Brand', name: 'Cadence' },
    aggregateRating: { '@type': 'AggregateRating', ratingValue: p.rating, reviewCount: p.reviews },
    offers: { '@type': 'Offer', priceCurrency: 'VND', price: CAD.unitPrice(p),
      availability: Object.keys(p.sizes).some(function (s) { return p.sizes[s] > 0; }) ? 'https://schema.org/InStock' : 'https://schema.org/OutOfStock' }
  });
  document.head.appendChild(ld);
})();
