/* SVG icon sprite, chèn đồng bộ ở đầu <body> (chạy được cả khi mở file://) */
(function () {
  var s = '<svg xmlns="http://www.w3.org/2000/svg" style="display:none" aria-hidden="true">' +
    '<symbol id="i-search" viewBox="0 0 24 24"><circle cx="11" cy="11" r="7"/><path d="m20 20-4-4"/></symbol>' +
    '<symbol id="i-user" viewBox="0 0 24 24"><circle cx="12" cy="8" r="3.5"/><path d="M5 20c0-3.6 3.1-5.5 7-5.5s7 1.9 7 5.5"/></symbol>' +
    '<symbol id="i-bag" viewBox="0 0 24 24"><path d="M5 8h14l-1 12H6z"/><path d="M9 8V7a3 3 0 0 1 6 0v1"/></symbol>' +
    '<symbol id="i-heart" viewBox="0 0 24 24"><path d="M12 19.5s-7-4.3-8.7-8.8A4.6 4.6 0 0 1 12 7a4.6 4.6 0 0 1 8.7 3.7C19 15.2 12 19.5 12 19.5z"/></symbol>' +
    '<symbol id="i-menu" viewBox="0 0 24 24"><path d="M4 8h16M4 16h16"/></symbol>' +
    '<symbol id="i-x" viewBox="0 0 24 24"><path d="M18 6 6 18M6 6l12 12"/></symbol>' +
    '<symbol id="i-arrow" viewBox="0 0 24 24"><path d="M5 12h14M13 6l6 6-6 6"/></symbol>' +
    '<symbol id="i-chev-r" viewBox="0 0 24 24"><path d="m9 6 6 6-6 6"/></symbol>' +
    '<symbol id="i-chev-d" viewBox="0 0 24 24"><path d="m6 9 6 6 6-6"/></symbol>' +
    '<symbol id="i-pin" viewBox="0 0 24 24"><path d="M12 21s-6.5-5.6-6.5-11a6.5 6.5 0 0 1 13 0c0 5.4-6.5 11-6.5 11z"/><circle cx="12" cy="10" r="2.3"/></symbol>' +
    '<symbol id="i-box" viewBox="0 0 24 24"><path d="m4 7.5 8-4 8 4v9l-8 4-8-4z"/><path d="m4 7.5 8 4 8-4M12 11.5v9"/></symbol>' +
    '<symbol id="i-help" viewBox="0 0 24 24"><circle cx="12" cy="12" r="8.5"/><path d="M9.6 9.5a2.5 2.5 0 0 1 4.8 1c0 1.7-2.4 2.1-2.4 3.5"/><path d="M12 17h.01"/></symbol>' +
    '<symbol id="i-truck" viewBox="0 0 24 24"><path d="M3 6.5h11v9H3zM14 9.5h4l3 3v3h-7"/><circle cx="7" cy="17.5" r="1.8"/><circle cx="17" cy="17.5" r="1.8"/></symbol>' +
    '<symbol id="i-return" viewBox="0 0 24 24"><path d="M9 7 5 11l4 4"/><path d="M5 11h9a5 5 0 0 1 0 10h-2"/></symbol>' +
    '<symbol id="i-filter" viewBox="0 0 24 24"><path d="M4 7h10M18 7h2M4 17h4M12 17h8"/><circle cx="16" cy="7" r="2"/><circle cx="10" cy="17" r="2"/></symbol>' +
    '<symbol id="i-star" viewBox="0 0 24 24"><path d="m12 3.8 2.5 5.1 5.6.8-4 4 .9 5.6L12 16.6l-5 2.7.9-5.6-4-4 5.6-.8z"/></symbol>' +
  '</svg>';
  document.body.insertAdjacentHTML('afterbegin', s);
  document.documentElement.classList.add('js');
})();
