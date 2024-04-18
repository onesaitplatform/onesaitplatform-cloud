
// This js file contains funtions for datatable, like to show dropdown correctly when a filter is make or page is changed

$(window).on("load",function(){  
	$('table').on('preDraw.dt', function() {
		$("body").click();
	});
	$('table').on('draw.dt', function() {
		$('.download-menu').parent().off('shown.bs.dropdown');
		$('.download-menu').parent().off('hide.bs.dropdown');
	
		$('.download-menu').parent().on('shown.bs.dropdown', function() {
			var $menu = $("ul", this);
			offset = $menu.offset();
			position = $menu.position();
			$('body').append($menu);
			$menu.show();
			$menu.css('position', 'absolute');
			$menu.css('top', (offset.top) + 'px');
			$menu.css('left', (offset.left) + 'px');
			$menu.css('min-width', '100px');
			$(this).data("myDropdownMenu", $menu);
		});
		$('.download-menu').parent().on('hide.bs.dropdown', function() {
			$(this).append($(this).data("myDropdownMenu"));
			$(this).data("myDropdownMenu").removeAttr('style');
		});
	});
});