(function () {
  'use strict';

  angular.module('angular-gridster2')
    .service('GridsterEmptyCell', GridsterEmptyCell);

  /** @ngInject */
  function GridsterEmptyCell(GridsterUtils) {
    return function (gridster) {
      var vm = this;

      vm.destroy = function () {
        delete vm.initialItem;
        delete gridster.movingItem;
        if (gridster.previewStyle) {
          gridster.previewStyle();
        }
      };

      vm.updateOptions = function () {
        if (gridster.$options.enableEmptyCellClick && !vm.emptyCellClick && gridster.options.emptyCellClickCallback) {
          vm.emptyCellClick = true;
          gridster.el.addEventListener('click', vm.emptyCellClickCb);
          gridster.el.addEventListener('touchend', vm.emptyCellClickCb);
        } else if (!gridster.$options.enableEmptyCellClick && vm.emptyCellClick) {
          vm.emptyCellClick = false;
          gridster.el.removeEventListener('click', vm.emptyCellClickCb);
          gridster.el.removeEventListener('touchend', vm.emptyCellClickCb);
        }
        if (gridster.$options.enableEmptyCellContextMenu && !vm.emptyCellContextMenu &&
          gridster.options.emptyCellContextMenuCallback) {
          vm.emptyCellContextMenu = true;
          gridster.el.addEventListener('contextmenu', vm.emptyCellContextMenuCb);
        } else if (!gridster.$options.enableEmptyCellContextMenu && vm.emptyCellContextMenu) {
          vm.emptyCellContextMenu = false;
          gridster.el.removeEventListener('contextmenu', vm.emptyCellContextMenuCb);
        }
        if (gridster.$options.enableEmptyCellDrop && !vm.emptyCellDrop && gridster.options.emptyCellDropCallback) {
          vm.emptyCellDrop = true;
          gridster.el.addEventListener('drop', vm.emptyCellDragDrop);
          gridster.el.addEventListener('dragover', vm.emptyCellDragOver);
          document.addEventListener('dragleave', vm.emptyCellDragOver);
        } else if (!gridster.$options.enableEmptyCellDrop && vm.emptyCellDrop) {
          vm.emptyCellDrop = false;
          gridster.el.removeEventListener('drop', vm.emptyCellDragDrop);
          gridster.el.removeEventListener('dragover', vm.emptyCellDragOver);
        }
        if (gridster.$options.enableEmptyCellDrag && !vm.emptyCellDrag && gridster.options.emptyCellDragCallback) {
          vm.emptyCellDrag = true;
          gridster.el.addEventListener('mousedown', vm.emptyCellMouseDown);
          gridster.el.addEventListener('touchstart', vm.emptyCellMouseDown);
        } else if (!gridster.$options.enableEmptyCellDrag && vm.emptyCellDrag) {
          vm.emptyCellDrag = false;
          gridster.el.removeEventListener('mousedown', vm.emptyCellMouseDown);
          gridster.el.removeEventListener('touchstart', vm.emptyCellMouseDown);
        }
      };
      vm.emptyCellClickCb = function (e) {
        if (gridster.movingItem || GridsterUtils.checkContentClassForEmptyCellClickEvent(gridster, e)) {
          return;
        }
        var item = vm.getValidItemFromEvent(e);
        if (!item) {
          return;
        }
        gridster.options.emptyCellClickCallback(e, item);
      };

      vm.emptyCellContextMenuCb = function (e) {
        if (gridster.movingItem || GridsterUtils.checkContentClassForEmptyCellClickEvent(gridster, e)) {
          return;
        }
        e.preventDefault();
        e.stopPropagation();
        var item = vm.getValidItemFromEvent(e);
        if (!item) {
          return;
        }
        gridster.options.emptyCellContextMenuCallback(e, item);
      };

      vm.emptyCellDragDrop = function (e) {
        var item = vm.getValidItemFromEvent(e);
        if (!item) {
          gridster.movingItem = null;
          gridster.previewStyle();
          return;
        }
        gridster.options.emptyCellDropCallback(e, gridster.movingItem);
        gridster.movingItem = null;
        gridster.previewStyle();
      };

      vm.emptyCellDragOver = function (e) {
        e.preventDefault();
        e.stopPropagation();
        var item = gridster.$options.enableEmptyCellAlign?vm.getValidAlignItemFromEvent(e):vm.getValidItemFromEvent(e);
        if (item) {
          e.dataTransfer.dropEffect = 'move';
          gridster.movingItem = item;
        } else {
          //e.dataTransfer.dropEffect = 'none';
          gridster.movingItem = null;
        }
        gridster.previewStyle();
      };

      vm.emptyCellMouseDown = function (e) {
        if (GridsterUtils.checkContentClassForEmptyCellClickEvent(gridster, e)) {
          return;
        }
        e.preventDefault();
        e.stopPropagation();
        var item = vm.getValidItemFromEvent(e);
        if (!item) {
          return;
        }
        vm.initialItem = item;
        gridster.movingItem = item;
        gridster.previewStyle();
        window.addEventListener('mousemove', vm.emptyCellMouseMove);
        window.addEventListener('touchmove', vm.emptyCellMouseMove);
        window.addEventListener('mouseup', vm.emptyCellMouseUp);
        window.addEventListener('touchend', vm.emptyCellMouseUp);
      };

      vm.emptyCellMouseMove = function (e) {
        e.preventDefault();
        e.stopPropagation();
        var item = vm.getValidItemFromEvent(e, vm.initialItem);
        if (!item) {
          return;
        }

        gridster.movingItem = item;
        gridster.previewStyle();
      };

      vm.emptyCellMouseUp = function (e) {
        window.removeEventListener('mousemove', vm.emptyCellMouseMove);
        window.removeEventListener('touchmove', vm.emptyCellMouseMove);
        window.removeEventListener('mouseup', vm.emptyCellMouseUp);
        window.removeEventListener('touchend', vm.emptyCellMouseUp);
        var item = vm.getValidItemFromEvent(e, vm.initialItem);
        if (item) {
          gridster.movingItem = item;
        }
        gridster.options.emptyCellDragCallback(e, gridster.movingItem);
        setTimeout(function () {
          if (gridster) {
            gridster.movingItem = null;
            gridster.previewStyle();
          }
        });
      };

      vm.getValidItemFromEvent = function (e, oldItem) {
        e.preventDefault();
        e.stopPropagation();
        GridsterUtils.checkTouchEvent(e);
        var rect = gridster.el.getBoundingClientRect();
        var x = e.clientX + gridster.el.scrollLeft - rect.left;
        var y = e.clientY + gridster.el.scrollTop - rect.top;
        var item = {
          x: gridster.pixelsToPositionX(x, Math.floor),
          y: gridster.pixelsToPositionY(y, Math.floor),
          cols: gridster.$options.defaultItemCols,
          rows: gridster.$options.defaultItemRows
        };
        if (oldItem) {
          item.cols = Math.min(Math.abs(oldItem.x - item.x) + 1, gridster.$options.emptyCellDragMaxCols);
          item.rows = Math.min(Math.abs(oldItem.y - item.y) + 1, gridster.$options.emptyCellDragMaxRows);
          if (oldItem.x < item.x) {
            item.x = oldItem.x;
          } else if (oldItem.x - item.x > gridster.$options.emptyCellDragMaxCols - 1) {
            item.x = gridster.movingItem.x;
          }
          if (oldItem.y < item.y) {
            item.y = oldItem.y;
          } else if (oldItem.y - item.y > gridster.$options.emptyCellDragMaxRows - 1) {
            item.y = gridster.movingItem.y;
          }
        }
        if (gridster.checkCollision(item)) {
          return;
        }
        return item;
      };


      vm.getValidAlignItemFromEvent = function (e, oldItem) {
        var itemOri = vm.getValidItemFromEvent(e, oldItem);
        if (!itemOri) {
          return;
        }
        else{
          var item = angular.copy(itemOri);
          var iter = gridster.$options.emptyCellAlignIter;
          while(item){
            //increment one border 
            var itemCopy = angular.copy(item);
            itemCopy.x--;
            itemCopy.y--;
            itemCopy.cols+=2;
            itemCopy.rows+=2;
            if(gridster.checkCollision(itemCopy)){
              //East
              var itemCopy2 = angular.copy(item);
              itemCopy2.cols++;
              while(iter && !gridster.checkCollision(itemCopy2)){
                itemCopy2.cols++;
                iter--;
              }
              itemCopy2.cols--;
              //West
              itemCopy2.x--;
              itemCopy2.cols++;
              while(iter && !gridster.checkCollision(itemCopy2)){
                itemCopy2.x--;
                itemCopy2.cols++;
                iter--;
              }
              itemCopy2.x++;
              itemCopy2.cols--;
              //North
              itemCopy2.y--;
              itemCopy2.rows++;
              while(iter && !gridster.checkCollision(itemCopy2)){
                itemCopy2.y--;
                itemCopy2.rows++;
                iter--;
              }
              itemCopy2.y++;
              itemCopy2.rows--;
              //South
              itemCopy2.rows++;
              while(iter && !gridster.checkCollision(itemCopy2)){
                itemCopy2.rows++;
                iter--;
              }
              itemCopy2.rows--;
              if(!iter){
                return itemOri;
              }
              else{
                var mousex = itemOri.x;  
                var mousey = itemOri.y;
                var boxcenterx = itemCopy2.x + (itemCopy2.cols/2);
                var boxcentery = itemCopy2.y + (itemCopy2.rows/2);
                var center = gridster.$options.emptyCellAlignCenter;
                if(Math.abs(boxcenterx - mousex) > center){
                  itemCopy2.cols = Math.ceil(itemCopy2.cols/2);
                  if(boxcenterx - mousex < 0 ){
                    itemCopy2.x = Math.ceil(boxcenterx);
                  }
                }
                if(Math.abs(boxcentery - mousey) > center){
                  itemCopy2.rows = Math.ceil(itemCopy2.rows/2);
                  if(boxcentery - mousey < 0 ){
                    itemCopy2.y = Math.ceil(boxcentery);
                  }
                }
                return itemCopy2;
              }
            }
            else{
              item = itemCopy;
            }
            iter--;
          }
        }
      };
    };
  }
})();
