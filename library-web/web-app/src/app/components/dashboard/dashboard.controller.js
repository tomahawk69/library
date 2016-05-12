(function() {
  'use strict';

  angular
    .module('libraryWebClient')
    .controller('DashboardController', ['libraryService', '$rootScope', '$interval', '$timeout', DashboardController]);

  /** @ngInject */
  function DashboardController(libraryService, $rootScope, $interval, $timeout) {
    var vm = this;
    vm.libraryService = libraryService;
    vm.name = "Friend";
    vm.data = libraryService.data;

//    $interval(function() {getDataStatus();}, 5000);

    var getDataStatus = function() {
      libraryService.getDataStatus();
    }

    var getDebugEnabled = function() {
      libraryService.getDebugEnabled();
    }

    vm.setDebugEnabled = function() {
      libraryService.setDebugEnabled();
    }

    vm.refreshData = function() {
      if (vm.isRefreshEnabled())
        libraryService.refreshData();
    }

    vm.stopRefreshData = function() {
      if (vm.isDataProcessing())
        libraryService.stopRefreshData();
    }

    vm.isRefreshEnabled = function() {
      return vm.data.status && vm.data.status.status == "IDLE";
    }

    vm.isDataProcessing = function() {
      return vm.data.status && vm.data.status.status == "REFRESH";
    }

    // "vm.creationDate" is available by directive option "bindToController: true"
    vm.relativeDate = moment(vm.creationDate).fromNow();
  }

})();
