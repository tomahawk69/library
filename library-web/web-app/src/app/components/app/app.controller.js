(function() {
  'use strict';

  angular
    .module('libraryWebClient')
    .controller('AppController', ['$scope', '$timeout', '$mdSidenav', AppController]);

  /** @ngInject */
  function AppController($scope, $timeout, $mdSidenav) {
    var vm = this;
    vm.toggleLeft = buildDelayedToggler('left');

    vm.closeSideBar = function () {
          $mdSidenav('left').close()
            .then(function () {
            });
        };

    function debounce(func, wait, context) {
      var timer;
      return function debounced() {
        var context = vm,
            args = Array.prototype.slice.call(arguments);
        $timeout.cancel(timer);
        timer = $timeout(function() {
          timer = undefined;
          func.apply(context, args);
        }, wait || 10);
      };
    }

    function buildDelayedToggler(navID) {
          return debounce(function() {
            $mdSidenav(navID)
              .toggle()
              .then(function () {
              });
          }, 200);
        }

    function buildToggler(navID) {
          return function() {
            $mdSidenav(navID)
              .toggle()
              .then(function () {
              });
          }
        }
  }

})();
