var loadStretchy = function(){
    	if( $('.cd-stretchy-nav').length > 0 ) {
    		var stretchyNavs = $('.cd-stretchy-nav');
    		
    		stretchyNavs.each(function(){
    			var stretchyNav = $(this),
    				stretchyNavTrigger = stretchyNav.find('.cd-nav-trigger');
    			
    			stretchyNavTrigger.on('click', function(event){
    				event.preventDefault();
    				stretchyNav.toggleClass('nav-is-visible');
    			});
    		});

    		$(document).on('click', function(event){
    			( !$(event.target).is('.cd-nav-trigger') && !$(event.target).is('.cd-nav-trigger span') ) && stretchyNavs.removeClass('nav-is-visible');
    		});
    	}
    	
    	if( $('.cd-gravitee-nav').length > 0 ) {
    		var stretchyNavs = $('.cd-gravitee-nav');
    		
    		stretchyNavs.each(function(){
    			var stretchyNav = $(this),
    				stretchyNavTrigger = stretchyNav.find('.cd-gravitee-trigger');
    			
    			stretchyNavTrigger.on('click', function(event){
    				event.preventDefault();
    				stretchyNav.toggleClass('nav-is-visible');
    			});
    		});

    		$(document).on('click', function(event){
    			( !$(event.target).is('.cd-gravitee-trigger') && !$(event.target).is('.cd-gravitee-trigger span') ) && stretchyNavs.removeClass('nav-is-visible');
    		});
    	}
}