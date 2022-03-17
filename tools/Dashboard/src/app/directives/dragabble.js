(function () {
  'use strict';

  angular.module('dashboardFramework').directive('draggable', function() {
    return function(scope, element) {
      // this gives us the native JS object
      var el = element[0];

      el.draggable = true;

      el.addEventListener(
        'dragstart',
        function(e) {
          e.dataTransfer.effectAllowed = 'move';
          e.dataTransfer.setData('type', this.id);
          this.classList.add('drag');
          if($('synoptic')){
            $('synoptic').css("z-index", "0");
          }
          return false;
        },
        false
      );

      el.addEventListener(
        'dragend',
        function(e) {
          if($('synoptic')){
            $('synoptic').css("z-index", "1");
          }
          this.classList.remove('drag');
          return false;
        },
        false
      );
    }
  });
})();
