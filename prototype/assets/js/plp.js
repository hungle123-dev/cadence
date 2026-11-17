/* PLP: lọc nhiều lựa chọn, đếm facet, sắp xếp, "Xem thêm", đồng bộ URL (deep link) */
(function () {
  var $ = function (s, r) { return (r || document).querySelector(s); };
  var $$ = function (s, r) { return Array.prototype.slice.call((r || document).querySelectorAll(s)); };
  var PAGE = 12;
  var L = CAD.labels;
  var mqDesktop = window.matchMedia('(min-width: 1024px)');

  var PRICE = [
    { id: 'lt1', name: 'Dưới 1.000.000 ₫', test: function (v) { return v < 1e6; } },
    { id: '1-2', name: '1.000.000 – 2.000.000 ₫', test: function (v) { return v >= 1e6 && v < 2e6; } },
    { id: '2-3', name: '2.000.000 – 3.000.000 ₫', test: function (v) { return v >= 2e6 && v < 3e6; } },
    { id: 'gt3', name: 'Trên 3.000.000 ₫', test: function (v) { return v >= 3e6; } }
  ];
  var SIZE_ORDER = ['XS', 'S', 'M', 'L', 'XL', '4', '5', '35', '36', '37', '38', '39', '40', '41', '42', '43', '44', 'OS'];

  /* ---------- State từ URL ---------- */
  var q = new URLSearchParams(location.search);
  var list = function (k) { return q.get(k) ? q.get(k).split(',') : []; };
  var state = {
    cat: list('cat'), type: list('type'), gender: list('g'), sport: list('s'), size: list('size'),
    color: list('color'), price: list('price'), sale: q.get('sale') === '1'
  };
  var query = (q.get('q') || '').trim();
  var searchHits = query ? CAD.search(query).map(function (p) { return p.slug; }) : null;
  var sort = q.get('sort') || (query ? 'relevance' : 'featured');
  if (query) {
    $('#sort').insertAdjacentHTML('afterbegin', '<option value="relevance">Phù hợp nhất</option>');
  }
  var shown = PAGE;
  $('#sort').value = sort;

  /* ---------- Điều kiện lọc ---------- */
  var tests = {
    cat: function (p, v) { return v.indexOf(p.cat) > -1; },
    type: function (p, v) { return v.indexOf(p.type) > -1; },
    gender: function (p, v) { return v.indexOf(p.gender) > -1 || (p.gender === 'unisex' && (v.indexOf('nam') > -1 || v.indexOf('nu') > -1)); },
    sport: function (p, v) { return v.indexOf(p.sport) > -1; },
    size: function (p, v) { return v.some(function (s) { return p.sizes[s] > 0; }); },  /* chỉ tính size CÒN HÀNG */
    color: function (p, v) { return p.colors.some(function (c) { return v.indexOf(c.family) > -1; }); },
    price: function (p, v) { return PRICE.some(function (b) { return v.indexOf(b.id) > -1 && b.test(CAD.unitPrice(p)); }); }
  };
  function matches(p, skip) {
    if (searchHits && searchHits.indexOf(p.slug) === -1) return false;
    for (var k in tests) {
      if (k !== skip && state[k].length && !tests[k](p, state[k])) return false;
    }
    if (skip !== 'sale' && state.sale && !p.salePrice) return false;
    return true;
  }
  function countWith(key, value) {
    return CAD.products.filter(function (p) {
      if (!matches(p, key)) return false;
      if (key === 'sale') return !!p.salePrice;
      return tests[key](p, [value]);
    }).length;
  }

  /* ---------- Render bộ lọc ---------- */
  function checkOpt(key, value, label, extra) {
    var n = countWith(key, value), on = state[key].indexOf(value) > -1;
    /* Ẩn lựa chọn không còn sản phẩm nào (trừ khi đang chọn), như các trang thương hiệu lớn */
    if (n === 0 && !on) return '';
    return '<label class="opt' + (n === 0 && !on ? ' is-empty' : '') + '">' +
      '<input type="checkbox" data-key="' + key + '" value="' + value + '"' + (on ? ' checked' : '') + (n === 0 && !on ? ' disabled' : '') + '>' +
      '<span class="box" aria-hidden="true"></span>' + (extra || '') + '<span>' + label + '</span><span class="n">' + n + '</span></label>';
  }
  function group(id, title, body, selCount) {
    return '<details class="fgroup" open><summary>' + title +
      (selCount ? '<span class="sel">' + selCount + ' đã chọn</span>' : '') + '</summary><div class="fgroup__body">' + body + '</div></details>';
  }
  function renderFilters() {
    var sizes = {};
    CAD.products.forEach(function (p) { Object.keys(p.sizes).forEach(function (s) { sizes[s] = 1; }); });
    var sizeKeys = SIZE_ORDER.filter(function (s) { return sizes[s] && ['OS', '4', '5'].indexOf(s) === -1; });
    var colorFams = Object.keys(CAD.colorFamilies);

    var cats = Object.keys(L.category).filter(function (c) { return state.cat.indexOf(c) > -1 || countWith('cat', c) > 0; });
    var html =
      group('cat', 'Danh mục', cats.map(function (c) { return checkOpt('cat', c, L.category[c]); }).join(''), state.cat.length) +
      group('type', 'Loại sản phẩm', Object.keys(L.type).map(function (t) { return checkOpt('type', t, L.type[t]); }).join(''), state.type.length) +
      group('gender', 'Giới tính', ['nam', 'nu', 'tre-em'].map(function (g) { return checkOpt('gender', g, L.gender[g]); }).join(''), state.gender.length) +
      group('size', 'Size còn hàng', '<div class="size-grid">' + sizeKeys.map(function (s) {
        var n = countWith('size', s), on = state.size.indexOf(s) > -1;
        return '<button type="button" class="size-btn" data-size="' + s + '" aria-pressed="' + on + '"' + (n === 0 && !on ? ' disabled' : '') +
          ' aria-label="Size ' + s + ', ' + n + ' sản phẩm">' + s + '</button>';
      }).join('') + '</div>', state.size.length) +
      group('color', 'Màu', colorFams.map(function (c) {
        return checkOpt('color', c, CAD.colorFamilies[c].name, '<span class="dot" style="background:' + CAD.colorFamilies[c].hex + '"></span>');
      }).join(''), state.color.length) +
      group('price', 'Khoảng giá', PRICE.map(function (b) { return checkOpt('price', b.id, b.name); }).join(''), state.price.length) +
      group('sport', 'Môn', Object.keys(L.sport).filter(function (sp) { return CAD.products.some(function (p) { return p.sport === sp; }); }).map(function (s) { return checkOpt('sport', s, L.sport[s]); }).join(''), state.sport.length) +
      group('sale', 'Ưu đãi',
        '<label class="opt"><input type="checkbox" data-key="sale" value="1"' + (state.sale ? ' checked' : '') + '><span class="box" aria-hidden="true"></span><span>Đang giảm giá</span><span class="n">' + countWith('sale', '1') + '</span></label>', state.sale ? 1 : 0);

    /* Giữ trạng thái đóng/mở của từng nhóm khi render lại */
    var openState = $$('#filters-body details').map(function (d) { return d.open; });
    $('#filters-body').innerHTML = html;
    if (openState.length) $$('#filters-body details').forEach(function (d, i) { d.open = openState[i]; });
  }

  /* ---------- Chip bộ lọc đang áp dụng ---------- */
  function chipLabel(key, v) {
    if (key === 'cat') return L.category[v];
    if (key === 'type') return L.type[v];
    if (key === 'gender') return L.gender[v];
    if (key === 'sport') return L.sport[v];
    if (key === 'size') return 'Size ' + v;
    if (key === 'color') return CAD.colorFamilies[v].name;
    if (key === 'price') return PRICE.filter(function (b) { return b.id === v; })[0].name;
  }
  function activeCount() {
    return ['cat', 'type', 'gender', 'sport', 'size', 'color', 'price'].reduce(function (s, k) { return s + state[k].length; }, 0) + (state.sale ? 1 : 0);
  }
  function renderChips() {
    var html = query ? '<button class="chip" type="button" data-remove-q aria-label="Bỏ từ khoá ' + query.replace(/"/g, '&quot;') + '">“' + query.replace(/</g, '&lt;') + '”<svg class="icon"><use href="#i-x"/></svg></button>' : '';
    ['cat', 'type', 'gender', 'sport', 'size', 'color', 'price'].forEach(function (k) {
      state[k].forEach(function (v) {
        html += '<button class="chip" type="button" data-remove="' + k + '" data-value="' + v + '" aria-label="Bỏ lọc ' + chipLabel(k, v) + '">' + chipLabel(k, v) + '<svg class="icon"><use href="#i-x"/></svg></button>';
      });
    });
    if (state.sale) html += '<button class="chip" type="button" data-remove="sale" data-value="1">Đang giảm giá<svg class="icon"><use href="#i-x"/></svg></button>';
    if (activeCount()) html += '<button class="chip chip--clear" type="button" data-clear>Xoá bộ lọc</button>';
    $('#chips').innerHTML = html;
    var n = activeCount();
    $('#filter-n').hidden = n === 0;
    $('#filter-n').textContent = n;
  }

  /* ---------- Kết quả ---------- */
  var sorters = {
    relevance: function (a, b) { return searchHits.indexOf(a.slug) - searchHits.indexOf(b.slug); },
    featured: function (a, b) { return a.rank - b.rank; },
    new: function (a, b) { return (b.flag === 'Mới') - (a.flag === 'Mới') || a.rank - b.rank; },
    'price-asc': function (a, b) { return CAD.unitPrice(a) - CAD.unitPrice(b); },
    'price-desc': function (a, b) { return CAD.unitPrice(b) - CAD.unitPrice(a); },
    rating: function (a, b) { return b.rating - a.rating; }
  };
  function renderResults() {
    var res = CAD.products.filter(function (p) { return matches(p); }).sort(sorters[sort]);
    $('#plp-count').textContent = '(' + res.length + ')';
    $('#apply-btn').textContent = res.length ? 'Xem ' + res.length + ' kết quả' : 'Không có kết quả';
    $('#results').innerHTML = res.slice(0, shown).map(CAD.card).join('');
    $('#empty').hidden = res.length > 0;
    var lm = $('#load-more');
    lm.hidden = res.length <= PAGE;
    var vis = Math.min(shown, res.length);
    $('#load-more-text').textContent = 'Đang xem ' + vis + ' / ' + res.length + ' sản phẩm';
    $('#load-more-bar').style.width = (vis / Math.max(1, res.length) * 100) + '%';
    $('#load-more-btn').hidden = vis >= res.length;
  }

  /* ---------- Tiêu đề theo bộ lọc ---------- */
  function renderTitle() {
    var parts = [];
    if (state.sale) parts.push('Sale');
    if (state.cat.length === 1) parts.push(L.category[state.cat[0]]);
    else if (state.sport.length === 1) parts.push(L.sport[state.sport[0]]);
    else if (state.type.length === 1) parts.push(L.type[state.type[0]]);
    if (state.gender.length === 1) parts.push(L.gender[state.gender[0]]);
    var t = query ? 'Kết quả cho “' + query + '”' + (parts.length ? ' · ' + parts.join(' · ') : '')
                  : parts.length ? parts.join(' · ') : 'Tất cả sản phẩm';
    $('#plp-title').textContent = t;
    $('#crumb-current').textContent = t;
    document.title = t + ' | Cadence';
  }

  /* ---------- URL ---------- */
  function syncUrl() {
    var p = new URLSearchParams();
    if (query) p.set('q', query);
    if (state.cat.length) p.set('cat', state.cat.join(','));
    if (state.type.length) p.set('type', state.type.join(','));
    if (state.gender.length) p.set('g', state.gender.join(','));
    if (state.sport.length) p.set('s', state.sport.join(','));
    if (state.size.length) p.set('size', state.size.join(','));
    if (state.color.length) p.set('color', state.color.join(','));
    if (state.price.length) p.set('price', state.price.join(','));
    if (state.sale) p.set('sale', '1');
    if (sort !== 'featured' && sort !== 'relevance') p.set('sort', sort);
    var qs = p.toString();
    history.replaceState(null, '', location.pathname + (qs ? '?' + qs : ''));
  }

  function update(resetPaging) {
    if (resetPaging) shown = PAGE;
    renderFilters(); renderChips(); renderResults(); renderTitle(); syncUrl();
  }
  function toggle(key, value) {
    if (key === 'sale') { state.sale = !state.sale; return; }
    var i = state[key].indexOf(value);
    if (i > -1) state[key].splice(i, 1); else state[key].push(value);
  }
  function clearAll() {
    ['cat', 'type', 'gender', 'sport', 'size', 'color', 'price'].forEach(function (k) { state[k] = []; });
    state.sale = false;
  }

  /* ---------- Sự kiện ---------- */
  $('#filters-body').addEventListener('change', function (e) {
    var t = e.target;
    if (t.matches('input[data-key]')) { toggle(t.getAttribute('data-key'), t.value); update(true); }
  });
  $('#filters-body').addEventListener('click', function (e) {
    var b = e.target.closest('.size-btn');
    if (b && !b.disabled) { toggle('size', b.getAttribute('data-size')); update(true); var nb = $('[data-size="' + b.getAttribute('data-size') + '"]'); if (nb) nb.focus(); }
  });
  document.addEventListener('click', function (e) {
    if (e.target.closest('[data-remove-q]')) {
      query = ''; searchHits = null;
      if (sort === 'relevance') { sort = 'featured'; $('#sort').value = 'featured'; }
      var rel = $('#sort option[value="relevance"]'); if (rel) rel.remove();
      update(true); return;
    }
    var r = e.target.closest('[data-remove]');
    if (r) { toggle(r.getAttribute('data-remove'), r.getAttribute('data-value')); update(true); return; }
    if (e.target.closest('[data-clear]')) { clearAll(); update(true); }
  });
  $('#sort').addEventListener('change', function (e) { sort = e.target.value; update(true); });
  $('#load-more-btn').addEventListener('click', function () {
    var first = shown;
    shown += PAGE; renderResults();
    var next = $$('#results .card')[first];
    if (next) { var a = $('a', next); if (a) a.focus({ preventScroll: true }); }
  });

  /* Nút bộ lọc: desktop = ẩn/hiện cột lọc; mobile = mở bảng lọc toàn màn hình */
  var filters = $('#filters'), plp = $('#plp'), toggleBtn = $('#filter-toggle');
  function syncToggleLabel() {
    if (mqDesktop.matches) {
      var hidden = plp.classList.contains('is-collapsed');
      $('#filter-toggle-label').textContent = hidden ? 'Hiện bộ lọc' : 'Ẩn bộ lọc';
      toggleBtn.setAttribute('aria-expanded', !hidden);
    } else {
      $('#filter-toggle-label').textContent = 'Bộ lọc';
      toggleBtn.setAttribute('aria-expanded', filters.classList.contains('is-open'));
    }
  }
  toggleBtn.addEventListener('click', function () {
    if (mqDesktop.matches) plp.classList.toggle('is-collapsed');
    else { filters.classList.add('is-open'); document.body.style.overflow = 'hidden'; var c = $('[data-filters-close]', filters); if (c) c.focus(); }
    syncToggleLabel();
  });
  $$('[data-filters-close]').forEach(function (b) {
    b.addEventListener('click', function () { filters.classList.remove('is-open'); document.body.style.overflow = ''; syncToggleLabel(); toggleBtn.focus(); });
  });
  mqDesktop.addEventListener('change', function () { filters.classList.remove('is-open'); document.body.style.overflow = ''; syncToggleLabel(); });
  syncToggleLabel();

  update(false);
})();
