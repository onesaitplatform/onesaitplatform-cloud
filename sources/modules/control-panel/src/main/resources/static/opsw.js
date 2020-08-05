


const version = "0.0.02";
const cacheName = `op-${version}`;
self.addEventListener('install', e => {
  e.waitUntil(
    caches.open(cacheName).then(cache => {
      return cache.addAll([    	
        `/controlpanel/static/js/pages/dashboardMessageHandler.js`,
        `/controlpanel/static/dashboards/styles/vendor.css`,
        `/controlpanel/static/dashboards/gridster.css`,
        `/controlpanel/static/dashboards/styles/app.css`,
        `/controlpanel/static/dashboards/scripts/vendor.js`,
        `/controlpanel/static/dashboards/gridster.js`,
        `/controlpanel/static/dashboards/resources/lf-ng-md-file-input.min.css`,
        `/controlpanel/static/dashboards/scripts/app.js`,
        `/controlpanel/static/vendor/vs/loader.js`,
        `/controlpanel/static/vendor/vs/editor/editor.main.nls.js`,
        `/controlpanel/static/vendor/vs/editor/editor.main.js`,
        `/controlpanel/static/svg/svgedit-config-iife.js`,
        `/controlpanel/static/vendor/onesait-ds/lib/theme-onesait/index.css`,
        `/controlpanel/static/vendor/onesait-ds/lib/index.js`,
        `/controlpanel/static/vendor/onesait-ds/lib/vue.min.js`,
        `/controlpanel/static/svg/svgedit-custom.css`
      ])
          .then(() => self.skipWaiting());
    })
  );
});

self.addEventListener('activate', event => {
  event.waitUntil(self.clients.claim());
});

self.addEventListener('fetch', event => {
  event.respondWith(
    caches.open(cacheName)
      .then(cache => cache.match(event.request, {ignoreSearch: true}))
      .then(response => {
      return response || fetch(event.request);
    })
  );
});
