var urlChanged=false;
var bearer = null;
if (window.location.href.split("?").length>1){
	if(window.location.href.split("?")[1].split("=").length>1 ){
		bearer = decodeURI(window.location.href.split("?")[1].split("=")[1])
	}
}
if(bearer!=null){
	const date = new Date()
	date.setMinutes(date.getMinutes() + 1);
	document.cookie = "platformbearer=" + bearer + '; expires=' + date.toUTCString();
}
$(document).ready(function(){
	$.get("/controlpanel/static/notebooks/startapp.html").done(function(data){
		var readyhash = '/controlpanel/notebooks/app/' + location.hash
		history.pushState(null, null, readyhash);
		var contentAndHook = data.replace("html ","div ");
		$("#angularAPP").html(contentAndHook);
		$(".navbar-inverse").parent().remove();
		jQuery.fn.size = function() {//compatibility with controlpanel jquery version and menu
		    return this.length;
		};
		var observer = new MutationObserver(fnHandler),
				elTarget = document.querySelector("#angularAPP"),
				objConfig = {
					childList: true,
					subtree : true,
					attributes: false, 
					characterData : false
				};

		//then actually do some observing
		observer.observe(elTarget, objConfig);
		
		var loadname = true;
		var loadnameresponse;
		
		//tooltip avoid error
		$( 'body > div > div.page-sidebar-wrapper > div > ul > li > a').tooltip();
		initHandleSidebarMenu();
		
		$("#button-sidebar-toggler").click(
			function(){
				if($(".page-sidebar-menu").hasClass("page-sidebar-menu-closed")) {
					$(".noteAction").css("margin-left", "240px","important");
					$(".noteAction").css("width", "calc(100% - 240px)","important");
					/*window.setTimeout(
							function(){
								initHandleSidebarMenu(true);
							},400
						)*/
				} else {
					$(".noteAction").css("margin-left", "60px","important");
					$(".noteAction").css("width", "calc(100% - 60px)","important");
					/*window.setTimeout(
						function(){
							initHandleSidebarMenu(false);
						},400
					)*/
				}
			}
		);
		
		function fnHandler () {
			
			$(".dropdown-toggle:not(#close-session)").dropdown();

			$(".dropdown.navbar-right").dropdown();

			$(".dropdown.navbar-right").on("click",function(){
				$(this).addClass("open");
			})
			
			if(angular ){
				if(!urlChanged){
					var elem = angular.element(document.querySelector('[ng-controller]'));

					//get the injector.
					var injector = elem.injector();

					//get the service.
					var urlService = injector.get('baseUrlSrv');
					
					urlService.getPort=function(){return portGlobal;};
					urlService.getWebsocketUrl=function(){return (portGlobal===443?"wss":"ws") + "://" + hostnameGlobal + ":" + portGlobal + "/ws"};
					urlService.getRestApiBase=function(){return "api"};
					
					//apply the changes to the scope.
					elem.scope().$apply();
					urlChanged=true;
				}
			}
			
			if($( ".labelBtn button .icon-trash" ).length==1){
				$( ".labelBtn button .icon-trash" ).parent().remove(); // trash icon
			}
			
			if($( ".labelBtn button .fa-users" ).length==1){
				$( ".labelBtn button .fa-users" ).parent().remove(); //user icon
			}
			
			if($( ".labelBtn button .fa-copy" ).length==1){
				$( ".labelBtn button .fa-copy" ).parent().remove(); //clone icon
			}
			
			if($(".setting-btn .fa-lock").length==1){
				$(".setting-btn .fa-lock").parent().remove()
			}

			if($(".labelBtn button .fa-download").length==1){
				$(".labelBtn button .fa-download").parent().remove()
			}

			if($("headroom span.btn-group:last").length==1){
				$("headroom span.btn-group:last").css("display","none")
			}

			if($(".ui-resizable-handle.ui-resizable-se.ui-icon.ui-icon-gripsmall-diagonal-se").length>0){
				$(".ui-resizable-handle.ui-resizable-se.ui-icon.ui-icon-gripsmall-diagonal-se").remove();
			}

			if($(".navbar-inverse").length==1){
				$(".navbar-inverse").parent().remove();
			}
			
			$(".noteAction h3 div p").off();
			
			$(".noteAction h3 div p").html("");
			
			/*Notebook editor view*/
			if(location.hash.indexOf("#/notebook")!==-1){
				/*iFrame view*/
				if(location.hash.substring(location.hash.length-8)==="asIframe" || bearer!=null){
					$(".page-content").css("cssText","margin: 0px !important;padding-top:0px !important");
					$("body").css("background-color","transparent");
					$(".notebookContent").css("padding-top","0px");
					$(".row").css("margin","0px");
					$(".mainrow").css("margin-top","-55px");
					$("#main").css("margin-top", "50px").css("height","initial");
					$(".noteAction.headroom--top").each(function () {
					    this.style.setProperty( 'top', '10px', 'important' );
					    this.style.setProperty( 'padding-left', '20px' );
					    this.style.setProperty( 'padding-top', '5px' );
					});
					$(".noteAction.headroom--pinned").each(function () {
					    this.style.setProperty( 'top', '10px', 'important' );
					    this.style.setProperty( 'padding-left', '20px' );
					    this.style.setProperty( 'padding-top', '5px' );
					});
				}
				/*Notebook view*/
				else{
					$(".page-header,footer,.page-sidebar-wrapper,.page-bar").css("display","block");
					$("body").css("color","#000");
					if($(".noteAction").css("margin-left") != '60px' && $(".noteAction").css("margin-left") != '240px') {
						$(".noteAction").css("width", "calc(100% - 60px)","important");	
						$(".noteAction").css("margin-left", "60px","important");
					}
					$(".noteAction").addClass("pageCreateHeader");
					$(".row").css("margin","initial");
					$("#main").css("margin-top", "initial").css("height","100%").css("width","100%");	
				}
				
				if(location.hash.substring(location.hash.length-8)!=="asIframe"){
					notebookId = location.hash.substring(location.hash.indexOf("#/notebook/")+11).split(/[\s,?]+/)[0]
					if(loadname){
						loadname = false;
						var headers = {}
						if (bearer) {
							headers = {'Authorization': 'Bearer ' + bearer}
						}
						$.get(
							{
								url:"../app/nameByIdZep/"+notebookId,
								dataType:"text",
								headers: headers
						}).done(function(data){
							loadnameresponse = '<b class="form-control-static2 ellipsis">' + data + '</b>';		
						}).fail(function(e){
							console.log("Error in name note, message detail: " + e.responseText);
						})
					}
					
					if(loadnameresponse){ 
						if($(".noteAction h3 div p").length>0){
							$(".noteAction h3 div").first().html(loadnameresponse);
							loadnameresponse = null;
						}
					}
				}
				$(".ui-grid-cell-contents").css("height","auto","important");
				$(".ui-grid-header-cell-wrapper").css("height","auto","important");
				$(".ace_line").css("white-space","inherit")
			}
			/*No notebook view, configurations, interpreters,...*/
			else{
				$(".page-header,footer,.page-sidebar-wrapper,.page-bar").css("display","block");
				$("#main").css("width","calc(100% - 70px)","important").css("height","initial");;
			}
			
		}
		
	});
})

