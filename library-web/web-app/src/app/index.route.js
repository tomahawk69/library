(function() {
  'use strict';

  angular
    .module('libraryWebClient')
    .config(routerConfig)
    .controller('RouterController', RouterController);

  /** @ngInject */
  function routerConfig($componentLoaderProvider) {
    $componentLoaderProvider.setTemplateMapping(function(name) {
      return 'app/components/' + name + '/' + name + '.html';
    });
  }

  /** @ngInject */
  function RouterController($router) {
    $router.config([
      { path: '/', component: 'main' },
      { path: '/dashboard', component: 'dashboard' }
    ]);
  }

})();
