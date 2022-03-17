(function () {
  'use strict';

  angular.module('angular-gridster2')
    .service('GridsterDraggable', GridsterDraggable);

  /** @ngInject */
  function GridsterDraggable(GridsterPush, GridsterSwap, GridsterScroll, GridsterUtils) {
    return function (gridsterItem, gridster) {
      var vm = this;
      vm.offsetLeft = 0;
      vm.offsetTop = 0;
      vm.margin = 0;
      vm.diffTop = 0;
      vm.diffLeft = 0;
      vm.top = 0;
      vm.left = 0;
      vm.height = 0;
      vm.width = 0;
      vm.positionX = 0;
      vm.positionY = 0;
      vm.positionXBackup = 0;
      vm.positionYBackup = 0;
      vm.enabled = false;
      vm.dragStartFunction = angular.noop;
      vm.dragFunction = angular.noop;
      vm.dragStopFunction = angular.noop;
      vm.push = undefined;
      vm.swap = undefined;
      vm.gridsterItem = gridsterItem;
      vm.gridster = gridster;
      vm.lastMouse = {
        clientX: 0,
        clientY: 0
      };
      vm.path = [];

      vm.destroy = function () {
        delete vm.gridster.movingItem;
        if (vm.gridster.previewStyle) {
          vm.gridster.previewStyle();
        }
        delete vm.gridster;
        if (vm.mousedown) {
          vm.gridsterItem.nativeEl.removeEventListener('mousedown', vm.dragStartDelay);
          vm.gridsterItem.nativeEl.removeEventListener('touchstart', vm.dragStartDelay);
        }
        delete vm.gridsterItem;
      };

      vm.dragStart = function (e) {
        switch (e.which) {
          case 1:
            // left mouse button
            break;
          case 2:
          case 3:
            // right or middle mouse button
            return;
        }

        if (vm.gridster.options.draggable && vm.gridster.options.draggable.start) {
          vm.gridster.options.draggable.start(vm.gridsterItem.item, vm.gridsterItem, e);
        }

        e.stopPropagation();
        e.preventDefault();
        vm.dragFunction = vm.dragMove.bind(vm);
        vm.dragStopFunction = vm.dragStop.bind(vm);

        document.addEventListener('mousemove', vm.dragFunction);
        document.addEventListener('mouseup', vm.dragStopFunction);
        window.addEventListener('blur', vm.dragStopFunction);
        vm.gridster.el.addEventListener('touchmove', vm.dragFunction);
        document.addEventListener('touchend', vm.dragStopFunction);
        document.addEventListener('touchcancel', vm.dragStopFunction);
        vm.gridsterItem.el.addClass('gridster-item-moving');
        vm.margin = vm.gridster.$options.margin;
        vm.offsetLeft = vm.gridster.el.scrollLeft - vm.gridster.el.offsetLeft;
        vm.offsetTop = vm.gridster.el.scrollTop - vm.gridster.el.offsetTop;
        vm.left = vm.gridsterItem.left;
        vm.top = vm.gridsterItem.top;
        vm.width = vm.gridsterItem.width;
        vm.height = vm.gridsterItem.height;
        vm.diffLeft = e.clientX + vm.offsetLeft - vm.margin - vm.left;
        vm.diffTop = e.clientY + vm.offsetTop - vm.margin - vm.top;
        vm.gridster.movingItem = vm.gridsterItem.$item;
        vm.gridster.previewStyle();
        vm.push = new GridsterPush(vm.gridsterItem);
        vm.swap = new GridsterSwap(vm.gridsterItem);
        vm.gridster.dragInProgress = true;
        vm.gridster.gridLines.updateGrid();
        vm.path.push({x: vm.gridsterItem.item.x, y: vm.gridsterItem.item.y});
        document.getElementsByTagName("gridster-preview")[0].style.cursor = "grabbing";
      };

      vm.dragMove = function (e) {
        e.stopPropagation();
        e.preventDefault();
        GridsterUtils.checkTouchEvent(e);
        vm.offsetLeft = vm.gridster.el.scrollLeft - vm.gridster.el.offsetLeft;
        vm.offsetTop = vm.gridster.el.scrollTop - vm.gridster.el.offsetTop;
        GridsterScroll(vm.gridsterItem, e, vm.lastMouse, vm.calculateItemPositionFromMousePosition.bind(vm));

        vm.calculateItemPositionFromMousePosition(e);

        vm.lastMouse.clientX = e.clientX;
        vm.lastMouse.clientY = e.clientY;
        vm.gridster.gridLines.updateGrid();
      };

      vm.calculateItemPositionFromMousePosition = function (e) {
        vm.left = e.clientX + vm.offsetLeft - vm.margin - vm.diffLeft;
        vm.top = e.clientY + vm.offsetTop - vm.margin - vm.diffTop;
        vm.calculateItemPosition();
      };

      vm.dragStop = function (e) {
        e.stopPropagation();
        e.preventDefault();
        GridsterScroll.cancelScroll();
        document.removeEventListener('mousemove', vm.dragFunction);
        document.removeEventListener('mouseup', vm.dragStopFunction);
        window.removeEventListener('blur', vm.dragStopFunction);
        vm.gridster.el.removeEventListener('touchmove', vm.dragFunction);
        document.removeEventListener('touchend', vm.dragStopFunction);
        document.removeEventListener('touchcancel', vm.dragStopFunction);
        vm.gridsterItem.el.removeClass('gridster-item-moving');
        vm.gridster.dragInProgress = false;
        vm.gridster.gridLines.updateGrid();
        vm.gridster.gridLines.updateGrid(false);
        vm.path = [];
        if (vm.gridster.options.draggable && vm.gridster.options.draggable.stop) {
          var promise = vm.gridster.options.draggable.stop(vm.gridsterItem.item, vm.gridsterItem, e);
          if (promise && promise.then) {
            promise.then(vm.makeDrag.bind(vm), vm.cancelDrag.bind(vm));
          } else {
            vm.makeDrag();
          }
        } else {
          vm.makeDrag();
        }
        setTimeout(function () {
          if (vm.gridster) {
            vm.gridster.movingItem = null;
            vm.gridster.previewStyle();
          }
        });
      };

      vm.cancelDrag = function () {
        vm.gridsterItem.$item.x = vm.gridsterItem.item.x;
        vm.gridsterItem.$item.y = vm.gridsterItem.item.y;
        vm.gridsterItem.setSize(true);
        vm.push.restoreItems();
        vm.swap.restoreSwapItem();
        vm.push.destroy();
        delete vm.push;
        vm.swap.destroy();
        delete vm.swap;
      };

      vm.makeDrag = function () {
        vm.gridsterItem.setSize(true);
        vm.gridsterItem.checkItemChanges(vm.gridsterItem.$item, vm.gridsterItem.item);
        vm.push.setPushedItems();
        vm.swap.setSwapItem();
        vm.push.destroy();
        delete vm.push;
        vm.swap.destroy();
        delete vm.swap;
      };

      vm.calculateItemPosition = function () {
        vm.positionX = vm.gridster.pixelsToPositionX(vm.left, Math.round);
        vm.positionY = vm.gridster.pixelsToPositionY(vm.top, Math.round);
        vm.positionXBackup = vm.gridsterItem.$item.x;
        vm.positionYBackup = vm.gridsterItem.$item.y;
        vm.gridsterItem.$item.x = vm.positionX;
        if (vm.gridster.checkGridCollision(vm.gridsterItem.$item)) {
          vm.gridsterItem.$item.x = vm.positionXBackup;
        } else {
          if(!vm.gridster.options.disableLiveMove){
            vm.gridsterItem.el.css('left', vm.left + 'px');
          }
        }
        vm.gridsterItem.$item.y = vm.positionY;
        if (vm.gridster.checkGridCollision(vm.gridsterItem.$item)) {
          vm.gridsterItem.$item.y = vm.positionYBackup;
        } else {
          if(!vm.gridster.options.disableLiveMove){
            vm.gridsterItem.el.css('top', vm.top + 'px');
          }
        }

        if (vm.positionXBackup !== vm.gridsterItem.$item.x || vm.positionYBackup !== vm.gridsterItem.$item.y) {
          var lastPosition = vm.path[vm.path.length - 1];
          var direction;
          if (lastPosition.x < vm.gridsterItem.$item.x) {
            direction = vm.push.fromWest;
          } else if (lastPosition.x > vm.gridsterItem.$item.x) {
            direction = vm.push.fromEast;
          } else if (lastPosition.y < vm.gridsterItem.$item.y) {
            direction = vm.push.fromNorth;
          } else if (lastPosition.y > vm.gridsterItem.$item.y) {
            direction = vm.push.fromSouth;
          }
          vm.push.pushItems(direction, vm.gridster.$options.disablePushOnDrag);
          vm.swap.swapItems();
          if (vm.gridster.checkCollision(vm.gridsterItem.$item)) {
            var xback = vm.gridsterItem.$item.x;    
            vm.gridsterItem.$item.x = vm.positionXBackup;  
            if(vm.gridster.checkCollision(vm.gridsterItem.$item)){
              vm.gridsterItem.$item.x = xback;
              vm.gridsterItem.$item.y = vm.positionYBackup;
              if(vm.gridster.checkCollision(vm.gridsterItem.$item)){
                vm.gridsterItem.$item.x = vm.positionXBackup;
              }
              else{
                vm.path.push({x: vm.gridsterItem.$item.x, y: vm.gridsterItem.$item.y});
                vm.gridster.previewStyle();
              }
            }
            else{
              vm.path.push({x: vm.gridsterItem.$item.x, y: vm.gridsterItem.$item.y});
              vm.gridster.previewStyle();
            }

          } else {
            vm.path.push({x: vm.gridsterItem.$item.x, y: vm.gridsterItem.$item.y});
            vm.gridster.previewStyle();
          }

              
          
          
          
          vm.push.checkPushBack();
        }
      };

      vm.toggle = function () {
        var enableDrag = vm.gridsterItem.canBeDragged();
        if (!vm.enabled && enableDrag) {
          vm.enabled = !vm.enabled;
          vm.gridsterItem.nativeEl.addEventListener('mousedown', vm.dragStartDelay);
          vm.gridsterItem.nativeEl.addEventListener('touchstart', vm.dragStartDelay);
        } else if (vm.enabled && !enableDrag) {
          vm.enabled = !vm.enabled;
          vm.gridsterItem.nativeEl.removeEventListener('mousedown', vm.dragStartDelay);
          vm.gridsterItem.nativeEl.removeEventListener('touchstart', vm.dragStartDelay);
        }
      };

      vm.dragStartDelay = function (e) {
        GridsterUtils.checkTouchEvent(e);
        if (e.target.classList.contains('gridster-item-resizable-handler')) {
          return;
        }
        if (GridsterUtils.checkContentClassForEvent(vm.gridster, e)) {
          return;
        }
        if (!vm.gridster.$options.draggable.delayStart) {
          vm.dragStart(e);
          return;
        }
        var timeout = setTimeout(function () {
          vm.dragStart(e);
          cancelDrag();
        }, vm.gridster.$options.draggable.delayStart);
        document.addEventListener('mouseup', cancelDrag);
        window.addEventListener('blur', cancelDrag);
        document.addEventListener('touchmove', cancelMove);
        document.addEventListener('touchend', cancelDrag);
        document.addEventListener('touchcancel', cancelDrag);

        function cancelMove(eventMove) {
          GridsterUtils.checkTouchEvent(eventMove);
          if (Math.abs(eventMove.clientX - e.clientX) > 9 || Math.abs(eventMove.clientY - e.clientY) > 9) {
            cancelDrag();
          }
        }

        function cancelDrag() {
          clearTimeout(timeout);
          document.removeEventListener('mouseup', cancelDrag);
          window.removeEventListener('blur', cancelDrag);
          document.removeEventListener('touchmove', cancelMove);
          document.removeEventListener('touchend', cancelDrag);
          document.removeEventListener('touchcancel', cancelDrag);
        }
      }
    };
  }
})();
