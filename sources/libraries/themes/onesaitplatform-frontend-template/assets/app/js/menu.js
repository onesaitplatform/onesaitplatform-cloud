var MenuController = function() {
    
	// DEFAULT PARAMETERS, VAR, CONSTS. 
    var APPNAME = 'onesait Platform Frontend'; 
	var LIB_TITLE = 'Menu Controller';	
    var logControl = 1;
	var LANGUAGE = ['en'];
	var currentLanguage = ''; // loaded from template.
	
	// CONTROLLER PRIVATE FUNCTIONS
	
		
	// LOAD MENU INFORMATION FROM USER-ROLE 
	// get JSON from HTML -> 
	
	var dashboardFn = function(obj){
		// obj: {src:"https://antwerp4cities.onesaitplatform.com/controlpanel/dashboards/view/04c30518-56f6-4315-9fb9-25cd23b4380b/",title:"Confort Index Dashboard", background: '', height: '600px', mode: 'INSERT'}
		dashboardFunctionStr = '';		
		dashboardFunctionStr = 'onclick="FrontendController.loadDashboard(\''+ obj.src +'\',\''+ obj.title +'\',\''+ obj.background +'\',\''+ obj.height +'\',\''+ obj.mode +'\')"';
		return dashboardFunctionStr;
	}
	
	
	var consoleMenu = function(){
		
		logControl ? console.log('|---> consoleMenu() -> Creating HTML Console Menu') : '';
		
		var menu_HTML	= ''
		,submenu_HTML	= ''
		,markUp_HTML	= ''
		,page_LANG 		= ''
		,menu_LANG 		= ''
		,heading		= ''
		,icon			= 'flaticon-more-v3'
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
		let dash = '';
		
		navItemsArr.map(function(item, index){
			
			// CLEAN VARS FOR EACH LOOP.
			markUp_HTML = menu_HTML = submenu_HTML = '';			
			logControl ? console.log('     |---> navItem-' + index + 'Item: ' + item.title.ES + ';  Submenus: ' + item.submenu.length + ' ' + submenus ) : '';
			
			if ( hasSubmenus(item) ){
				submenus = true;										
				menu_HTML	+= '<li class="m-menu__item m-menu__item--submenu m-menu__item--bottom-1" aria-haspopup="true" m-menu-submenu-toggle="hover">'
							 + '<a href="javascript:;" class="m-menu__link m-menu__toggle"><i class="m-menu__link-icon '+ item.icon +'"></i><span class="m-menu__link-text">'+ item.title[ menu_LANG ] +'</span><i class="m-menu__ver-arrow la la-angle-right"></i></a>'
							 + '<div class="m-menu__submenu m-menu__submenu--up" style="">'
							 + '<span class="m-menu__arrow"></span>'
							 + '<ul class="m-menu__subnav">';
							
				
				// SUB-NAV-ITEM LOOP
				item.submenu.map(function(subitem, subindex){					
					if (subitem.url !== ''){
					submenu_HTML   +='<li class="m-menu__item " aria-haspopup="true" m-menu-link-redirect="1">'
									+'<a href="'+ subitem.url +'" class="m-menu__link "><i class="m-menu__link-bullet m-menu__link-bullet--dot"><span></span></i><span class="m-menu__link-text">'+ subitem.title[ menu_LANG ] +'</span></a>'
									+'</li>';
					}
					else if (!jQuery.isEmptyObject(subitem.dashboard)){
						dash = dashboardFn(subitem.dashboard);
						submenu_HTML   +='<li class="m-menu__item " aria-haspopup="true" m-menu-link-redirect="1">'
									+'<a href="#" '+ dash +' class="m-menu__link "><i class="m-menu__link-bullet m-menu__link-bullet--dot"><span></span></i><span class="m-menu__link-text">'+ subitem.title[ menu_LANG ] +'</span></a>'
									+'</li>';
					}
					logControl ? console.log('     |---> sub navItem-'+ subindex + '; SubItem: ' + subitem.title[ menu_LANG ] + '.') : '';
							
				});
				// add submenus and close submenu ul of nav-item.
				menu_HTML += submenu_HTML + '	</ul></div>' + '</li>';
				
				// ADD TO FINAL MARKUP AND APPENTO MENU (.page-sidebar-menu)
				markUp_HTML += menu_HTML;
				$(markUp_HTML).appendTo($('#app-asideMenu'));				
			}
			else {
				// NAV-ITEM WITHOUT SUBMENU
				submenus = false;
				// CHECK FOR SEPARATOR -> MENU WITHOUT SUBMENUS AND NULL LINK
				if (item.url === '' && jQuery.isEmptyObject(item.dashboard)){
					icon = item.icon !== '' ? item.icon : icon; 
					menu_HTML  +='<li class="m-menu__section ">'
								+'<h4 class="m-menu__section-text">' + item.title[ menu_LANG ] + '</h4>'
								+'<i class="m-menu__section-icon '+ icon +'"></i>'
								+'</li>';
				}
				else{
					// NEW FUNCTIONALITY TO MAKE LINK OR TO LOAD DASHBOARD ON SRC.
					if (item.url !== ''){ 
						menu_HTML	+='<li class="m-menu__item " aria-haspopup="true" m-menu-link-redirect="1">'
									+'<a href="'+ item.url +'" class="m-menu__link "><i class="m-menu__link-icon '+ item.icon +'"></i><span class="m-menu__link-text">'+ item.title[ menu_LANG ] +'</span></a>'
									+'</li>';
					}
					else if (!jQuery.isEmptyObject(item.dashboard)){
						// new to load DASHBOARD no to link page.
						dash = dashboardFn(item.dashboard);
						menu_HTML	+='<li class="m-menu__item " aria-haspopup="true" m-menu-link-redirect="1">'
									+'<a href="#" '+ dash +' class="m-menu__link "><i class="m-menu__link-icon '+ item.icon +'"></i><span class="m-menu__link-text">'+ item.title[ menu_LANG ] +'</span></a>'
									+'</li>';
					}
				}
				
				// ADD AND APPENTO MENU (.page-sidebar-menu) 
				$(menu_HTML).appendTo($('#app-asideMenu'));				
			}			
		});
		// SET ACTIVE NAV.
		setActiveNavItem();
		
		// SHOW OR NOT HOME MENU ITEM:
		menuJson.home ? $('li.m-menu__item.start').removeClass('m--hide') : $('li.m-menu__item.start').addClass('m--hide');
		
	}
	
	// AUX. CHECK IF A NAV-ITEM HAD SUBMENU ITEMS
	var hasSubmenus = function(item){ var checkSubmenus = item.submenu.length > 0 ? true : false; return checkSubmenus;  }
	
	// AUX. GET CURRENT PAGE URL AND DETECT ACTIVE NAV-ITEM 
	var setActiveNavItem = function(){
		
		logControl ? console.log('|---> setActiveNavItem() -> Setting current nav-item Active') : '';
		
		var templ = top.location.pathname ? top.location.pathname : window.location.pathname;
		var page = templ.split("/").pop();		
		logControl ? console.log('|---> CURRENT PAGE: ' +  page) : '';
		
		// CHECK FIRST NAV (HOME) EXCEP.
		firstMenu = $('#app-asideMenu > li.m-menu__item.start > a.m-menu__link');	
		
		if ( page === 'index.html'){ firstMenu.closest('li.m-menu__item').addClass('m-menu__item--active'); return false;} else { firstMenu.closest('li.m-menu__item').removeClass('m-menu__item--active');}
		
		// GET ALL NAVS, THEN CHECK URL vs. CURRENT PAGE --> ACTIVE.
		var allMenus = $('#app-asideMenu > li.m-menu__item > div.m-menu__submenu > ul.m-menu__subnav  > li.m-menu__item > a.m-menu__link');
		allMenus.each(function(ilink,navlink){
				
			logControl ? console.log('|---> nav-link-' + ilink + ' URL: ' + navlink + ' PAGE: ' + $(this)[0].pathname) : '';
			
			if ( currentPath === $(this)[0].pathname ){					
				currentLi = $(this).closest('li.m-menu__item');
				currentNav = currentLi.parents('.m-menu__item');
				
				// APPLY ACTIVE CLASSES
				currentLi.addClass('m-menu__item--open');							
				currentNav.addClass('m-menu__item--active');
				currentNav.find('.arrow').addClass('open');
				return false;				
			}			
		});		
	}
	
	
	// CONTROLLER PUBLIC FUNCTIONS 
	return{
		
		// LOAD() JSON LOAD FROM TEMPLATE TO CONTROLLER
		load: function(Data) { 
			logControl ? console.log("\n" +LIB_TITLE + ': load()') : '';
			return menuReg = Data;
		},
		
		lang: function(lang){
			logControl ? console.log("\n" +LIB_TITLE + ': lang()') : '';
			logControl ? console.log('|---> lang() -> assign current Language to Console Menu: ' + lang) : '';
			return currentLanguage = lang;
			
		},
		
		// INIT() CONTROLLER INIT CALLS
		init: function(){
			logControl ? console.log("\n" +LIB_TITLE + ': init()') : '';
			
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
			})
		}		
	}
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
