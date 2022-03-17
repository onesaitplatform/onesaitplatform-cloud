var MenuController = function() {
    
	// DEFAULT PARAMETERS, VAR, CONSTS. 
    var APPNAME = 'onesait Platform Control Panel'; 
	var LIB_TITLE = 'Menu Controller';	
    var logControl = 0;
	var LANGUAGE = ['es'];
	var currentLanguage = ''; // loaded from template.
	
	// CONTROLLER PRIVATE FUNCTIONS
	
		
	// LOAD MENU INFORMATION FROM USER-ROLE 
	// get SERVER-JSON from header.html -> headerReg.menu and CREATE HTML MENU.
	var consoleMenu = function(){
		
		logControl ? console.log('|---> consoleMenu() -> Creating HTML Console Menu') : '';
		
		var menu_HTML	= ''
		,submenu_HTML	= ''
		,markUp_HTML	= ''
		,page_LANG 		= ''
		,menu_LANG 		= ''
		,heading		= ''
		.icon			= 'icomoon'
		,submenus 		= false;
		
		// NO-DATA NO-FUN!
		if (!menuReg){ $.alert({title: 'MENU ERROR!',content: 'No Menu Data!'}); return false; }
		
		// GET JSON FROM CONTROLLER LOAD()
		var menuJson = menuReg;
		
		// GET CURRENT LANGUAGE OR 'ES'.
		page_LANG = currentLanguage || LANGUAGE;
		menu_LANG = page_LANG.toString().toUpperCase();
		
		logControl ? console.log('     |---> menu: ' + menuJson.menu + ' NoSession Path: ' + menuJson.noSession + ' Rol: ' + menuJson.rol + ' Navigation Objects: ' + menuJson.navigation.length + ' Language: ' + menu_LANG ) : '';
		
		// NAV-ITEM MAIN LOOP
		var navItemsArr = menuJson.navigation;
		navItemsArr.map(function(item, index){
			
			// CLEAN VARS FOR EACH LOOP.
			markUp_HTML = menu_HTML = submenu_HTML = '';			
			logControl ? console.log('     |---> navItem-' + index + 'Item: ' + item.title.ES + ';  Submenus: ' + item.submenu.length + ' ' + submenus ) : '';
			
			if ( hasSubmenus(item) ){
				submenus = true;
				menu_HTML  +='<li class="nav-item">'
							+'	<a  class="nav-link dropdown-toggle tooltips" data-toggle="dropdown" data-placement="right"  data-original-title="'+item.title[ menu_LANG ]+'">'
							+'		<i class="'+ item.icon +'"></i>'
							+'		<span class="title">'+ item.title[ menu_LANG ] +'</span>'
							+'		<span class="arrow"></span>'
							+'	</a>'
							+'	<ul class="dropdown-menu dropdown-submenu-position">';
				
				//Add a info on title on submenu
				
					menu_HTML   +='<li class="info-parent-submenu">'+ item.title[ menu_LANG ]+'</li>';										
									
				
				
				// SUB-NAV-ITEM LOOP			 
				item.submenu.map(function(subitem, subindex){					
					
					submenu_HTML   +='<li class="nav-item">'
									+'	<a href="'+ subitem.url +'" class="nav-link nav-toggle">'
									+'		<i class="'+ subitem.icon +'"></i>'
									+'		<span class="title">'+ subitem.title[ menu_LANG ] +'</span>'
									+'	</a>'
									+'</li>';
							
					logControl ? console.log('     |---> sub navItem-'+ subindex + '; SubItem: ' + subitem.title[ menu_LANG ] + '.') : '';
							
				});
				// add submenus and close submenu ul of nav-item.
				menu_HTML += submenu_HTML + '	</ul>' + '</li>';
				
				// ADD TO FINAL MARKUP AND APPENTO MENU (.page-sidebar-menu)
				markUp_HTML += menu_HTML;
				$(markUp_HTML).appendTo($('.page-sidebar-menu'));				
			}
			else {
				// NAV-ITEM WITHOUT SUBMENU
				submenus = false;
				// CHECK FOR SEPARATOR -> MENU WITHOUT SUBMENUS AND NULL LINK
				if (item.url === ''){
					icon = item.icon !== '' ? item.icon : icon; 
					heading = item.title[ menu_LANG ] === '' ? '<i class="'+ icon +'"></i>'  : item.title[ menu_LANG ];
					menu_HTML  +='<li class="heading text-center">'
								+'	<h3 class="uppercase titleHeading"> ' + heading + '</h3>'
								+'</li>';
				}
				else{
					menu_HTML  +='<li class="nav-item">'
								+'	<a class="nav-link dropdown-toggle tooltips" data-toggle="dropdown" data-placement="right" data-original-title="'+item.title[ menu_LANG ]+'">'
								+'		<i class="'+ item.icon +'"></i>'
								+'		<span class="title">'+ item.title[ menu_LANG ] +'</span>'
								+'	</a>'
								+'</li>';
				}
				
				// ADD AND APPENTO MENU (.page-sidebar-menu) 
				$(menu_HTML).appendTo($('.page-sidebar-menu'));				
			}			
		});
		// SET ACTIVE NAV.
		setActiveNavItem();
		
	}
	
	// AUX. CHECK IF A NAV-ITEM HAD SUBMENU ITEMS
	var hasSubmenus = function(item){ var checkSubmenus = item.submenu.length > 0 ? true : false; return checkSubmenus;  }
	
	// AUX. GET CURRENT PAGE URL AND DETECT ACTIVE NAV-ITEM 
	var setActiveNavItem = function(){
		
		logControl ? console.log('|---> setActiveNavItem() -> Setting current nav-item Active') : '';
		
		var currentPath = window.location.pathname;		
		logControl ? console.log('|---> CURRENT PATH: ' +  currentPath) : '';
		
		// CHECK FIRST NAV (HOME) EXCEP.
		firstMenu = $('.page-sidebar-menu > li.nav-item.start > a.nav-link.nav-toggle');
		if ( currentPath === '/controlpanel/main' ){ firstMenu.closest('li.nav-item').addClass('open active'); return false;} else { firstMenu.closest('li.nav-item').removeClass('open active');}
		
		// GET ALL NAVS, THEN CHECK URL vs. CURRENT PATH --> ACTIVE.
		var allMenus = $('.page-sidebar-menu > li.nav-item > ul > li.nav-item > a.nav-link.nav-toggle');
		
		
		allMenus.each(function(ilink,navlink){
				
			logControl ? console.log('|---> nav-link-' + ilink + ' URL: ' + navlink + ' PATH: ' + $(this)[0].pathname) : '';
			
			if ( currentPath === $(this)[0].pathname ){					
				currentLi = $(this).closest('li.nav-item');
				currentNav = currentLi.parents('.nav-item');
				
				// APPLY ACTIVE CLASSES
				currentLi.addClass('active open');							
				currentNav.addClass('active open');
				currentNav.find('.arrow').addClass('open');
				return false;				
			}			
		});		
	}
	
	
	// CONTROLLER PUBLIC FUNCTIONS 
	return{
		
		// LOAD() JSON LOAD FROM TEMPLATE TO CONTROLLER
		load: function(Data) { 
			logControl ? console.log(LIB_TITLE + ': load()') : '';
			return menuReg = Data;
		},
		
		lang: function(lang){
			logControl ? console.log(LIB_TITLE + ': lang()') : '';
			logControl ? console.log('|---> lang() -> assign current Language to Console Menu: ' + lang) : '';
			return currentLanguage = lang;
			
		},
		
		// INIT() CONTROLLER INIT CALLS
		init: function(){
			logControl ? console.log(LIB_TITLE + ': init()') : '';
			
			// load menu (role)
			consoleMenu();
			
			// INICIALIZACIÓN DE BUSCADORES LIVE-SEARCH 
			$('#search-query').keyup(function(){
			
			var searchValue = $(this).val().toLowerCase();
				// selector para coger todos los span donde están los títulos de los menus.
				var Menus = $('.page-sidebar-menu > li.nav-item > ul.sub-menu  > li.nav-item > a.nav-link.nav-toggle > span.title');
					
				// live-search 
				var matchProjects = '';
				if (searchValue !== '') {
					Menus.each(function( index ){
						if (index < 0) { return; }
						var menuTitle = $(this).text().toLowerCase();
						if ( menuTitle.includes(searchValue) ){
							//lo incluye: hacemos algo para remarcar el título o lo que se nos ocurra, o nada.
							logControl ? console.log($(this).text() + ' - ' + searchValue + '-> SI'): '';
							 $(this).parents("li.nav-item > ul.sub-menu  > li.nav-item").show();	
						}
						else {
							// no lo incluye
							logControl ? console.log($(this).text() + ' - ' + searchValue + ' -> NO'): '';
							$(this).parents("li.nav-item > ul.sub-menu  > li.nav-item").hide();							
						}
					});
					
					// CONTROL DE MENUS PPALES completamente ocultos
					var submenus = $('.page-sidebar-menu > li.nav-item > ul.sub-menu');
					submenus.each(function( index ){
						logControl ? console.log(index +' totales: ' + $(this).children().length + ' ocultos: ' + $(this).children('li[style*="display: none"]').length): '';						
						if ($(this).children().length == $(this).children('li[style*="display: none"]').length){							
							$(this).parent('li.nav-item').addClass('hided').hide();
						}
						else{
							$(this).parent('li.nav-item').removeClass('hided').show();
						}
						
					});

					
				}
				else{
					// si hay algún proyecto oculto lo volvemos a mostrar	
					$('.page-sidebar-menu > li.nav-item > ul.sub-menu  > li.nav-item').show();
					$('.hided').removeClass('hided').show();
				}		
				
				
				
			});
			
			
		}		
	};
}();

// AUTO INIT CONTROLLER WHEN READY
jQuery(document).ready(function() {
	
	// LOADING JSON DATA FROM THE TEMPLATE (CONST, i18, ...)
	MenuController.load(menuJson);
	// LOADING CURRENT LANGUAGE FROM THE TEMPLATE
	MenuController.lang(currentLanguage);	
	// AUTO INIT CONTROLLER.
	MenuController.init();
});
