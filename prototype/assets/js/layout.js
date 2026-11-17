/* Phần khung dùng chung (utility bar, header, mega menu, footer, drawers, toast).
   Cách dùng trong mỗi trang:
     <script src="assets/js/layout.js" data-part="header"></script>   → ngay sau sprite.js
     <script>CAD.layout('footer')</script>                           → sau </main>
   Chèn đồng bộ tại vị trí thẻ script để trang không bị nháy. */
window.CAD = window.CAD || {};

(function () {
  var P = 'plp.html?';
  /* Menu chính: [link, nhãn, class] */
  var NAV = [
    [P + 'sort=new', 'Mới & nổi bật'], [P + 'g=nam', 'Nam'], [P + 'g=nu', 'Nữ'], [P + 'g=tre-em', 'Trẻ em'],
    [P + 's=chay-bo', 'Chạy bộ'], [P + 's=tap-luyen', 'Tập luyện'], [P + 'sale=1', 'Sale', 'is-sale']
  ];

  /* Mega menu: mỗi mục là các cột [tiêu đề, link tiêu đề, [[nhãn, link]…]]. Link dùng đúng tham số lọc của PLP. */
  function byGender(g) {
    var q = 'g=' + g + '&';
    return [
      ['Nổi bật', P + q + 'sort=new', [['Hàng mới về', P + q + 'sort=new'], ['Bán chạy nhất', P + q.slice(0, -1)], ['Đang giảm giá', P + q + 'sale=1']]],
      ['Giày', P + q + 'type=shoes', [['Tất cả giày', P + q + 'type=shoes'], ['Chạy bộ', P + q + 'cat=giay-chay-bo'], ['Chạy địa hình', P + q + 'cat=giay-trail']].concat(g === 'nu' ? [] : [['Tập luyện', P + q + 'cat=giay-tap']]).concat([['Lifestyle', P + q + 'cat=giay-lifestyle']])],
      ['Quần áo', P + q + 'type=apparel', (g === 'nu'
        ? [['Tất cả quần áo', P + q + 'type=apparel'], ['Áo thun & áo tập', P + q + 'cat=ao-thun'], ['Quần legging', P + q + 'cat=quan-legging'], ['Quần short', P + q + 'cat=quan-short'], ['Hoodie & áo nỉ', P + q + 'cat=hoodie'], ['Áo khoác', P + q + 'cat=ao-khoac']]
        : [['Tất cả quần áo', P + q + 'type=apparel'], ['Áo thun & áo tập', P + q + 'cat=ao-thun'], ['Áo ba lỗ', P + q + 'cat=ao-ba-lo'], ['Quần short', P + q + 'cat=quan-short'], ['Hoodie & áo nỉ', P + q + 'cat=hoodie'], ['Áo khoác', P + q + 'cat=ao-khoac']])],
      ['Phụ kiện', P + 'type=accessories', [['Tất', P + 'cat=tat'], ['Mũ', P + 'cat=mu'], ['Balo & túi', P + 'cat=balo'], ['Bình nước', P + 'cat=binh-nuoc'], ['Găng tay tập', P + 'cat=gang-tay']]],
      ['Theo môn', P + q.slice(0, -1), [['Chạy bộ', P + q + 's=chay-bo'], ['Chạy địa hình', P + q + 's=trail'], ['Tập luyện', P + q + 's=tap-luyen'], ['Mặc hằng ngày', P + q + 's=lifestyle']]]
    ];
  }
  var MEGA = {
    'Mới & nổi bật': [
      ['Hàng mới về', P + 'sort=new', [['Tất cả hàng mới', P + 'sort=new'], ['Giày mới', P + 'type=shoes&sort=new'], ['Quần áo mới', P + 'type=apparel&sort=new'], ['Phụ kiện mới', P + 'type=accessories&sort=new']]],
      ['Bán chạy nhất', P + 'sort=featured', [['Giày chạy bộ', P + 'cat=giay-chay-bo'], ['Giày lifestyle', P + 'cat=giay-lifestyle'], ['Áo thun', P + 'cat=ao-thun'], ['Hoodie & áo nỉ', P + 'cat=hoodie']]],
      ['Gợi ý', P + 'sort=rating', [['Đánh giá cao nhất', P + 'sort=rating'], ['Dưới 1.000.000 ₫', P + 'price=lt1'], ['Đang giảm giá', P + 'sale=1']]]
    ],
    'Nam': byGender('nam'),
    'Nữ': byGender('nu'),
    'Trẻ em': [
      ['Nổi bật', P + 'g=tre-em&sort=new', [['Hàng mới về', P + 'g=tre-em&sort=new'], ['Đang giảm giá', P + 'g=tre-em&sale=1']]],
      ['Quần áo', P + 'g=tre-em&type=apparel', [['Tất cả quần áo trẻ em', P + 'g=tre-em&type=apparel'], ['Áo thun', P + 'g=tre-em&cat=ao-thun'], ['Quần short', P + 'g=tre-em&cat=quan-short'], ['Hoodie & áo nỉ', P + 'g=tre-em&cat=hoodie']]]
    ],
    'Chạy bộ': [
      ['Giày chạy', P + 's=chay-bo&type=shoes', [['Chạy đường nhựa', P + 'cat=giay-chay-bo'], ['Chạy địa hình', P + 'cat=giay-trail'], ['Giày chạy nam', P + 'cat=giay-chay-bo&g=nam'], ['Giày chạy nữ', P + 'cat=giay-chay-bo&g=nu']]],
      ['Quần áo chạy', P + 's=chay-bo&type=apparel', [['Áo chạy bộ', P + 's=chay-bo&cat=ao-thun'], ['Áo ba lỗ', P + 'cat=ao-ba-lo'], ['Quần short chạy', P + 's=chay-bo&cat=quan-short'], ['Quần legging', P + 'cat=quan-legging'], ['Áo khoác gió', P + 's=chay-bo&cat=ao-khoac']]],
      ['Phụ kiện chạy', P + 's=chay-bo&type=accessories', [['Tất chạy bộ', P + 'cat=tat'], ['Mũ & băng đô', P + 'cat=mu'], ['Bình nước', P + 'cat=binh-nuoc']]],
      ['Theo đối tượng', P + 's=chay-bo', [['Nam', P + 's=chay-bo&g=nam'], ['Nữ', P + 's=chay-bo&g=nu']]]
    ],
    'Tập luyện': [
      ['Giày tập', P + 's=tap-luyen&type=shoes', [['Giày tập luyện', P + 'cat=giay-tap']]],
      ['Quần áo tập', P + 's=tap-luyen&type=apparel', [['Áo thun & áo tập', P + 's=tap-luyen&cat=ao-thun'], ['Quần short', P + 's=tap-luyen&cat=quan-short'], ['Quần legging', P + 'cat=quan-legging'], ['Áo khoác', P + 's=tap-luyen&cat=ao-khoac']]],
      ['Phụ kiện tập', P + 's=tap-luyen&type=accessories', [['Bình nước', P + 'cat=binh-nuoc'], ['Găng tay tập', P + 'cat=gang-tay']]],
      ['Theo đối tượng', P + 's=tap-luyen', [['Nam', P + 's=tap-luyen&g=nam'], ['Nữ', P + 's=tap-luyen&g=nu']]]
    ]
  };

  var here = location.pathname.split('/').pop() + location.search;

  function megaPanel(label, cols) {
    return '<div class="mega" id="mega-' + label.replace(/[^a-z]/gi, '') + '"><div class="mega__inner">' +
      cols.map(function (c) {
        return '<div class="mega__col"><a class="mega__head" href="' + c[1] + '">' + c[0] + '</a><ul>' +
          c[2].map(function (l) { return '<li><a href="' + l[1] + '">' + l[0] + '</a></li>'; }).join('') + '</ul></div>';
      }).join('') +
      '</div></div>';
  }
  function navDesktop() {
    return NAV.map(function (n) {
      var current = here === n[0] ? ' aria-current="page"' : '';
      var cols = MEGA[n[1]];
      var link = '<a class="nav__link' + (n[2] ? ' ' + n[2] : '') + '" href="' + n[0] + '"' + current + '>' + n[1] + '</a>';
      return '<div class="nav__item">' + link + (cols ? megaPanel(n[1], cols) : '') + '</div>';
    }).join('');
  }
  /* Menu mobile: mỗi mục có nhóm con xổ ra (details/summary), không cần JS */
  function navMobile() {
    return NAV.map(function (n) {
      var cols = MEGA[n[1]];
      if (!cols) return '<a class="mnav__top' + (n[2] ? ' ' + n[2] : '') + '" href="' + n[0] + '">' + n[1] + '</a>';
      return '<details class="mnav__group"><summary class="mnav__top">' + n[1] + '<svg class="icon" aria-hidden="true"><use href="#i-chev-r"/></svg></summary>' +
        '<div class="mnav__sub"><a class="mnav__all" href="' + n[0] + '">Xem tất cả ' + n[1] + '</a>' +
        cols.map(function (c) {
          return '<p class="mnav__head">' + c[0] + '</p>' + c[2].map(function (l) { return '<a href="' + l[1] + '">' + l[0] + '</a>'; }).join('');
        }).join('') + '</div></details>';
    }).join('');
  }

  var parts = {
    header:
      '<a class="skip-link" href="#main">Bỏ qua tới nội dung chính</a>' +
      '<div class="utility"><div class="container utility__inner">' +
        '<a class="utility__brand" href="index.html">Cadence Club: miễn phí giao hàng cho hội viên</a>' +
        '<nav class="utility__links" aria-label="Hỗ trợ"><a href="#">Tìm cửa hàng</a><span aria-hidden="true">|</span><a href="#">Trợ giúp</a><span aria-hidden="true">|</span><a href="order-lookup.html">Tra cứu đơn hàng</a><span aria-hidden="true">|</span><a href="account.html?tab=register">Tham gia</a><span aria-hidden="true">|</span><a href="account.html">Đăng nhập</a></nav>' +
      '</div></div>' +
      '<header class="header"><div class="container header__inner">' +
        '<div class="header__start">' +
          '<button class="icon-btn menu-toggle" type="button" data-open="menu-drawer" aria-label="Mở menu"><svg class="icon"><use href="#i-menu"/></svg></button>' +
          '<a class="wordmark" href="index.html" aria-label="Cadence, về trang chủ">Cadence</a>' +
        '</div>' +
        '<nav class="nav" aria-label="Danh mục chính">' + navDesktop() + '</nav>' +
        '<div class="header__actions">' +
          '<button class="search-field" type="button" data-search-open aria-haspopup="dialog"><span class="search-field__icon"><svg class="icon"><use href="#i-search"/></svg></span>Tìm kiếm</button>' +
          '<button class="icon-btn search-btn" type="button" data-search-open aria-haspopup="dialog" aria-label="Tìm kiếm"><svg class="icon"><use href="#i-search"/></svg></button>' +
          '<a class="icon-btn" href="account.html" aria-label="Tài khoản"><svg class="icon"><use href="#i-user"/></svg></a>' +
          '<button class="icon-btn" type="button" data-open="cart-drawer" aria-label="Giỏ hàng"><svg class="icon"><use href="#i-bag"/></svg><span class="cart-count" data-cart-count data-count="0">0</span></button>' +
        '</div>' +
      '</div></header>' +
      '<div class="promo" role="note"><div class="container promo__inner">' +
        '<p><strong>Miễn phí giao hàng</strong> cho đơn từ 1.500.000 ₫.<span class="promo__more"> Đổi size miễn phí trong 30 ngày.</span></p>' +
        '<a class="link" href="#">Xem chi tiết</a>' +
      '</div></div>',

    footer:
      '<footer class="footer"><div class="container">' +
        '<div class="footer__top">' +
          '<div class="footer__cols">' +
            '<div class="footer__col"><h2 class="h4">Hỗ trợ</h2><a href="order-lookup.html">Tra cứu đơn hàng</a><a href="#">Giao hàng</a><a href="#">Đổi trả</a><a href="#">Hình thức thanh toán</a><a href="#">Hướng dẫn chọn size</a><a href="#">Liên hệ</a></div>' +
            '<div class="footer__col"><h2 class="h4">Mua sắm</h2><a href="plp.html?g=nam">Nam</a><a href="plp.html?g=nu">Nữ</a><a href="plp.html?g=tre-em">Trẻ em</a><a href="plp.html?sort=new">Hàng mới về</a><a href="plp.html?sale=1">Sale</a></div>' +
            '<div class="footer__col"><h2 class="h4">Cadence</h2><a href="#">Về Cadence</a><a href="account.html?tab=register">Cadence Club</a><a href="index.html#launch">Lịch ra mắt</a><a href="#">Tuyển dụng</a><a href="#">Cửa hàng</a></div>' +
          '</div>' +
          '<div class="footer__news"><h2 class="h4">Nhận tin ra mắt sớm</h2>' +
            '<p class="muted">Lịch ra mắt, câu chuyện từ Run Club và mã giảm 10% cho đơn đầu tiên.</p>' +
            '<form class="newsletter" data-newsletter><label class="sr-only" for="nl-email">Email</label>' +
            '<input id="nl-email" type="email" required placeholder="Email của bạn" autocomplete="email"><button class="btn" type="submit">Đăng ký</button></form></div>' +
        '</div>' +
        '<div class="footer__bottom">' +
          '<div class="footer__legal"><span class="footer__region"><svg class="icon icon--sm" aria-hidden="true"><use href="#i-pin"/></svg>Việt Nam</span><span>© 2026 Cadence. Thương hiệu giả định cho dự án học tập.</span></div>' +
          '<nav class="footer__links" aria-label="Pháp lý"><a href="#">Điều khoản</a><a href="#">Bảo mật</a><a href="#">Cookie</a><a href="admin/index.html">Trang quản trị (prototype)</a></nav>' +
        '</div>' +
        '<p class="footer__credit">Ảnh: Unsplash · Thanh toán: COD, VNPay</p>' +
      '</div></footer>' +

      '<div class="overlay" aria-hidden="true"></div>' +

      '<aside class="drawer drawer--left" id="menu-drawer" aria-label="Menu" inert>' +
        '<div class="drawer__head"><span class="wordmark">Cadence</span><button class="icon-btn" type="button" data-close aria-label="Đóng menu"><svg class="icon"><use href="#i-x"/></svg></button></div>' +
        '<div class="drawer__body"><nav class="mnav" aria-label="Danh mục">' + navMobile() + '</nav>' +
          '<div class="mnav-join"><p>Trở thành hội viên Cadence Club để nhận ưu đãi và hàng ra mắt sớm.</p><div class="mnav-join__btns"><a class="btn" href="account.html?tab=register">Tham gia</a><a class="btn btn--outline" href="account.html">Đăng nhập</a></div></div>' +
          '<div class="mnav-secondary"><a href="order-lookup.html"><svg class="icon" aria-hidden="true"><use href="#i-box"/></svg>Tra cứu đơn hàng</a><a href="#"><svg class="icon" aria-hidden="true"><use href="#i-help"/></svg>Trợ giúp</a><a href="#"><svg class="icon" aria-hidden="true"><use href="#i-pin"/></svg>Tìm cửa hàng</a></div></div>' +
      '</aside>' +
      '<aside class="drawer drawer--right" id="cart-drawer" aria-label="Giỏ hàng" inert>' +
        '<div class="drawer__head"><h2 class="h3">Giỏ hàng</h2><button class="icon-btn" type="button" data-close aria-label="Đóng giỏ hàng"><svg class="icon"><use href="#i-x"/></svg></button></div>' +
        '<div class="drawer__body" id="cart-body"></div>' +
        '<div class="drawer__foot" id="cart-foot" hidden>' +
          '<div class="ship-meter"><span id="ship-text"></span><div class="ship-meter__bar"><span id="ship-bar"></span></div></div>' +
          '<div style="display:flex;justify-content:space-between;font-weight:600"><span>Tạm tính</span><span class="mono" id="cart-subtotal">0 ₫</span></div>' +
          '<a class="btn btn--block" href="checkout.html">Thanh toán</a><a class="btn btn--outline btn--block" href="cart.html">Xem giỏ hàng</a>' +
        '</div>' +
      '</aside>' +

      '<div class="search" id="search" role="dialog" aria-modal="true" aria-label="Tìm kiếm sản phẩm" hidden>' +
        '<div class="search__panel">' +
          '<div class="container search__bar">' +
            '<form class="search__form" id="search-form" role="search" action="plp.html">' +
              '<svg class="icon" aria-hidden="true"><use href="#i-search"/></svg>' +
              '<label class="sr-only" for="search-input">Tìm sản phẩm</label>' +
              '<input class="search__input" id="search-input" name="q" type="search" placeholder="Tìm giày, áo, môn thể thao…" autocomplete="off" spellcheck="false" ' +
                'role="combobox" aria-expanded="false" aria-controls="search-results" aria-autocomplete="list" enterkeyhint="search">' +
            '</form>' +
            '<button class="link" type="button" data-search-close>Đóng</button>' +
          '</div>' +
          '<div class="container search__body" id="search-results" aria-live="polite"></div>' +
        '</div>' +
        '<div class="search__scrim" data-search-close></div>' +
      '</div>' +

      '<div class="toast" id="toast" role="status" aria-live="polite"></div>'
  };

  CAD.layout = function (part) {
    var s = document.currentScript;
    s.insertAdjacentHTML('beforebegin', parts[part]);
  };

  var own = document.currentScript;
  if (own && own.getAttribute('data-part')) own.insertAdjacentHTML('beforebegin', parts[own.getAttribute('data-part')]);

  /* Mega menu: aria-expanded theo trạng thái mở, Esc đóng và trả focus về mục menu */
  document.querySelectorAll('.nav__item').forEach(function (item) {
    var link = item.querySelector('.nav__link'), panel = item.querySelector('.mega');
    if (!panel) return;
    link.setAttribute('aria-expanded', 'false'); link.setAttribute('aria-controls', panel.id);
    function set(open) { link.setAttribute('aria-expanded', open ? 'true' : 'false'); }
    item.addEventListener('mouseenter', function () { item.classList.remove('is-dismissed'); set(true); });
    item.addEventListener('mouseleave', function () { item.classList.remove('is-dismissed'); set(false); });
    item.addEventListener('focusin', function () { set(!item.classList.contains('is-dismissed')); });
    item.addEventListener('focusout', function (e) { if (!item.contains(e.relatedTarget)) { item.classList.remove('is-dismissed'); set(false); } });
    item.addEventListener('keydown', function (e) {
      if (e.key === 'Escape') { item.classList.add('is-dismissed'); set(false); link.focus(); }
    });
  });

})();