function initHandleSidebarMenu() {
	$("a.nav-link.tooltips").on("click", function(){
		if(!$(".page-sidebar-menu").hasClass("page-sidebar-menu-closed")){
			$(".tooltip").remove();
	        var that = $(this).closest('.nav-item').children('.nav-link');

	        var hasSubMenu = that.next().hasClass('sub-menu');
	
	        var parent =that.parent().parent();
	        var the = that;
	        var menu = $('.page-sidebar-menu');
	        var sub = that.next();
	
	        var autoScroll = menu.data("auto-scroll");
	        var slideSpeed = parseInt(menu.data("slide-speed"));
	        var keepExpand = menu.data("keep-expanded");
	        
	        if (parent.children('li.open').hasClass('open')) {
	            parent.children('li.open').children('a').children('.arrow').removeClass('open');
	            parent.children('li.open').children('.sub-menu:not(.always-open)').slideUp(slideSpeed);
	            parent.children('li.open').removeClass('open');
	        }
	
	        var slideOffeset = -200;
	
	        if (sub.is(":visible")) {
	            $('.arrow', the).removeClass("open");
	            the.parent().removeClass("open");
	            sub.slideUp(slideSpeed);
	        } else if (hasSubMenu) {
	            $('.arrow', the).addClass("open");
	            the.parent().addClass("open");
	            sub.slideDown(slideSpeed);
	        }
		}
	})
}