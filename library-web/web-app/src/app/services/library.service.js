(function() {
  'use strict';

  angular
    .module('libraryWebClient')
    .factory('libraryService', ['$http', '$log', '$timeout', '$interval', libraryService]);

  /** @ngInject */
  function libraryService($http, log, $timeout, $interval) {
    var vm = this;
    vm.data = {debugEnabled: false};
    vm.settings = {isOffline : true}
    var urlBase = 'http://localhost:8083/';

    vm.refreshData = function() {
      log.info("refreshData");
      $http.get(urlBase + "refreshData").then(function(response) {
        vm.data.message = response.data;
        log.debug(response);
      });
      vm.getDataStatus();
    }

    vm.stopRefreshData = function() {
      log.info("stopRefreshData");
      $http.get(urlBase + "stopRefreshData").then(function(response) {
        vm.data.message = response.data;
        log.debug(response);
      });
      vm.getDataStatus();
    }

    vm.getDataStatus = function() {
      if (vm.data.debugEnabled) {
        log.debug("getDataStatus");
      }
      $http.get(urlBase + "getDataStatus").then(function(response) {
        vm.data.status = response.data;
        if (vm.settings.isOffline) {
          vm.settings.isOffline = false;
          vm.getDebugEnabled();
        }
      }, function() {
        vm.data['status'] = {};
        vm.settings.isOffline = true;
        vm.data.status.status = 'OFFLINE';
      });
    }

    vm.getDebugEnabled = function() {
      if (vm.data.debugEnabled) {
        log.debug("isDebugEnabled");
      }
      $http.get(urlBase + "debugEnabled").then(function(response) {
        vm.data.debugEnabled = response.data == true;
      });
    }

    vm.setDebugEnabled = function(callback) {
      log.info("setDebugEnabled");
      var config = {headers : {'Content-Type': 'application/x-www-form-urlencoded'}};
      var data = "debugEnabled=" + vm.data.debugEnabled;
      $http.post(urlBase + "debugEnabled", data, config).then(function(response) {
        vm.data.debugEnabled = response.data == true;
      });
    }

    $timeout(function() {vm.getDebugEnabled();}, 100);
    $interval(function() {vm.getDataStatus();}, 5000);

    return vm;
  }
})();
