(function() {
  'use strict';

  angular
    .module('libraryWebClient')
    .controller('MainController', MainController);

  /** @ngInject */
  function MainController($timeout, webDevTec, toastr) {
    var vm = this;

    vm.awesomeThings = [];
    vm.classAnimation = '';
    vm.creationDate = 1461413371984;
    vm.showToastr = showToastr;

    activate();
//    $scope.toggleLeft1 = 1;//buildDelayedToggler('left');


    function activate() {
      getWebDevTec();
      $timeout(function() {
        vm.classAnimation = 'rubberBand';
      }, 4000);
    }

    function showToastr() {
      toastr.info('Fork <a href="https://github.com/Swiip/generator-gulp-angular" target="_blank"><b>generator-gulp-angular</b></a>');
      vm.classAnimation = '';
    }

    function getWebDevTec() {
      vm.awesomeThings = webDevTec.getTec();

      angular.forEach(vm.awesomeThings, function(awesomeThing) {
        awesomeThing.rank = Math.random();
      });
    }

//    $scope.closeSideBar = function () {
//          $mdSidenav('left').close()
//            .then(function () {
//              $log.debug("close LEFT is done");
//            });
//        };

//    function debounce(func, wait, context) {
//      var timer;
//      return function debounced() {
//        var context = $scope,
//            args = Array.prototype.slice.call(arguments);
//        $timeout.cancel(timer);
//        timer = $timeout(function() {
//          timer = undefined;
//          func.apply(context, args);
//        }, wait || 10);
//      };
//    }
//
//    function buildDelayedToggler(navID) {
//          return debounce(function() {
//            $mdSidenav(navID)
//              .toggle()
//              .then(function () {
//                $log.debug("toggle " + navID + " is done");
//              });
//          }, 200);
//        }
//
//    function buildToggler(navID) {
//          return function() {
//            $mdSidenav(navID)
//              .toggle()
//              .then(function () {
//                $log.debug("toggle " + navID + " is done");
//              });
//          }
//        }

  }
})();
