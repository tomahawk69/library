(function() {
  'use strict';

  angular
    .module('libraryWebClient')
    .run(runBlock);

  /** @ngInject */
  function runBlock($log) {

    $log.debug('runBlock end');
  }

})();
