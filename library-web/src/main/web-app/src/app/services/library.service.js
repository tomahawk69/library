(function() {
  'use strict';

  angular
    .module('libraryWebClient')
    .factory('libraryService', ['$http', '$log', libraryService]);

  /** @ngInject */
  function libraryService($http, log) {
    var vm = this;
    vm.data = {};
    var urlBase = 'http://localhost:8080/';
    vm.refreshData = function() {
      log.debug("refreshData");
      $http.get(urlBase + "refreshData").then(function(response) {
        vm.data.message = response.data;
        log.debug(response);
      });
      vm.getDataStatus();
    }

    vm.stopRefreshData = function() {
      log.debug("stopRefreshData");
      $http.get(urlBase + "stopRefreshData").then(function(response) {
        vm.data.message = response.data;
        log.debug(response);
      });
      vm.getDataStatus();
    }

    vm.getDataStatus = function() {
      $http.get(urlBase + "getDataStatus").then(function(response) {
        vm.data.status = response.data;
      }, function() {
        vm.data['status'] = {};
        vm.data.status.status = 'OFFLINE';
      });
    }
    return vm;
  }
})();
